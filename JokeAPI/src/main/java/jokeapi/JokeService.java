package jokeapi;

import jokeapi.endpoints.Categories;
import jokeapi.endpoints.Info;
import jokeapi.endpoints.Joke;
import reactor.core.publisher.Mono;

import java.util.List;

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