/**
 * web-events - An event-bus extension for events transmission over websockets
 * Copyright (C) 2016  Adrian Cristian Ionescu - https://github.com/acionescu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.segoia.eventbus.web.ws.v0;

import net.segoia.event.eventbus.peers.PeerContext;
import net.segoia.event.eventbus.peers.PeerManager;
import net.segoia.event.eventbus.peers.PeerManagerFactory;

public class PeerManagerV0Factory implements PeerManagerFactory{

    @Override
    public PeerManager buildPeerManager(PeerContext peerContext) {
	return  new EventNodePeerManagerV0(peerContext);
    }

}
