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

import net.segoia.event.eventbus.PeerBindRequest;
import net.segoia.util.logging.Logger;
import net.segoia.util.logging.MasterLogManager;

public class WsServerEndpointTransceiver extends EventNodeWsEndpointTransceiver{
    private static Logger logger = MasterLogManager.getLogger(WsServerEndpointTransceiver.class.getSimpleName());
    @Override
    protected void init() {
	super.init();
	/* for the server endpoint init is start */
	start();
    }

    @Override
    public void start() {
	getEventNode().registerPeer(new PeerBindRequest(this));
    }

    @Override
    protected void handleError(Throwable t) {
	logger.error("WS error: "+t.getMessage(),t);
//	t.printStackTrace();
    }

    @Override
    protected void handleSendError(Throwable t) {
	logger.error("WS send error: "+t.getMessage(),t);
//	t.printStackTrace();
    }

}
