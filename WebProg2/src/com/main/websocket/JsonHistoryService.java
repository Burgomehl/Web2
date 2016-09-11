package com.main.websocket;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.main.messages.Message;
import com.main.model.History;

@Path("/History")
public class JsonHistoryService {
	
		private History history;
		
		public JsonHistoryService() {
			history = History.getInstance();
		}
		@GET
		@Path("/get")
		@Produces({"application/json"})
		public List<Message> test() {
			return history.getHistory();
		}
}
