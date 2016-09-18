package com.main.coder;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.main.messages.Message;

public class Decoder implements javax.websocket.Decoder.Text<Message> {
	
	private ObjectMapper objMapper;

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		objMapper = new ObjectMapper();
	}

	@Override
	public Message decode(String s) throws DecodeException {
		try {
			System.out.println(s);
			return objMapper.readValue(s, Message.class);
		} catch (IOException e) {
			throw new DecodeException(s,"...");
		}
	}

	@Override
	public boolean willDecode(String s) {
		return s != null;
	}

}
