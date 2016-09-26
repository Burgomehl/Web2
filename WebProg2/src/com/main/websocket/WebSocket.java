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
import javax.websocket.DeploymentException;
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
import com.main.messages.DeleteAfterBeforeMessage;
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

	private final static String url = "ws://localhost:8080/WebProg2/websocket/robot";
	// private final static String url
	// ="ws://195.37.49.24/sos16_01/websocket/robot";

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
			sendToAllSessions(message);
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
			synchronized (historyHandler.getHistory()) {
				for (String string : readValue.getIds()) {
					historyHandler.deleteHistoryItemsById(Long.valueOf(string));
				}
			}
			sendCleanCanvas();
			resendHistory();
			break;
		case ANIMATE:
			gotAnimateMessage(content);
			break;
		case CLEANCANVAS:
			sendCleanCanvas();
			break;
		case CLEANUP:
			break;
		case DELETEAFTER:
			DeleteAfterBeforeMessage objectsToDeleteAfter = objMapper.readValue(content,
					DeleteAfterBeforeMessage.class);
			synchronized (historyHandler.getHistory()) {
				historyHandler.deleteAfter(objectsToDeleteAfter);
			}
			sendCleanUp();
			resendHistory();
			break;
		case DELETEBEFORE:
			DeleteAfterBeforeMessage objectsToDeleteBefore = objMapper.readValue(content,
					DeleteAfterBeforeMessage.class);
			synchronized (historyHandler.getHistory()) {
				historyHandler.deleteBefore(objectsToDeleteBefore);
			}
			sendCleanUp();
			resendHistory();
			break;
		case LOGEDIN:
			if (historyHandler != null) {
				resendHistory();
			}
			break;
		default:
			break;
		}
	}

	private void gotAnimateMessage(JsonNode content) throws IOException, JsonParseException, JsonMappingException {
		DeleteMessage objectsToAnimate = objMapper.readValue(content, DeleteMessage.class);
		synchronized (historyHandler.getHistory()) {
			historyHandler.animate(objectsToAnimate);
		}
		resendHistory();
	}

	private void gotHistoryMessage(Message message) throws IOException, JsonParseException, JsonMappingException {
		FormMessage histObj = objMapper.readValue(message.getContent(), FormMessage.class);
		histObj.setId(historyHandler.getCurrentId());
		synchronized (historyHandler.getHistory()) {
			historyHandler.addHistory(histObj);
		}
		sendHistoryObject(histObj);
	}

	private void sendCleanUp() {
		Message m = new Message();
		m.setType(Type.CLEANUP);
		sendToAllSessions(m);
	}

	private void sendToAllSessions(Message m) {
		synchronized (WebSocket.session) {
			for (Session sessionToSend : WebSocket.session) {
				sendMessage(m, sessionToSend);
			}
		}
	}

	private void sendCleanCanvas() {
		Message m = new Message();
		m.setType(Type.CLEANCANVAS);
		sendToAllSessions(m);
	}

	private void sendMessage(Message message, Session session) {
		synchronized (session) {
			try {
				session.getBasicRemote().sendObject(message);
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
			}
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		System.out.println("Connection opened.");
		WebSocket.session.add(session);
		adjustRobot();
	}

	public synchronized static void adjustRobot() {
		if ((session.size() >= 1 && session.size() < 3) && robo == null) {
			try {
				robo = new Robot(new URI(url));
			} catch (URISyntaxException | DeploymentException | IOException e) {
				e.printStackTrace();
			}
			robo.start();
		} else if ((session.size() <= 1 || session.size() > 3) && robo != null) {
			robo.setRobotStop();
			robo = null;
		}
	}

	private void resendHistory() {
		synchronized (historyHandler.getHistory()) {
			for (FormMessage hist : historyHandler.getHistory()) {
				sendHistoryObject(hist);
			}
		}
	}

	private void sendHistoryObject(FormMessage hist) {
		JsonNode readTree;
		readTree = objMapper.valueToTree(hist);
		Message message = new Message();
		message.setContent(readTree);
		message.setType(Type.HISTORY);
		sendToAllSessions(message);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Connection closed.");
		WebSocket.session.remove(session);
		adjustRobot();
	}
}
