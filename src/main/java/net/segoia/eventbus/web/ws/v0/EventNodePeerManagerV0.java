package net.segoia.eventbus.web.ws.v0;

import net.segoia.event.eventbus.Event;
import net.segoia.event.eventbus.constants.EventParams;
import net.segoia.event.eventbus.constants.Events;
import net.segoia.event.eventbus.peers.EventTransceiver;
import net.segoia.event.eventbus.peers.PeerContext;
import net.segoia.event.eventbus.peers.PeerEventContext;
import net.segoia.event.eventbus.peers.PeerManager;
import net.segoia.event.eventbus.peers.manager.states.PeerManagerState;
import net.segoia.eventbus.web.websocket.server.AuthError;

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

    public static PeerManagerState CONNECTED = new PeerManagerState() {

	@Override
	protected void registerPeerEventHandlers() {
	    registerPeerEventProcessor("EBUS:PEER:AUTH", (c) -> {
		Event event = c.getEvent();
		PeerManager pm = c.getPeerManager();
		String clientId = (String) event.getParam(EventParams.clientId);
		if (pm.getPeerId().equals(clientId)) {
		    pm.onReady();
		} else {
		    Event error = Events.builder().peer().error().auth().build();
		    error.addParam(EventParams.reason, new AuthError(EventParams.clientId, pm.getPeerId(), clientId));
		    pm.forwardToPeer(error);
		    pm.terminate();
		}
		event.setHandled();
	    });
	    
	    registerPeerEventProcessor((c)->{
		if(!c.getEvent().isHandled()) {
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
	    Event event = Events.builder().ebus().peer().connected().build();
	    event.addParam(EventParams.clientId, peerManager.getPeerId());

	    peerManager.forwardToPeer(event);
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

}
