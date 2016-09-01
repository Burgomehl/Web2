package com.main.websocket;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
public class WebSocket {

	@OnMessage
	public void onMessage(String message, Session session) throws IOException, InterruptedException {
		System.out.println("User input: " + message);
		session.getBasicRemote().sendText("Hello World Mr. " + message);

		// sending message to client each 1 second
		for (int i = 0; i <= 25; ++i) {
			session.getBasicRemote().sendText(i + " Message from server.");
			Thread.sleep(1000);
		}
	}

	@OnOpen
	public void onOpen() {
		System.out.println("Connection opened.");
	}

	@OnClose
	public void onClose() {
		System.out.println("Connection closed.");
	}
}
