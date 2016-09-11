package com.main.messages;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonNode;

@XmlRootElement
public class Message {

	private Type type;
	private JsonNode content;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public JsonNode getContent() {
		return content;
	}

	public void setContent(JsonNode content) {
		this.content = content;
	}

}
