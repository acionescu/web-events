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

import java.util.ArrayList;
import java.util.List;

import net.segoia.event.conditions.Condition;
import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.constants.ErrorEvents;
import net.segoia.event.eventbus.constants.EventConstants;
import net.segoia.event.eventbus.constants.EventParams;
import net.segoia.event.eventbus.constants.Events;
import net.segoia.event.eventbus.peers.PeerContext;
import net.segoia.event.eventbus.peers.PeerEventContext;
import net.segoia.event.eventbus.peers.PeerManager;
import net.segoia.event.eventbus.peers.core.EventTransceiver;
import net.segoia.event.eventbus.peers.manager.states.PeerManagerState;
import net.segoia.event.eventbus.vo.security.SessionAuthContext;
import net.segoia.eventbus.web.websocket.server.AuthError;
import net.segoia.eventbus.web.websocket.server.AuthRequiredData;

public class EventNodePeerManagerV0 extends PeerManager {

    public EventNodePeerManagerV0(String peerId, EventTransceiver transceiver) {
	super(peerId, transceiver);
    }

    public EventNodePeerManagerV0(PeerContext peerContext) {
	super(peerContext);
    }

    @Override
    protected void startInServerMode() {
	setAcceptedState(ACCEPTED);
	goToState(CONNECTED);
    }

    protected void startInClientMode() {
	setAcceptedState(CLIENT_ACCEPTED);
	goToState(CLIENT_CONNECT);
    }

    public static PeerManagerState CONNECTED = new PeerManagerState() {

	@Override
	protected void registerPeerEventHandlers() {
	    registerPeerEventProcessor("EBUS:PEER:AUTH", (c) -> {
		Event event = c.getEvent();
		PeerManager pm = c.getPeerManager();
		String clientId = (String) event.getParam(EventParams.clientId);

		Event error = null;

		if (!pm.getPeerId().equals(clientId)) {
		    error = Events.builder().peer().error().auth().build();
		    error.addParam(EventParams.reason, new AuthError(EventParams.clientId, pm.getPeerId(), clientId));

		}

		Condition peerAuthCondition = pm.getConfig().getPeerAuthCondition();
		if (peerAuthCondition != null) {

		    Event rootEvent = event.getHeader().getRootEvent();

		    if (rootEvent == null) {
			throw new RuntimeException("Auth condition specified, but root event is missing");
		    }

		    /* see if a session auth context is presetn */

		    SessionAuthContext sessionAuthContext = (SessionAuthContext) rootEvent
			    .getHeaderParam(EventConstants.AUTH_SESSION);

		    if (sessionAuthContext == null || !sessionAuthContext.isVerified(peerAuthCondition)) {

			if (!peerAuthCondition.test(c)) {
			    /* if an auth condition is specified and it fails, send an auth error */
			    error = Events.builder().peer().error().auth().build();
			    error.addParam(EventParams.reason, ErrorEvents.SIMPLE_AUTH_ERROR);

			} else {
			    /* auth successful, add an auth session context */

			    if (sessionAuthContext == null) {
				/* create a session auth context if missing */
				sessionAuthContext = new SessionAuthContext();
				/* add the context as root event header param */
				rootEvent.addHeaderParam(EventConstants.AUTH_SESSION, sessionAuthContext);
			    }
			    /* add this condition on the context */
			    sessionAuthContext.addAuthCondition(peerAuthCondition);

			}

		    }
		}

		if (error != null) {
		    pm.forwardToPeer(error);
		    pm.terminate();
		} else {
		    pm.onReady();
		}

		event.setHandled();
	    });

	    registerPeerEventProcessor((c) -> {
		if (!c.getEvent().isHandled()) {
		    /* if the user sends an arbitrary unhandled event, then terminate the connection */
		    c.getPeerManager().terminate();
		}
	    });
	}

	@Override
	protected void registerLocalEventHandlers() {
	    // TODO Auto-generated method stub

	}

	@Override
	public void onExitState(PeerManager peerManager) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void onEnterState(PeerManager peerManager) {
	    Event connectedEvent = Events.builder().ebus().peer().connected().build();
	    connectedEvent.addParam(EventParams.clientId, peerManager.getPeerId());

	    Condition peerAuthCondition = peerManager.getConfig().getPeerAuthCondition();
	    
	    if (peerAuthCondition != null) {
		Event rootEvent = peerManager.getPeerContext().getCauseEvent().getHeader().getRootEvent();
		SessionAuthContext authSessionContext = (SessionAuthContext) rootEvent
			.getHeaderParam(EventConstants.AUTH_SESSION);
		if (authSessionContext == null || !authSessionContext.isVerified(peerAuthCondition)) {
		    /* add an auth required param */
		    List<String> requiredParamsNames = new ArrayList<>(peerAuthCondition.getRequiredParamsNames());
		    connectedEvent.addParam(EventParams.authRequired, new AuthRequiredData(requiredParamsNames));
		}
		else {
		    if(authSessionContext != null) {
			System.out.println("session auth is already verified on root event "+rootEvent);
		    }
		}
	    }

	    peerManager.forwardToPeer(connectedEvent);
	}
    };

