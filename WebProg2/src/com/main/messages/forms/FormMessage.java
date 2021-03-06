package com.main.messages.forms;

import org.codehaus.jackson.JsonNode;

public class FormMessage {
	private FormType type;
	private String name;
	private boolean animated;
	private int xAnimation;
	private int yAnimation;
	private JsonNode content;
	private long id;

	public boolean isAnimated() {
		return animated;
	}

	public void setAnimated(boolean animated) {
		this.animated = animated;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getxAnimation() {
		return xAnimation;
	}

	public void setxAnimation(int xAnimation) {
		this.xAnimation = xAnimation;
	}

	public int getyAnimation() {
		return yAnimation;
	}

	public void setyAnimation(int yAnimation) {
		this.yAnimation = yAnimation;
	}

}
