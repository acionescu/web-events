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
package net.segoia.eventbus.web.websocket.server;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.Session;

import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.constants.EventParams;
import net.segoia.event.eventbus.constants.Events;
import net.segoia.eventbus.web.websocket.WsEndpoint;
import net.segoia.eventbus.web.websocket.WsEndpointState;

public abstract class AbstractEventNodeWebsocketServerEndpoint extends WsEndpoint {
    private WebsocketServerEventNode localNode;

    private static ScheduledExecutorService scheduler = (ScheduledExecutorService) Executors
	    .newScheduledThreadPool(5, new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
		    Thread t = Executors.defaultThreadFactory().newThread(r);
		    t.setDaemon(true);
		    return t;
		}
	    });

    private long lastReceivedEventTs;

    private long lastSentEventTs;

    private Runnable connectionChecker;

    private ScheduledFuture<?> connectionCheckerFuture;

    protected long connectionCheckPeriod = 35000;

    protected long maxAllowedInactivity = 30000;

    public void onOpen(Session session, EndpointConfig config) {
	setUp(session, config);
	gotoState(CONNECTED);

	connectionCheckerFuture = scheduleTask(connectionChecker, connectionCheckPeriod);
	sendConnectedEvent();

    }

    @OnClose
    public void onClose(Session session) {
	System.out.println("terminating node");
	if (connectionCheckerFuture != null) {
	    connectionCheckerFuture.cancel(true);
	}
	localNode.terminate();
    }

    @OnError
    public void onError(Throwable t) {
	System.err.println("Terminating node " + getLocalNodeId() + " due to error ");
	if (connectionCheckerFuture != null) {
	    connectionCheckerFuture.cancel(true);
	}
	localNode.terminate();
	t.printStackTrace();

    }

    /**
     * Set up this endpoint on open
     * 
     * @param session
     * @param config
     */
    protected void setUp(Session session, EndpointConfig config) {
	this.session = session;
	localNode = buildLocalNode();

	connectionChecker = () -> {
	    long now = System.currentTimeMillis();
	    long inactivityPeriod = now - lastReceivedEventTs;
	    long replyInactivity = now - lastSentEventTs;

	    if (inactivityPeriod >= maxAllowedInactivity || replyInactivity >= maxAllowedInactivity) {
		try {
		    /* send a ping event */
		    sendEvent(new PingEvent());
		} catch (Exception e) {
		    /* doesn't matter */
		}
	    }

	};
    }

    protected Event buildEventFromMessage(String message) {
	return Event.fromJson(message);
    }

    /**
     * Called when an event is received
     * 
     * @param event
     */
    protected void onEvent(Event event) {
	lastReceivedEventTs = System.currentTimeMillis();
	/* by default delegate this to the current state */
	state.handleEvent(event, this);
    }

    protected void sendConnectedEvent() {
	Event event = Events.builder().ebus().peer().connected().build();
	event.addParam(EventParams.clientId, localNode.getId());

	sendEvent(event);
    }

    protected ScheduledFuture<?> scheduleTask(Runnable runnable, long delay) {
	return scheduler.scheduleAtFixedRate(runnable, delay, delay, TimeUnit.MILLISECONDS);
    }

    protected ScheduledFuture<?> scheduleOneTimeTask(Runnable runnable, long delay){
	return scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.eventbus.web.websocket.WsEndpoint#sendEvent(net.segoia.event.eventbus.Event)
     */
    @Override
    public Future<Void> sendEvent(Event event) {
	lastSentEventTs = System.currentTimeMillis();
	return super.sendEvent(event);
    }

    protected abstract WebsocketServerEventNode buildLocalNode();

    @Override
    public String getLocalNodeId() {
	return localNode.getId();
    }
    
    private void gotoState(WsEndpointState newState) {
	System.out.println("ws: "+state+" -> "+newState);
	state=newState;
    }

    @Override
    public void onAccepted() {
	/* first init local node */

	initLocalNode(localNode);
	
	/* then send the accepted event */
	
	gotoState(ACCEPTED);
	sendAuthenticated();
	

    }

    /**
     * This is called once the client websocket connection is accepted </br>
     * Override to initialize the local event node for this connection
     * 
     * @param localNode
     */
    protected abstract void initLocalNode(WebsocketServerEventNode localNode);

    public void sendAuthenticated() {
	Event event = Events.builder().ebus().peer().authenticated().build();
	sendEvent(event);
    }

    /* STATES */

    public static WsServerEndpointState CONNECTED = new WsServerEndpointState("CONNECTED") {

	@Override
	public void handleEvent(Event event, AbstractEventNodeWebsocketServerEndpoint wse) {
	    String et = event.getEt();

	    switch (et) {
	    case "EBUS:PEER:AUTH":
		String clientId = (String) event.getParam(EventParams.clientId);
		if (wse.getLocalNodeId().equals(clientId)) {
		    wse.onAccepted();
		} else {
		    Event error = Events.builder().peer().error().auth().build();
		    error.addParam(EventParams.reason,
			    new AuthError(EventParams.clientId, wse.getLocalNodeId(), clientId));
		    wse.sendEvent(error);
		}
		break;
	    }

	}
    };

    public static WsServerEndpointState ACCEPTED = new WsServerEndpointState("ACCEPTED") {

	@Override
	public void handleEvent(Event event, AbstractEventNodeWebsocketServerEndpoint wse) {
	    boolean process = handleSpecialEvent(event, wse);
	    if (process) {
		wse.localNode.onWsEvent(event);
	    }
	}

	private boolean handleSpecialEvent(Event event, AbstractEventNodeWebsocketServerEndpoint wse) {
	    String et = event.getEt();
	    boolean processFurther = true;
	    switch (et) {
	    case "EBUS:PEER:PING":
		wse.sendEvent(new Event("EBUS:PEER:PONG"));
		processFurther = false;
		break;
	    }

	    return processFurther;
	}
    };

}