package net.segoia.eventbus.web.ws.v0;

import net.segoia.event.eventbus.peers.PeerContext;
import net.segoia.event.eventbus.peers.PeerManager;
import net.segoia.event.eventbus.peers.PeerManagerFactory;

public class PeerManagerV0Factory implements PeerManagerFactory{

    @Override
    public PeerManager buidPeerManager(PeerContext peerContext) {
	return  new EventNodePeerManagerV0(peerContext);
    }

}
