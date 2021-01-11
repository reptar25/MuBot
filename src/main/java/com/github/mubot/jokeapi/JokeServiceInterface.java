package com.github.mubot.jokeapi;

import java.util.List;

import reactor.core.publisher.Mono;

public interface JokeServiceInterface {

	public Mono<String> getVersion();

	public Mono<List<String>> getCategories();

	public Mono<List<String>> getJoke(String category);

	public Mono<List<String>> getJoke();

	public Mono<List<String>> getJoke(JokeRequest request);
}
