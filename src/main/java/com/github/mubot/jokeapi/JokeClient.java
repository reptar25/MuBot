package com.github.mubot.jokeapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class JokeClient {

	private static final String JOKE_API_BASE_URL = "https://v2.jokeapi.dev";
	private static final String JOKE_MIME_TYPE = "application/json";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static JokeServiceInterface jokeService;
	protected static HttpClient webClient;

	static {
		jokeService = new JokeService();
		webClient = HttpClient.create().baseUrl(JOKE_API_BASE_URL)
				.headers(h -> h.set(HttpHeaderNames.CONTENT_TYPE, JOKE_MIME_TYPE));
	}

	public static Mono<JsonNode> readTree(String json) {
		try {
			return checkError(OBJECT_MAPPER.readTree(json));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Mono<JsonNode> checkError(JsonNode root) {
		if (root.get("error").asBoolean())
			return Mono.error(new RuntimeException(
					"jokeapi error: " + root.get("message").asText() + "\n" + root.get("additionalInfo").asText()));

		return Mono.just(root);
	}

	public static JokeServiceInterface getJokeService() {
		return jokeService;
	}

	public static HttpClient getWebClient() {
		return webClient;
	}
	
}
