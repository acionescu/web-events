package net.segoia.eventbus.web.ws.v0;

import net.segoia.event.eventbus.peers.events.bind.PeerBindRequest;

public class WsServerEndpointTransceiver extends EventNodeWsEndpointTransceiver{

    @Override
    protected void init() {
	super.init();
	/* for server endpoint init is start */
	start();
    }

    @Override
    public void start() {
	getEventNode().registerPeer(new PeerBindRequest(this));
    }

    @Override
    protected void handleError(Throwable t) {
	t.printStackTrace();
    }

    @Override
    protected void handleSendError(Throwable t) {
	t.printStackTrace();
    }

}
