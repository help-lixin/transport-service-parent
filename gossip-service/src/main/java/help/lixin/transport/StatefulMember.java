/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package help.lixin.transport;

import help.lixin.transport.util.Address;
import help.lixin.transport.util.Version;

import java.util.Properties;

/**
 * Default cluster node.
 */
public class StatefulMember extends Member {
    private final Version version;
    private volatile boolean active;
    private volatile boolean reachable;

    public StatefulMember(MemberId id, Address address) {
        super(id, address);
        this.version = null;
    }

    public StatefulMember(
            MemberId id,
            Address address,
            String zone,
            String rack,
            String host,
            Properties properties,
            Version version) {
        super(id, address, zone, rack, host, properties);
        this.version = version;
    }

    @Override
    public Version version() {
        return version;
    }

    /**
     * Sets whether this member is an active member of the cluster.
     *
     * @param active whether this member is an active member of the cluster
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Sets whether this member is reachable.
     *
     * @param reachable whether this member is reachable
     */
    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isReachable() {
        return reachable;
    }
}
