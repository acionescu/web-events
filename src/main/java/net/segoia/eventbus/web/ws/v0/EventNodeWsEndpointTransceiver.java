package net.segoia.eventbus.web.ws.v0;

import net.segoia.event.eventbus.peers.EventNode;

public abstract class EventNodeWsEndpointTransceiver extends WsEndpointTransceiver{
    private EventNode eventNode;
    

    public EventNode getEventNode() {
        return eventNode;
    }

    public void setEventNode(EventNode eventNode) {
        this.eventNode = eventNode;
    }
    
}
