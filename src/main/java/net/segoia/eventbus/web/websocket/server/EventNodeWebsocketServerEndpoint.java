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

import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import net.segoia.event.eventbus.Event;

public abstract class EventNodeWebsocketServerEndpoint extends AbstractEventNodeWebsocketServerEndpoint{
    
    
    
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
	super.onOpen(session, config);
    }

    @Override
    protected WebsocketServerEventNode buildLocalNode() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected void initLocalNode(WebsocketServerEventNode localNode) {
	// TODO Auto-generated method stub
	
    }

    @OnMessage
    public void onMessage(String message) {
	try {
	    Event event = buildEventFromMessage(message);
	    onEvent(event);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
