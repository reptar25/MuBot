package com.github.MudPitBot.jokeAPI;

import reactor.core.publisher.Mono;

class Info {

	private static final String INFO_URI = "/info";

	public static Mono<String> getVersion() {
		return JokeClient.webClient.get().uri(INFO_URI).responseContent().aggregate().asString()
				.flatMap(JokeClient::readTree).map(json -> json.get("version").asText());
	}

}