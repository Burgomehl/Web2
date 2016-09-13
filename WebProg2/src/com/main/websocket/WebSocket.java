package com.main.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.main.coder.Decoder;
import com.main.coder.Encoder;
import com.main.messages.DeleteMessage;
import com.main.messages.Message;
import com.main.messages.Type;
import com.main.messages.forms.FormMessage;
import com.main.model.History;
import com.main.websocket.robot.Robot;

@ServerEndpoint(value = "/websocket", encoders = { Encoder.class }, decoders = { Decoder.class })
public class WebSocket {
	private ObjectMapper objMapper = new ObjectMapper();
	private History history;
	private static Set<Session> session = Collections.synchronizedSet(new HashSet<>());
	private Robot robo;

	public WebSocket() {
		history = History.getInstance();
	}

	@OnMessage
	public void onMessage(Message message, Session session)
			throws JsonParseException, JsonMappingException, IOException {

		JsonNode content = message.getContent();
		switch (message.getType()) {
		case TEXT:
			System.out.println("User input: " + content);
			for (Session sessions : this.session) {
				sendMessage(message, sessions);
			}
			break;
		case HISTORY:
			FormMessage histObj = objMapper.readValue(message.getContent(), FormMessage.class);
			histObj.setId(history.getCurrentId());
			history.addHistory(histObj);
			System.out.println(content);
			for (Session sessions : this.session) {
				sendHistoryObject(sessions, histObj);
			}
			break;
		case DELETE:
			history.deleteHistory();
			sendCleanUp();
			resendHistory();
			break;
		case DELETEBYID:
			DeleteMessage readValue = objMapper.readValue(content, DeleteMessage.class);
			for (String string : readValue.getIds()) {
				history.deleteHistoryItemsById(Long.valueOf(string));
			}
			sendCleanUp();
			resendHistory();
			break;
		default:
			System.out.println("hm");
			break;
		}
	}

	private void sendCleanUp() {
		Message m = new Message();
		m.setType(Type.CLEANUP);
		for (Session sessionToSend : this.session) {
			sendMessage(m, sessionToSend);
		}
	}

	private void sendMessage(Message message, Session sessions) {
		try {
			sessions.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException e) {
			e.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		this.session.add(session);
		if (history != null) {
			resendHistory();
		}
		adjustRobot();
		System.out.println("Connection opened.");
	}

	public synchronized void adjustRobot() {
		return; //TODO: geht noch nicht.
//		if (session.size() == 1 && session.size() < 3) {
//			try {
//				robo = new Robot(new URI("ws://localhost:8080/WebProg2/websocket"));
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			}
//			robo.start();
//		} else {
//			if (robo != null) {
//				robo.setRobotStop();
//			}
//		}

	}

	private void resendHistory() {
		for (Session sessions : this.session) {
			for (FormMessage hist : history.getHistory()) {
				sendHistoryObject(sessions, hist);
			}
		}
	}

	private void sendHistoryObject(Session sessions, FormMessage hist) {
		JsonNode readTree;
		try {
			readTree = objMapper.readTree(objMapper.writeValueAsString(hist));
			Message message = new Message();
			message.setContent(readTree);
			message.setType(Type.HISTORY);
			sendMessage(message, sessions);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		this.session.remove(session);
		adjustRobot();
		System.out.println("Connection closed.");
	}
}
