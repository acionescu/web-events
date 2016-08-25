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
import java.net.URISyntaxException;

import net.segoia.event.conditions.TrueCondition;
import net.segoia.event.eventbus.peers.AgentNode;
import net.segoia.event.eventbus.peers.EventRelay;

/**
 * This websocket client node will open a new websocket for each peer, in a one to one relationship
 * @author adi
 *
 */
public class ParallelWebsocketClientNode extends AgentNode {
    private URI uri;

    public ParallelWebsocketClientNode(URI uri) {
	super();
	this.uri = uri;
    }

    public ParallelWebsocketClientNode(String uri) throws URISyntaxException {
	this(new URI(uri));
    }

    public void start() throws Exception {
	
    }

    @Override
    protected void nodeConfig() {
	/* make sure we set autorelay enabled, otherwise all events coming from the websocket will be blocked by default */
	config.setAutoRelayEnabled(true);
	/* let the client send anything */
	config.setDefaultRequestedEvents(new TrueCondition());
    }

    @Override
    public void cleanUp() {
	
    }


    @Override
    protected EventRelay buildLocalRelay(String peerId) {
	return new ParallelClientWebsocketRelay(peerId, this, uri);
    }

    @Override
    protected void onTerminate() {
	// TODO Auto-generated method stub
	
    }

    
}
