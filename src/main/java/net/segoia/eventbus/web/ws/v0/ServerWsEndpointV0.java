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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpSession;
import javax.websocket.OnMessage;

import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.EventContext;
import net.segoia.event.eventbus.PeerBindRequest;
import net.segoia.event.eventbus.peers.core.PeerDataEvent;
import net.segoia.event.eventbus.peers.events.bind.PeerBindRequestEvent;
import net.segoia.event.eventbus.peers.vo.PeerData;
import net.segoia.eventbus.events.web.util.WebEventsUtil;
import net.segoia.eventbus.stats.SimpleStats;
import net.segoia.util.logging.Logger;
import net.segoia.util.logging.MasterLogManager;

public abstract class ServerWsEndpointV0 extends WsServerEndpointTransceiver {
    private static Logger logger = MasterLogManager.getLogger(ServerWsEndpointV0.class.getSimpleName());
    /**
     * Keep a reference to the http session as well to extract client info
     */
    private HttpSession httpSession;

    private Event rootEvent;

    private SimpleStats stats = new SimpleStats();

    private long maxAllowedActivity = 50;

    @Override
    protected void init() {
	initEventNode();
	/* get the http session reference */
	this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

	super.init();
	/* increase maximum message buffer */
//	session.setMaxBinaryMessageBufferSize(512000);
//	session.setMaxTextMessageBufferSize(512000);
	System.out.println("initialized server endpoint with buffer size "+session.getMaxBinaryMessageBufferSize());
    }

    @Override
    public void start() {
	PeerBindRequest bindRequest = new PeerBindRequest(this);
	PeerBindRequestEvent bindRequestEvent = new PeerBindRequestEvent(bindRequest);
	getRootEvent().setAsCauseFor(bindRequestEvent);
	getEventNode().registerPeer(bindRequestEvent);
    }

    protected abstract void initEventNode();

    @Override
    public String getChannel() {
	return "WSS_V0";
    }

    @Override
    public void sendData(byte[] data) {
//	System.out.println("Sending "+new String(data));
	super.sendData(data);
    }

    @Override
    public void receiveData(byte[] data) {
//	System.out.println("Receiving "+new String(data));
	PeerDataEvent dataEvent = new PeerDataEvent(new PeerData(data));
	Event re = getRootEvent();
	if (re != null) {
	    re.setAsCauseFor(dataEvent);
	}

	receiveData(dataEvent);
    }

    @Override
    public void receiveData(PeerDataEvent dataEvent) {
	/* make sure we're not exceeding allowed activity level */
	stats.onEvent(new EventContext(dataEvent));
	float activityIndex = stats.getActivityIndex();
//	logger.debug("Activity index: " + activityIndex);
	if (activityIndex > maxAllowedActivity) {
	    logger.warn(this + " terminating due to exceeded activity threshold: "+activityIndex);
	    terminate();
	    return;
	}
	super.receiveData(dataEvent);
    }

    @OnMessage
    public void onMessage(String string) {
	try {
	    receiveData(string.getBytes("UTF-8"));
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private Event getRootEvent() {
	if (this.rootEvent == null) {
	    this.rootEvent = WebEventsUtil.getRootEvent(httpSession);
	}
	return this.rootEvent;
    }

    public HttpSession getHttpSession() {
	return httpSession;
    }

    public long getMaxAllowedActivity() {
	return maxAllowedActivity;
    }

    public void setMaxAllowedActivity(long maxAllowedActivity) {
	this.maxAllowedActivity = maxAllowedActivity;
    }

}
