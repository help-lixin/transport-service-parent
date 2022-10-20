package help.lixin.transport.util;

import com.google.common.net.HostAndPort;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public final class Address {
    private static final int DEFAULT_PORT = 5679;
    private final String host;
    private final int port;
    private transient volatile Type type;
    private volatile InetSocketAddress socketAddress;

    public Address(final String host, final int port) {
        this(host, port, null);
    }

    public Address(final String host, final int port, final InetAddress address) {
        this.host = host;
        this.port = port;
        if (address != null) {
            type = address instanceof Inet6Address ? Type.IPV6 : Type.IPV4;
            socketAddress = new InetSocketAddress(address, port);
        } else {
            socketAddress = InetSocketAddress.createUnresolved(host, port);
        }
    }

    /**
     * Returns an address that binds to all interfaces.
     *
     * @return the address
     */
    public static Address local() {
        return from(DEFAULT_PORT);
    }

    /**
     * Returns the address from the given host:port string.
     *
     * @param address the address string
     * @return the address
     */
    public static Address from(final String address) {
        try {
            final HostAndPort parsedAddress =
                    HostAndPort.fromString(address).withDefaultPort(DEFAULT_PORT);
            return new Address(parsedAddress.getHost(), parsedAddress.getPort());
        } catch (final IllegalStateException e) {
            return from(DEFAULT_PORT);
        }
    }

    /**
     * Returns an address for the given host/port.
     *
     * @param host the host name
     * @param port the port
     * @return a new address
     */
    public static Address from(final String host, final int port) {
        return new Address(host, port);
    }

    /**
     * Returns an address for the local host and the given port.
     *
     * @param port the port
     * @return a new address
     */
    public static Address from(final int port) {
        try {
            final InetAddress address = getLocalAddress();
            return new Address(address.getHostName(), port);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Failed to locate host", e);
        }
    }

    /**
     * Returns the local host.
     */
    private static InetAddress getLocalAddress() throws UnknownHostException {
        try {
            return InetAddress.getLocalHost(); // first NIC
        } catch (final Exception ignore) {
            return InetAddress.getByName(null);
        }
    }

    /**
     * Returns the host name.
     *
     * @return the host name
     */
    public String host() {
        return host;
    }

    /**
     * Returns the port.
     *
     * @return the port
     */
    public int port() {
        return port;
    }

    /**
     * Returns the IP address.
     *
     * @return the IP address
     */
    public InetAddress address() {
        return address(false);
    }

    /**
     * Returns the IP address.
     *
     * @param resolve whether to force resolve the hostname
     * @return the IP address
     */
    public InetAddress address(final boolean resolve) {
        if (resolve || socketAddress.isUnresolved()) {
            // the constructor will by default attempt to resolve the host, and will fallback to the an
            // unresolved address if it couldn't
            socketAddress = new InetSocketAddress(host, port);
            return socketAddress.getAddress();
        }

        return socketAddress.getAddress();
    }

    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    /**
     * Returns the address type.
     *
     * @return the address type
     */
    public Type type() {
        if (type == null) {
            synchronized (this) {
                if (type == null) {
                    type = address() instanceof Inet6Address ? Type.IPV6 : Type.IPV4;
                }
            }
        }
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Address that = (Address) obj;
        return host.equals(that.host) && port == that.port;
    }

    @Override
    public String toString() {
        final String host = host();
        final int port = port();
        if (host.matches("([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}")) {
            return String.format("[%s]:%d", host, port);
        } else {
            return String.format("%s:%d", host, port);
        }
    }

    /**
     * Address type.
     */
    public enum Type {
        IPV4,
        IPV6,
    }
}

