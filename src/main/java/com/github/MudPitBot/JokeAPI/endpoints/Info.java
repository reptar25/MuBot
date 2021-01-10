package com.github.MudPitBot.JokeAPI.endpoints;

import com.github.MudPitBot.JokeAPI.JokeClient;

import reactor.core.publisher.Mono;

public class Info {

	private static final String INFO_URI = "/info";

	public static Mono<String> getVersion() {
		return JokeClient.getWebClient().get().uri(INFO_URI).responseContent().aggregate().asString()
				.flatMap(JokeClient::readTree).map(json -> json.get("version").asText());
	}

}