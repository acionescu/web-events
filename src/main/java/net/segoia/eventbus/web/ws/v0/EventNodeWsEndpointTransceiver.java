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

import net.segoia.event.eventbus.peers.EventNode;

public abstract class EventNodeWsEndpointTransceiver extends WsEndpointTransceiver{
    private EventNode eventNode;
    

    public EventNode getEventNode() {
        return eventNode;
    }

    public void setEventNode(EventNode eventNode) {
        this.eventNode = eventNode;
    }
    
    public String getLocalNodeId() {
	if(eventNode != null) {
	    return eventNode.getId();
	}
	return null;
    }
}
