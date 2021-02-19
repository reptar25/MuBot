package jokeapi;

import java.util.List;

import reactor.core.publisher.Mono;

public interface JokeServiceInterface {

	Mono<String> getVersion();

	Mono<List<String>> getCategories();

	Mono<List<String>> getJoke(String category);

	Mono<List<String>> getJoke();

	Mono<List<String>> getJoke(JokeRequest request);
}
