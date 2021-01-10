package com.github.MudPitBot.JokeAPI;

import java.util.List;

import com.github.MudPitBot.JokeAPI.endpoints.Categories;
import com.github.MudPitBot.JokeAPI.endpoints.Info;
import com.github.MudPitBot.JokeAPI.endpoints.Joke;

import reactor.core.publisher.Mono;

public class JokeService implements JokeServiceInterface {

	@Override
	public Mono<String> getVersion() {
		return Info.getVersion();
	}

	@Override
	public Mono<List<String>> getCategories() {
		return Categories.getCategories();
	}

	@Override
	public Mono<List<String>> getJoke(JokeRequest request) {
		return Joke.getJoke(request);
	}

	@Override
	public Mono<List<String>> getJoke(String category) {
		return Joke.getJoke(category);
	}

	@Override
	public Mono<List<String>> getJoke() {
		return Joke.getJoke();
	}
}