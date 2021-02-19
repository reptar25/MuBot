package jokeapi.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import jokeapi.JokeClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Categories {

	private static final String CATEGORIES_URI = "/categories";

	public static Mono<List<String>> getCategories() {
		return JokeClient.getWebClient().get().uri(CATEGORIES_URI).responseContent().aggregate().asString()
				.flatMap(JokeClient::readTree)
				.map(json -> StreamSupport.stream(json.get("categories").spliterator(), true).map(JsonNode::asText)
						.collect(Collectors.toList()));
	}

}