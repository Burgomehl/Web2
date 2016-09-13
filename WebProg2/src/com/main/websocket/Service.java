package com.main.websocket;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.main.messages.Message;
import com.main.messages.Type;
import com.main.messages.forms.Ellipse;
import com.main.messages.forms.FormMessage;
import com.main.messages.forms.Rectangle;
import com.main.model.History;

@Path("/get")
public class Service {

	private History history;
	private ObjectMapper objMapper = new ObjectMapper();

	public Service() {
		history = History.getInstance();
	}

	@GET
	@Path("/history")
	@Produces({ "application/json" })
	public List<Message> test() {
		List<Message> listToSend = new ArrayList<Message>();
		for (FormMessage hist : history.getHistory()) {
			JsonNode readTree;
			try {
				readTree = objMapper.readTree(objMapper.writeValueAsString(hist));
				Message message = new Message();
				message.setContent(readTree);
				message.setType(Type.HISTORY);
				listToSend.add(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return listToSend;
	}

	@GET
	@Path("/picture")
	@Produces({ "image/png" })
	public Response printImage() {
		BufferedImage image = new BufferedImage(800, 800, 1);
		List<Message> listToSend = new ArrayList<Message>();
		for (FormMessage hist : history.getHistory()) {
			switch (hist.getType()) {
			case RECTANGLE:
				Rectangle rec;
				try {
					rec = objMapper.readValue(hist.getContent(), Rectangle.class);
					image.getGraphics().setColor(Color.getColor(rec.getColor()));
					image.getGraphics().drawRect(rec.getA(), rec.getB(), rec.getX()-rec.getA(), rec.getY()-rec.getB());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case ELLIPSE:
				Ellipse ell;
				try {
					ell = objMapper.readValue(hist.getContent(), Ellipse.class);
					image.getGraphics().drawOval(ell.getX()-(int)ell.getRad(), ell.getY()-(int)ell.getRad(), (int)(ell.getRad()*2), (int)(ell.getRad()*2));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return Response.ok(image).build();
	}
}
