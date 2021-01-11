package com.github.mubot.jokeapi.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JokeJson {
	private String type;
	private JsonNode node;

	public String getType() {
		return type;
	}

	public JsonNode getNode() {
		return node;
	}

	public JokeJson(String type, JsonNode json) {
		this.type = type;
		this.node = json;
	}
}
