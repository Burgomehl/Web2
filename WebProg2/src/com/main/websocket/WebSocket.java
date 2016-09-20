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

public class WebSocket {
	private ObjectMapper objMapper = new ObjectMapper();
	private HistoryHandler historyHandler;
	private static Set<Session> session = Collections.synchronizedSet(new HashSet<>());
	private static Robot robo;

	public WebSocket() {
		historyHandler = HistoryHandler.getInstance();
	}

	@OnMessage
	public void onMessage(Message message, Session session)
			throws JsonParseException, JsonMappingException, IOException {

		JsonNode content = message.getContent();
		switch (message.getType()) {
		case TEXT:
			System.out.println("User input: " + content);
			synchronized (this.session) {
				for (Session sessions : this.session) {
					sendMessage(message, sessions);
				}
			}
			break;
		case HISTORY:
			gotHistoryMessage(message);
			break;
		case DELETE:
			historyHandler.deleteHistory();
			sendCleanUp();
			resendHistory();
			break;
		case DELETEBYID:
			DeleteMessage readValue = objMapper.readValue(content, DeleteMessage.class);
			for (String string : readValue.getIds()) {
				historyHandler.deleteHistoryItemsById(Long.valueOf(string));
			}
			sendCleanUp();
			resendHistory();
			break;
		case ANIMATE:
			gotAnimateMessage(content);
			break;
		case CLEANCANVAS:
			sendCleanCanvas();
			break;
		default:
			System.out.println("hm");
			break;
		}
	}

	private void gotAnimateMessage(JsonNode content) throws IOException, JsonParseException, JsonMappingException {
		DeleteMessage objectsToAnimate = objMapper.readValue(content, DeleteMessage.class);
		historyHandler.animate(objectsToAnimate);
		resendHistory();
	}

	private void gotHistoryMessage(Message message) throws IOException, JsonParseException, JsonMappingException {
		FormMessage histObj = objMapper.readValue(message.getContent(), FormMessage.class);
		histObj.setId(historyHandler.getCurrentId());
		historyHandler.addHistory(histObj);
		synchronized (this.session) {
			for (Session sessions : this.session) {
				sendHistoryObject(sessions, histObj);
			}
		}
	}

	private void sendCleanUp() {
		Message m = new Message();
		m.setType(Type.CLEANUP);
		synchronized (this.session) {
			for (Session sessionToSend : this.session) {
				sendMessage(m, sessionToSend);
			}
		}
	}

	private void sendCleanCanvas() {
		Message m = new Message();
		m.setType(Type.CLEANCANVAS);
		synchronized (this.session) {
			for (Session sessionToSend : this.session) {
				sendMessage(m, sessionToSend);
			}
		}
	}

	private void sendMessage(Message message, Session sessions) {
		synchronized (sessions) {
			try {
				sessions.getBasicRemote().sendObject(message);
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		this.session.add(session);
		if (historyHandler != null) {
			resendHistory();
		}
		adjustRobot();
		System.out.println("Connection opened.");
	}

	public synchronized static void adjustRobot() {
		if ((session.size() >= 1 && session.size() < 3) && robo == null) {
			try {
				robo = new Robot(new URI("ws://localhost:8080/WebProg2/websocket/robot"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			robo.start();
		} else if ((session.size() < 1 || session.size() > 3) && robo != null) {
			if (robo != null) {
				robo.setRobotStop();
				robo = null;
			}
		}
	}

	private void resendHistory() {
		synchronized (this.session) {
			for (Session sessions : this.session) {
				synchronized (historyHandler.getHistory()) {
					for (FormMessage hist : historyHandler.getHistory()) {
						sendHistoryObject(sessions, hist);
					}
				}
			}
		}
	}

	private void sendHistoryObject(Session sessions, FormMessage hist) {
		JsonNode readTree;
		readTree = objMapper.valueToTree(hist);
		Message message = new Message();
		message.setContent(readTree);
		message.setType(Type.HISTORY);
		sendMessage(message, sessions);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		this.session.remove(session);
		adjustRobot();
		System.out.println("Connection closed.");
	}
}
