/*
 * Copyright 2018-present Open Networking Foundation
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

import help.lixin.transport.event.ListenerService;
import help.lixin.transport.event.NodeDiscoveryEvent;
import help.lixin.transport.event.NodeDiscoveryEventListener;
import help.lixin.transport.gossip.Node;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Node discovery service.
 */
public interface NodeDiscoveryService extends ListenerService<NodeDiscoveryEvent, NodeDiscoveryEventListener> {


    /**
     * Returns the set of active nodes.
     *
     * @return the set of active nodes
     */
    Set<Node> getNodes();

    CompletableFuture<NodeDiscoveryService> start();

    /**
     * Returns a boolean value indicating whether the managed object is running.
     *
     * @return Indicates whether the managed object is running.
     */
    boolean isRunning();

    /**
     * Stops the managed object.
     *
     * @return A completable future to be completed once the object has been stopped.
     */
    CompletableFuture<Void> stop();
}
