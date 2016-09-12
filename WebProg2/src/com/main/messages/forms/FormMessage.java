package com.main.messages.forms;

import org.codehaus.jackson.JsonNode;

public class FormMessage {
	private FormType type;
	private JsonNode content;
	private long id;

	public FormType getType() {
		return type;
	}

	public void setType(FormType type) {
		this.type = type;
	}

	public JsonNode getContent() {
		return content;
	}

	public void setContent(JsonNode content) {
		this.content = content;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}