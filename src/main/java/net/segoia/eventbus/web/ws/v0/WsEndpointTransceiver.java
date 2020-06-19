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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import net.segoia.event.eventbus.peers.core.AbstractEventTransceiver;
import net.segoia.event.eventbus.peers.events.ClosePeerEvent;
import net.segoia.event.eventbus.peers.events.bind.PeerBindRejectedEvent;
import net.segoia.event.eventbus.peers.vo.ClosePeerData;
import net.segoia.event.eventbus.peers.vo.PeerLeavingReason;
import net.segoia.util.logging.Logger;
import net.segoia.util.logging.MasterLogManager;

public abstract class WsEndpointTransceiver extends AbstractEventTransceiver {
    private static Logger logger = MasterLogManager.getLogger(WsEndpointTransceiver.class.getSimpleName());
    private static Map<String, CloseReason.CloseCode> closeEventsCodes=new HashMap<>();

    /**
     * We will use a fixed thread pool to send events to all websocket connected peers
     */
    private static ScheduledExecutorService sendThreadPool = Executors.newScheduledThreadPool(30, new ThreadFactory() {

	@Override
	public Thread newThread(Runnable r) {
	    Thread th = new Thread(r);
	    th.setDaemon(true);
	    return th;
	}
    });
    
    
    static {
	closeEventsCodes.put(PeerBindRejectedEvent.ET, CloseReason.CloseCodes.CANNOT_ACCEPT);
    }

    protected Session session;
    protected EndpointConfig config;
    protected Basic basicRemote;
    

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
	this.session = session;
	this.config = config;
	init();
    }

    @OnMessage
    public void onMessage(byte[] bytes ) {
//	byte[] data = bytes.array();
	receiveData(bytes);
    }

    @OnClose
    public void onClose(CloseReason reason) {
	logger.info(this+" closing ws "+reason.getReasonPhrase());
	onPeerLeaving(new PeerLeavingReason(reason.getCloseCode().getCode(), reason.getReasonPhrase()));
    }
    
    @OnError
    public void onError(Throwable t) {
	handleError(t);
    }
    

    @Override
    public void terminate() {
	if (!session.isOpen()) {
	    return;
	}

	try {
	    session.close();
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }
    
    private void terminate(CloseReason closeReason) {
	if(closeReason == null) {
	    terminate();
	    return;
	}
	if (!session.isOpen()) {
	    return;
	}

	try {
	    session.close(closeReason);
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void terminate(ClosePeerEvent closeEvent) {
	if(closeEvent != null) {
	    CloseCode closeCode = closeEventsCodes.get(closeEvent.getEt());
	    if(closeCode != null) {
		ClosePeerData data = closeEvent.getData();
		if(data != null) {
		    String message = data.getMessage();
		    terminate(new CloseReason(closeCode, message));
		    return;
		}
		
	    }
	}
	terminate();
    }

    @Override
    protected void init() {
	session.setMaxBinaryMessageBufferSize(128000);
	basicRemote = session.getBasicRemote();
	
    }
    
    protected abstract void handleError(Throwable t);

    protected abstract void handleSendError(Throwable t);

    @Override
    public void sendData(byte[] data) {
	final WsEndpointTransceiver endpoint = this;
	sendThreadPool.submit(new Callable<Void>() {

	    @Override
	    public Void call() throws Exception {
		synchronized (session) {
		    try {
			basicRemote.sendBinary(ByteBuffer.wrap(data));
		    } catch (IOException e) {
			e.printStackTrace();
			endpoint.handleSendError(e);
		    }
		}
		return null;
	    }

	});
    }

    @Override
    public String getChannel() {
	try {
	    return session.getRequestURI().toURL().getProtocol();
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;
    }
    
    protected Future<Void> submitTask(Callable<Void> task){
	return sendThreadPool.submit(task);
    }
    
    protected Future<Void> scheduleTask(Callable<Void> task, long delay){
	return sendThreadPool.schedule(task, delay, TimeUnit.MILLISECONDS);
    }


}
