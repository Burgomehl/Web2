package com.main.websocket.robot;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
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

import com.main.coder.Decoder;
import com.main.coder.Encoder;
import com.main.messages.Message;
import com.main.messages.Type;
import com.main.messages.forms.Ellipse;
import com.main.messages.forms.FormMessage;
import com.main.messages.forms.FormType;
import com.main.messages.forms.Line;
import com.main.messages.forms.Polygon;
import com.main.messages.forms.Rectangle;
import com.main.messages.forms.Snake;
import com.main.websocket.HistoryHandler;

@ClientEndpoint(encoders = { Encoder.class }, decoders = { Decoder.class })
public class Robot extends Thread {
	public static boolean active = false;
	private int posX = 750, posY = 750;
	private int sizeA = 50, sizeB = 50;
	HistoryHandler historyHandler;
	private ObjectMapper objMapper = new ObjectMapper();
	private Session robotSession;

	public Robot(URI endpointURI) throws DeploymentException, IOException {

		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.connectToServer(this, endpointURI);
		historyHandler = HistoryHandler.getInstance();
	}

	public void setRobotStop() {
		active = false;
	}

	@Override
	public void run() {
		synchronized (this) {
			String color = "#ff0000";
			Random r = new Random();
			while (active) {
				switch (r.nextInt(5)) {
				case 0:
					Rectangle obj = new Rectangle();
					obj.setX(r.nextInt(posX - sizeA));
					obj.setY(r.nextInt(posY - sizeB));
					obj.setA(r.nextInt(sizeA));
					obj.setB(r.nextInt(sizeB));
					obj.setColor(color);
					send(Rectangle.class, obj, FormType.RECTANGLE);
					break;
				case 1:
					Ellipse obj1 = new Ellipse();
					obj1.setX(r.nextInt(posX - sizeA));
					obj1.setY(r.nextInt(posY - sizeB));
					obj1.setColor(color);
					int a = r.nextInt(sizeA);
					int b = r.nextInt(sizeB);
					double rad = Math
							.sqrt((obj1.getX() - a) * (obj1.getX() - a) + (obj1.getY() - b) * (obj1.getY() - b));
					obj1.setRad(rad);
					send(Ellipse.class, obj1, FormType.ELLIPSE);
					break;
				case 2:
					Line li = new Line();
					li.setX(r.nextInt(posX + sizeA));
					li.setY(r.nextInt(posY + sizeB));
					li.setA(r.nextInt(posX + sizeA));
					li.setB(r.nextInt(posY + sizeB));
					li.setColor(color);
					send(Line.class, li, FormType.LINE);
					break;
				case 3:
					Snake sna = new Snake();
					int length = r.nextInt(20)+1;
					int[] aElements = new int[length];
					int[] bElements = new int[length];
					aElements[0] = r.nextInt(posX + sizeA);
					bElements[0] = r.nextInt(posY + sizeB);
					for (int i = 1; i < aElements.length; ++i) {
						aElements[i] = aElements[i - 1] + r.nextInt(2) - 1;
						bElements[i] = bElements[i - 1] + r.nextInt(2) - 1;
					}
					sna.setaElements(aElements);
					sna.setbElements(bElements);
					sna.setColor(color);
					send(Snake.class, sna, FormType.SNAKE);
					break;
				case 4:
					Polygon pol = new Polygon();
					int lengthPol = r.nextInt(90);
					int[] aElementsPol = new int[lengthPol];
					int[] bElementsPol = new int[lengthPol];
					for (int i = 0; i < aElementsPol.length; ++i) {
						aElementsPol[i] = r.nextInt(posX + sizeA);
						bElementsPol[i] = r.nextInt(posY + sizeB);
					}
					pol.setaElements(aElementsPol);
					pol.setbElements(bElementsPol);
					pol.setColor(color);
					send(Polygon.class, pol, FormType.SNAKE);
					break;
				}
				try {
					wait(60000);
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
	}

	private void send(Class classes, Object obj, FormType type) {
		JsonNode readTree;
		try {
			readTree = objMapper.readTree(objMapper.writeValueAsString(classes.cast(obj)));
			Message message = translateToJson(type, readTree);
			this.robotSession.getAsyncRemote().sendObject(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private Message translateToJson(FormType type, JsonNode readTree)
			throws IOException, JsonProcessingException, JsonGenerationException, JsonMappingException {
		FormMessage formMessage = new FormMessage();
		formMessage.setId(historyHandler.getCurrentId());
		formMessage.setContent(readTree);
		formMessage.setType(type);
		formMessage.setName("Roboter");
		readTree = objMapper.readTree(objMapper.writeValueAsString(formMessage));
		Message message = new Message();
		message.setType(Type.HISTORY);
		message.setContent(readTree);
		message.setName("Roboter");
		return message;
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		// nothing
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		System.out.println("Robot: Connection opened.");
		robotSession = session;
		active = true;
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Robot: Connection closed.");
		robotSession = null;
	}
}
