package com.github.MudPitBot.jokeAPI;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import reactor.core.publisher.Mono;

class Categories {

	private static final String CATEGORIES_URI = "/categories";

	public static Mono<List<String>> getCategories() {
		return JokeClient.webClient.get().uri(CATEGORIES_URI).responseContent().aggregate().asString()
				.flatMap(JokeClient::readTree)
				.map(json -> StreamSupport.stream(json.get("categories").spliterator(), true).map(node -> node.asText())
						.collect(Collectors.toList()));
	}

}