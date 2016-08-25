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
package net.segoia.eventbus.web.websocket.client;

import java.net.URI;

import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.peers.EventNode;
import net.segoia.event.eventbus.peers.PeerEventContext;

public class SerialClientWebsocketRelay extends ParallelClientWebsocketRelay {

    public SerialClientWebsocketRelay(String id, EventNode parentNode, URI uri) {
	super(id, parentNode, uri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.event.eventbus.peers.EventRelay#sendEvent(net.segoia.event.eventbus.Event)
     */
    @Override
    protected void sendEvent(Event event) {
	/*
	 * instead of sending the event to the peer relay, we'll sent it to the parent node, to distribute it to all the
	 * peers
	 */
	parentNode.onRemoteEvent(new PeerEventContext(this, event));
    }

}
