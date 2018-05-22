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

import java.net.URI;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

public class WsClientEndpointTransceiver extends WsEndpointTransceiver {
    private boolean autoReconnect = true;

    /**
     * Wait this amount of time between reconnect attempts
     */
    private long reconnectDelay = 60000;

    private URI uri;

    public WsClientEndpointTransceiver(URI uri) {
	super();
	this.uri = uri;
    }

    @Override
    public void start() {
	doConnect();
    }

    @Override
    protected void handleError(Throwable t) {
	// TODO Auto-generated method stub

    }

    @Override
    protected void handleSendError(Throwable t) {
	// TODO Auto-generated method stub

    }

    protected void doConnect() {
	WebSocketContainer wsContainer = ContainerProvider.getWebSocketContainer();
	try {
	    wsContainer.connectToServer(this, uri);
	} catch (Exception e) {
	    handleError(e);

//	    if (autoReconnect) {
//		scheduleTask(new Callable<Void>() {
//
//		    @Override
//		    public Void call() throws Exception {
//			System.out.println(getLocalNodeId() + " trying to reconnect to " + uri);
//			doConnect();
//			return null;
//		    }
//		}, reconnectDelay);
//	    }
	}
    }

    public boolean isAutoReconnect() {
	return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
	this.autoReconnect = autoReconnect;
    }

    public long getReconnectDelay() {
	return reconnectDelay;
    }

    public void setReconnectDelay(long reconnectDelay) {
	this.reconnectDelay = reconnectDelay;
    }

    public URI getUri() {
	return uri;
    }

    public void setUri(URI uri) {
	this.uri = uri;
    }

}