    public static PeerManagerState ACCEPTED = new PeerManagerState() {

	@Override
	public <E extends Event> boolean handleEventFromPeer(PeerEventContext<E> ec) {
	    ec.getPeerManager().postEvent(ec.getEvent());
	    return true;
	}

	@Override
	protected void registerPeerEventHandlers() {
	    // TODO Auto-generated method stub

	}

	@Override
	protected void registerLocalEventHandlers() {

	}

	@Override
	public void onExitState(PeerManager peerManager) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void onEnterState(PeerManager peerManager) {
	    Event event = Events.builder().ebus().peer().authenticated().build();
	    peerManager.forwardToPeer(event);

	}
    };

    /* client */

    public static PeerManagerState CLIENT_CONNECT = new PeerManagerState() {

	@Override
	public void onEnterState(PeerManager peerManager) {
	    peerManager.getPeerContext().getRelay().start();
	}

	@Override
	public void onExitState(PeerManager peerManager) {
	    // TODO Auto-generated method stub

	}

	@Override
	protected void registerLocalEventHandlers() {
	    // TODO Auto-generated method stub

	}

	@Override
	protected void registerPeerEventHandlers() {
	    registerPeerEventProcessor("EBUS:PEER:CONNECTED", (c) -> {
		Event event = c.getEvent();
		PeerManager peerManager = c.getPeerManager();
		Event authReq = Events.builder().ebus().peer().auth().build();
		authReq.addParam(EventParams.clientId, event.getParam(EventParams.clientId));

		peerManager.goToState(CLIENT_CONNECTED);
		peerManager.forwardToPeer(authReq);
		event.setHandled();

	    });

	}

    };

    public static PeerManagerState CLIENT_CONNECTED = new PeerManagerState() {

	@Override
	public void onEnterState(PeerManager peerManager) {

	}

	@Override
	public void onExitState(PeerManager peerManager) {
	    // TODO Auto-generated method stub

	}

	@Override
	protected void registerLocalEventHandlers() {
	    // TODO Auto-generated method stub

	}

	@Override
	protected void registerPeerEventHandlers() {
	    registerPeerEventProcessor("EBUS:PEER:AUTHENTICATED", (c) -> {
		PeerManager pm = c.getPeerManager();
		pm.onReady();
		c.getEvent().setHandled();
	    });
	}
    };

    public static PeerManagerState CLIENT_ACCEPTED = new PeerManagerState() {

	@Override
	public <E extends Event> boolean handleEventFromPeer(PeerEventContext<E> ec) {
	    ec.getPeerManager().postEvent(ec.getEvent());
	    return true;
	}

	@Override
	protected void registerPeerEventHandlers() {
	    // TODO Auto-generated method stub

	}

	@Override
	protected void registerLocalEventHandlers() {

	}

	@Override
	public void onExitState(PeerManager peerManager) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void onEnterState(PeerManager peerManager) {

	}
    };
}
