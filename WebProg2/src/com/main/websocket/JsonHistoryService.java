package com.main.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.main.messages.Message;
import com.main.messages.Type;
import com.main.messages.forms.FormMessage;
import com.main.model.History;

@Path("/History")
public class JsonHistoryService {
	
		private History history;
		private ObjectMapper objMapper = new ObjectMapper();
		
		public JsonHistoryService() {
			history = History.getInstance();
		}
		@GET
		@Path("/get")
		@Produces({"application/json"})
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
}
