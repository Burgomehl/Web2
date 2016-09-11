package com.main.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.main.coder.Decoder;
import com.main.coder.Encoder;
import com.main.messages.Message;
import com.main.model.History;

@ServerEndpoint(value = "/websocket", encoders = { Encoder.class }, decoders = { Decoder.class })
public class WebSocket {
	private History history;
	private static Set<Session> session = Collections.synchronizedSet(new HashSet<>());

	public WebSocket() {
		history = History.getInstance();
	}
	
	@OnMessage
	public void onMessage(Message message, Session session) throws IOException, InterruptedException {

		switch (message.getType()) {
		case TEXT:
			System.out.println("User input: " + message.getContent());
			for (Session sessions : this.session) {
				try {
					sessions.getBasicRemote().sendObject(message);
				} catch (EncodeException e) {
					e.printStackTrace();
				}
				
			}
			break;
		case HISTORY:
			history.addHistory(message);
			System.out.println(message.getContent());
			break;
		default:
			System.out.println("hm");
			break;
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		this.session.add(session);
		if (history != null) {
			for (Session sessions : this.session) {
				for (Message hist : history.getHistory()) {
					try {
						sessions.getBasicRemote().sendObject(hist);
					} catch (IOException | EncodeException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Connection opened.");
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		this.session.remove(session);
		System.out.println("Connection closed.");
	}
}
