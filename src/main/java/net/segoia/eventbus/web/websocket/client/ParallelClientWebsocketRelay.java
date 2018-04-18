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

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.peers.EventNode;
import net.segoia.event.eventbus.peers.EventRelay;

/**
 * This websocket relay will simply forward the remote events to the websocket, and all events from the ws to the peer
 * relay, without notifying the parent node
 * 
 * @author adi
 *
 */
public class ParallelClientWebsocketRelay extends EventRelay {
    private EventNodeWebsocketClientEndpoint ws;
    private URI uri;

    public ParallelClientWebsocketRelay(String id, EventNode parentNode, URI uri) {
	super(id, parentNode);
	this.uri = uri;
    }

    @Override
    protected void init() {

	ws = new EventNodeWebsocketClientEndpoint(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.event.eventbus.peers.EventRelay#start()
     */
    @Override
    protected void start() {
//	WebSocketContainer wsContainer = ContainerProvider.getWebSocketContainer();
//	try {
//	    wsContainer.connectToServer(ws, uri);
//	} catch (Exception e) {
//	    e.printStackTrace();
//	}
	
	ws.connect(uri);
    }

    @Override
    protected void cleanUp() {
	ws.terminate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.event.eventbus.peers.EventRelay#onRemoteEvent(net.segoia.event.eventbus.Event)
     */
    @Override
    public void onRemoteEvent(Event event) {
	/* this is just a proxy, not a real node, so don't add ourselves as a relay node */
//	event.addRelay(getParentNodeId());
	ws.sendEvent(event);
    }

    /* (non-Javadoc)
     * @see net.segoia.event.eventbus.peers.EventRelay#sendEvent(net.segoia.event.eventbus.Event)
     */
    @Override
    protected void sendEvent(Event event) {
	/* we don't want to add this proxy as a relay */
	transceiver.onRemoteEvent(event);
    }
    
    /**
     * Called when ws closed due to error or by the other end
     */
    public void onWsClosed() {
	
    }

}
