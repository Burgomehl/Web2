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
import com.main.messages.forms.Line;
import com.main.messages.forms.Polygon;
import com.main.messages.forms.Rectangle;
import com.main.messages.forms.Snake;
import com.main.model.History;

@Path("/get")
public class Service {

	private HistoryHandler historyHandler;
	private ObjectMapper objMapper = new ObjectMapper();

	public Service() {
		historyHandler = HistoryHandler.getInstance();
	}

	@GET
	@Path("/history")
	@Produces({ "application/json" })
	public List<Message> test() {
		List<Message> listToSend = new ArrayList<Message>();
		for (FormMessage hist : historyHandler.getHistory()) {
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
	
	private static Color hex2Rgb(String colorStr) {
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}

	@GET
	@Path("/picture")
	@Produces({ "image/png" })
	public Response printImage() {
		BufferedImage image = new BufferedImage(800, 800, 1);
		List<Message> listToSend = new ArrayList<Message>();
		for (FormMessage hist : historyHandler.getHistory()) {
			switch (hist.getType()) {
			case RECTANGLE:
				Rectangle rec;
				try {
					rec = objMapper.readValue(hist.getContent(), Rectangle.class);
					System.out.println(rec.getColor());
					String color = rec.getColor();
					image.getGraphics().setColor(hex2Rgb(color));
					image.getGraphics().drawRect(rec.getA(), rec.getB(), rec.getX(), rec.getY());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case ELLIPSE:
				Ellipse ell;
				try {
					ell = objMapper.readValue(hist.getContent(), Ellipse.class);
					String color = ell.getColor();
					image.getGraphics().setColor(hex2Rgb(color));
					image.getGraphics().drawOval(ell.getX()-(int)ell.getRad(), ell.getY()-(int)ell.getRad(), (int)(ell.getRad()*2), (int)(ell.getRad()*2));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case LINE:
				Line li;
				try {
					li = objMapper.readValue(hist.getContent(), Line.class);
					String color = li.getColor();
					image.getGraphics().setColor(hex2Rgb(color));
					image.getGraphics().drawLine(li.getX(), li.getY(), li.getA(), li.getB());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case POLYGON:
				Polygon pol;
				try {
					pol = objMapper.readValue(hist.getContent(), Polygon.class);
					String color = pol.getColor();
					image.getGraphics().setColor(hex2Rgb(color));
					for(int i = 1; i < pol.getaElements().length; ++i){
						image.getGraphics().drawLine(pol.getaElements()[i-1], pol.getbElements()[i-1], pol.getaElements()[i], pol.getbElements()[i]);
					}
					image.getGraphics().drawLine(pol.getaElements()[0], pol.getbElements()[0], pol.getaElements()[pol.getaElements().length-1], pol.getbElements()[pol.getbElements().length-1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case SNAKE:
				Snake sna;
				try {
					sna = objMapper.readValue(hist.getContent(), Snake.class);
					String color = sna.getColor();
					image.getGraphics().setColor(hex2Rgb(color));
					for(int i = 1; i < sna.getaElements().length; ++i){
						image.getGraphics().drawLine(sna.getaElements()[i-1], sna.getbElements()[i-1], sna.getaElements()[i], sna.getbElements()[i]);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return Response.ok(image).build();
	}
}
