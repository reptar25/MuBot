package jokeapi.endpoints;

import jokeapi.JokeClient;
import jokeapi.JokeRequest;
import jokeapi.JokeRequestOptions;
import jokeapi.util.JokeJson;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class Joke {

	private static final String JOKE_URI = "/joke";

	public static Mono<List<String>> getJoke(String category) {
		return getJoke(new JokeRequest(new JokeRequestOptions().addCategory(category)));
	}

	public static Mono<List<String>> getJoke() {
		return getJoke(new JokeRequest(new JokeRequestOptions().addCategory("Any")));
	}

	public static Mono<List<String>> getJoke(JokeRequest request) {
		return JokeClient.getWebClient().get().uri(JOKE_URI + request.toString()).responseContent().aggregate()
				.asString().flatMap(JokeClient::readTree).map(json -> new JokeJson(json.get("type").asText(), json))
				.map(Joke::handleResponse);
	}

	private static List<String> handleResponse(JokeJson jokeJson) {
		ArrayList<String> jokeLines = new ArrayList<>();
		switch (jokeJson.getType()) {
		case "single":
			jokeLines.add(jokeJson.getNode().get("joke").asText());
			return jokeLines;
		case "twopart":
			jokeLines.add(jokeJson.getNode().get("setup").asText());
			jokeLines.add(jokeJson.getNode().get("delivery").asText());
			return jokeLines;
		default:
			throw new IllegalArgumentException("Unknown joke type: " + jokeJson.getType());
		}
	}
}
