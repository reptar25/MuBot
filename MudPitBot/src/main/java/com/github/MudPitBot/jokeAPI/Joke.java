package com.github.MudPitBot.jokeAPI;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

class Joke {

	private static final String JOKE_URI = "/joke";

	public static Mono<List<String>> getJoke(String category) {
		return getJoke(new JokeRequest.Builder().addCategory(category).build());
	}

	public static Mono<List<String>> getJoke() {
		return getJoke(new JokeRequest.Builder().addCategory("Any").build());
	}

	public static Mono<List<String>> getJoke(JokeRequest request) {
		return JokeClient.webClient.get().uri(JOKE_URI + request.toString()).responseContent().aggregate().asString()
				.flatMap(JokeClient::readTree).map(json -> new JokeJson(json.get("type").asText(), json))
				.map(Joke::handleResponse);
	}

	private static List<String> handleResponse(JokeJson jokeJson) {
		ArrayList<String> jokeLines = new ArrayList<String>();
		switch (jokeJson.getType()) {
		case "single":
			jokeLines.add(jokeJson.getNode().get("joke").asText());
			return jokeLines;
		case "twopart":
			jokeLines.add(jokeJson.getNode().get("setup").asText());
			jokeLines.add(jokeJson.getNode().get("delivery").asText());
			return jokeLines;
		default:
			throw new RuntimeException("Unknown joke type: " + jokeJson.getType());
		}
	}

}

class JokeJson {
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
