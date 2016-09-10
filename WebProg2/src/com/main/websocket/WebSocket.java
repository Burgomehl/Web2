package com.main.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.CloseReason;
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



@ServerEndpoint(value="/websocket", encoders = {Encoder.class}, decoders = {Decoder.class})
public class WebSocket {
	private History history;
	private static Set <Session> session = Collections.synchronizedSet(new HashSet<>());

	@OnMessage
	public void onMessage(Message message, Session session) throws IOException, InterruptedException {
		
		switch (message.getType()) {
		case TEXT:
			System.out.println("User input: " + message.getContent());
			session.getBasicRemote().sendText("Hello World Mr. " + message.getContent());

			session.getBasicRemote().sendText("Hello "+ message.getContent() +" to the World of this web");
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
		history = new History();
		System.out.println("Connection opened.");
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		this.session.remove(session);
		System.out.println("Connection closed.");
	}
}
