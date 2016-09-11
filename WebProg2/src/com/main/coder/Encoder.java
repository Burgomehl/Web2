package com.main.coder;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.main.messages.Message;

public class Encoder implements javax.websocket.Encoder.Text<Message> {
	
	private ObjectMapper objMapper;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		objMapper = new ObjectMapper();
	}

	@Override
	public String encode(Message s) throws EncodeException {
		try {
			return objMapper.writeValueAsString(s);
		} catch (IOException e) {
			throw new EncodeException(s, e.getMessage());
		}
	}

}
