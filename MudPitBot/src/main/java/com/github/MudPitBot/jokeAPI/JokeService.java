package com.github.MudPitBot.jokeAPI;

import java.util.List;
import reactor.core.publisher.Mono;

public class JokeService implements JokeServiceInterface {

	public Mono<String> getVersion() {
		return Info.getVersion();
	}

	public Mono<List<String>> getCategories() {
		return Categories.getCategories();
	}

	public Mono<List<String>> getJoke(JokeRequest request) {
		return Joke.getJoke(request);
	}

	public Mono<List<String>> getJoke(String category) {
		return Joke.getJoke(category);
	}

	public Mono<List<String>> getJoke() {
		return Joke.getJoke();
	}
}