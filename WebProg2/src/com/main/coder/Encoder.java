package com.main.coder;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;
import com.main.messages.Message;

public class Encoder implements javax.websocket.Encoder.Text<Message> {
	
	private Gson gson;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		gson = new Gson();
	}

	@Override
	public String encode(Message s) throws EncodeException {
		return gson.toJson(s);
	}

}
