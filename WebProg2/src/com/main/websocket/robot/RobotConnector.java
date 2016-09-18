package com.main.websocket.robot;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import com.main.coder.Decoder;
import com.main.coder.Encoder;
import com.main.websocket.WebSocket;

public class RobotConnector implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		try {
			ServerContainer container = (ServerContainer) servletContext
					.getAttribute("javax.websocket.server.ServerContainer");
			ServerEndpointConfig c = ServerEndpointConfig.Builder.create(WebSocket.class, "/websocket/robot")
					.decoders(Arrays.asList(Decoder.class)).encoders(Arrays.asList(Encoder.class)).build();
			container.addEndpoint(c);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}