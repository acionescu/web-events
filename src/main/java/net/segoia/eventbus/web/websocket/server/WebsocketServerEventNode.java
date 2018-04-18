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

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import net.segoia.event.conditions.TrueCondition;
import net.segoia.event.eventbus.AsyncEventTracker;
import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.EventContext;
import net.segoia.event.eventbus.EventTracker;
import net.segoia.event.eventbus.FilteringEventBus;
import net.segoia.event.eventbus.BlockingEventDispatcher;
import net.segoia.event.eventbus.peers.AgentNode;
import net.segoia.event.eventbus.peers.DefaultEventRelay;
import net.segoia.event.eventbus.peers.EventRelay;

public abstract class WebsocketServerEventNode extends AgentNode {

    private AbstractEventNodeWebsocketServerEndpoint ws;
    

    /**
     * Keep a separate bus to handle events coming from the ws client
     */
    protected FilteringEventBus wsEventsBus;

    public WebsocketServerEventNode(AbstractEventNodeWebsocketServerEndpoint ws) {
	/* we don't want the agent to autoinitialize, we will do it */
	super(false);
	this.ws = ws;
    }

    @Override
    protected void nodeConfig() {
	config.setAutoRelayEnabled(true);
	config.setDefaultRequestedEvents(new TrueCondition());

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.event.eventbus.peers.AgentNode#nodeInit()
     */
    @Override
    protected void nodeInit() {
	super.nodeInit();

	wsEventsBus = spawnAdditionalBus(new WebsocketServerNodeDispatcher());
	wsEventsBus.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.segoia.event.eventbus.peers.EventNode#registerHandlers()
     */
    @Override
    protected void registerHandlers() {
	super.registerHandlers();
	/* register a generic handler that will send all received events to the websocket endpoint */
	addEventHandler((c) -> this.handleServerEvent(c.getEvent()),9999);
    }

    /**
     * Called on events coming from the server
     * 
     * @param event
     * @return
     */
    protected EventTracker handleServerEvent(Event event) {
	Future<Void> future = ws.sendEvent(event);
	return new AsyncEventTracker(future, true);
    }
    
    protected ScheduledFuture<?> scheduleTask(Runnable runnable, long delay) {
	return ws.scheduleTask(runnable, delay);
    }
    
    protected ScheduledFuture<?> scheduleOneTimeTask(Runnable runnable, long delay){
	return ws.scheduleOneTimeTask(runnable, delay);
    }

    @Override
    protected EventRelay buildLocalRelay(String peerId) {
	return new DefaultEventRelay(peerId, this);
    }

    public void onWsEvent(Event event) {
	wsEventsBus.postEvent(event);
    }

    /**
     * Called on events coming from the ws client
     * 
     * @param event
     */
    protected void handleWsEvent(Event event) {
	forwardToAll(event);
    }

    @Override
    public void cleanUp() {
	ws.terminate();
    }

    class WebsocketServerNodeDispatcher extends BlockingEventDispatcher {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.segoia.event.eventbus.SimpleEventDispatcher#dispatchEvent(net.segoia.event.eventbus.EventContext)
	 */
	@Override
	public boolean dispatchEvent(EventContext ec) {
	    Event event = ec.event();
	    /* make sure the client can't inject relays */
	    event.clearRelays();
	    
	    /* process whatever handlers we have for this event */
	    super.dispatchEvent(ec);
	    
	    /* do a final handling of the event */
	    handleWsEvent(ec.getEvent());
	    return true;
	}

    }
}
