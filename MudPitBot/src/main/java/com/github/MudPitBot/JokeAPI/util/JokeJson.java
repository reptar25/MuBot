package com.github.MudPitBot.JokeAPI.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JokeJson {
	String type;
	JsonNode node;

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
