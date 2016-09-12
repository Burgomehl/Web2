package com.main.websocket.robot;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.main.messages.Message;
import com.main.messages.Type;
import com.main.messages.forms.Ellipse;
import com.main.messages.forms.FormMessage;
import com.main.messages.forms.FormType;
import com.main.messages.forms.Rectangle;
import com.main.model.History;
import com.main.websocket.WebSocket;

@ClientEndpoint
public class Robot extends Thread {
	public static boolean active = false;
	private int posX = 750, posY = 750;
	private int sizeA = 50, sizeB = 50;
	History history;
	private ObjectMapper objMapper = new ObjectMapper();
	private Session robotSession;

	public Robot(URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(Robot.class, endpointURI);
			history = History.getInstance();
		} catch (DeploymentException | IOException e) {
			e.printStackTrace();
		}
	}

	public void setRobotStop() {
		active = false;
	}

	@Override
	public void run() {
		Random r = new Random();
		while (active) {
			switch (r.nextInt(2)) {
			case 0:
				Rectangle obj = new Rectangle();
				obj.setX(r.nextInt(posX - sizeA));
				obj.setY(r.nextInt(posY - sizeB));
				obj.setA(r.nextInt(sizeA));
				obj.setB(r.nextInt(sizeB));
				send(obj, FormType.RECTANGLE);
				break;
			case 1:
				Ellipse obj1 = new Ellipse();
				obj1.setX(r.nextInt(posX - sizeA));
				obj1.setY(r.nextInt(posY - sizeB));
				int a = r.nextInt(sizeA);
				int b = r.nextInt(sizeB);
				double rad = Math.sqrt((obj1.getX() - a) * (obj1.getX() - a) + (obj1.getY() - b) * (obj1.getY() - b));
				obj1.setRad(rad);
				send(obj1, FormType.ELLIPSE);
				break;
			}
			try {
				wait(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {
			robotSession.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(Rectangle obj, FormType type) {
		JsonNode readTree;
		try {
			readTree = objMapper.readTree(objMapper.writeValueAsString(obj));
			Message message = translateToJson(type, readTree);
			this.robotSession.getAsyncRemote().sendObject(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void send(Ellipse obj, FormType type) {
		JsonNode readTree;
		try {
			readTree = objMapper.readTree(objMapper.writeValueAsString(obj));
			Message message = translateToJson(type, readTree);
			this.robotSession.getAsyncRemote().sendObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Message translateToJson(FormType type, JsonNode readTree)
			throws IOException, JsonProcessingException, JsonGenerationException, JsonMappingException {
		FormMessage formMessage = new FormMessage();
		formMessage.setId(history.getCurrentId());
		formMessage.setContent(readTree);
		formMessage.setType(type);
		readTree = objMapper.readTree(objMapper.writeValueAsString(formMessage));
		Message message = new Message();
		message.setType(Type.HISTORY);
		message.setContent(readTree);
		return message;
	}

	@OnMessage
	public void onMessage(String m) {
		// nothing
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		System.out.println("Robot: Connection opened.");
		robotSession = session;
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Robot: Connection closed.");
	}
}
