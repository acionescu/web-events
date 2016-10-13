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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.CloseReason.CloseCodes;

import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.EventContext;
import net.segoia.event.eventbus.EventHandle;
import net.segoia.event.eventbus.constants.EventParams;
import net.segoia.event.eventbus.constants.Events;
import net.segoia.eventbus.web.websocket.WsEndpoint;

@ClientEndpoint
public class EventNodeWebsocketClientEndpoint extends WsEndpoint {
    private ParallelClientWebsocketRelay localRelay;
    /* the id assigned by the server */
    private String remoteClientId;

    private boolean autoReconnect = true;

    /**
     * Wait this amount of time between reconnect attempts
     */
    private long reconnectDelay = 60000;

    private URI uri;

    public EventNodeWebsocketClientEndpoint(ParallelClientWebsocketRelay localRelay) {
	super();
	this.localRelay = localRelay;
    }

    public void connect(URI uri) {
	this.uri = uri;
	doConnect();
    }

    protected void doConnect() {
	WebSocketContainer wsContainer = ContainerProvider.getWebSocketContainer();
	try {
	    wsContainer.connectToServer(this, uri);
	} catch (Exception e) {
	    e.printStackTrace();

	    if (autoReconnect) {
		scheduleTask(new Callable<Void>() {

		    @Override
		    public Void call() throws Exception {
			System.out.println(getLocalNodeId() +" trying to reconnect to "+uri);
			doConnect();
			return null;
		    }
		}, reconnectDelay);
	    }
	}
    }

    @OnOpen
    public void onOpen(Session session) {
	this.session = session;
	state = WAIT_CONNECTED;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
	try {
	    Event event = Event.fromJson(message);
	    state.handleEvent(event, this);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.eventbus.web.websocket.WsEndpoint#sendEvent(net.segoia.event.eventbus.Event)
     */
    @Override
    public Future<Void> sendEvent(Event event) {
	// /* replace local id with remote id */
	// event.replaceRelay(getLocalNodeId(), remoteClientId);
	event.removeLastRelay();
	return super.sendEvent(event);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
	System.out.println(getLocalNodeId() + " closing ws "+closeReason);
	if(closeReason.getCloseCode().equals(CloseCodes.NORMAL_CLOSURE)) {
	    /* if we've closed the socket then return */
	    return;
	}
	    
	localRelay.onWsClosed();
	if(autoReconnect) {
	    doConnect();
	}
    }

    @Override
    public String getLocalNodeId() {
	/* since this is a simple proxy we need to use the remote node id */
	return localRelay.getRemoteNodeId();
    }

    @Override
    public void onAccepted() {
	state = ACCEPTED;
	System.out.println(getLocalNodeId() + " client ws ready");
    }

    protected void sendAuth() {
	Event event = Events.builder().ebus().peer().auth().build();
	event.addParam(EventParams.clientId, remoteClientId);
	sendEvent(event);
    }

    public static WsClientEndpointState ACCEPTED = new WsClientEndpointState() {

	@Override
	public void handleEvent(Event event, EventNodeWebsocketClientEndpoint wse) {
	    /* replace remote id with local id */
	    if (wse.remoteClientId.equals(event.to())) {
		event.to(wse.getLocalNodeId());
		/* replace remote id in the relay data as well */

		EventHandle eh = Events.builder().scope("WSCLIENT").category("EVENT").name("map-dest-id").getHandle();
		if (eh.isAllowed()) {
		    eh.addParam("old", wse.remoteClientId);
		    eh.addParam("new", wse.getLocalNodeId());
		    eh.post();
		}

	    }
	    wse.localRelay.onLocalEvent(new EventContext(event, null));

	}
    };

    public static WsClientEndpointState WAIT_CONNECTED = new WsClientEndpointState() {

	@Override
	public void handleEvent(Event event, EventNodeWebsocketClientEndpoint wse) {
	    String et = event.getEt();

	    switch (et) {
	    case "EBUS:PEER:CONNECTED":
		wse.remoteClientId = (String) event.getParam(EventParams.clientId);
		wse.state = WAIT_AUTH_RESPONSE;
		wse.sendAuth();

	    }

	}
    };

    public static WsClientEndpointState WAIT_AUTH_RESPONSE = new WsClientEndpointState() {

	@Override
	public void handleEvent(Event event, EventNodeWebsocketClientEndpoint wse) {
	    String et = event.getEt();

	    switch (et) {
	    case "EBUS:PEER:AUTHENTICATED":
		wse.onAccepted();
	    }

	}
    };

    @Override
    public void onError(Throwable e) {
	localRelay.terminate();

    }

}
