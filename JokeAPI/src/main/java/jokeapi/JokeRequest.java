package jokeapi;

import jokeapi.util.JokeEnums.JokeLanguage;
import jokeapi.util.JokeEnums.JokeType;
import jokeapi.util.JokeEnums.ResponseFormat;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;

public class JokeRequest {

	private static final Logger LOGGER = Loggers.getLogger(JokeRequest.class);
	private final ArrayList<String> parameters;
	private final JokeRequestOptions options;

	public JokeRequest(JokeRequestOptions options) {
		this.options = options;
		parameters = new ArrayList<>();
		buildParameters();
	}

	private void buildParameters() {
		if (!options.getBlacklistFlags().isEmpty()) {
			parameters.add("blacklistFlags=" + String.join(",", options.getBlacklistFlags()));
		}

		if (!options.getLanguage().equals(JokeLanguage.DEFAULT)) {
			parameters.add(options.getLanguage().toString());
		}

		if (!options.getResponseFormat().equals(ResponseFormat.DEFAULT)) {
			parameters.add(options.getResponseFormat().toString());
		}

		if (!options.getJokeType().equals(JokeType.DEFAULT)) {
			parameters.add(options.getJokeType().toString());
		}

		if (!options.getContains().isEmpty()) {
			parameters.add("contains=" + options.getContains());
		}

		if (options.getAmount() > 1) {
			parameters.add("amount=" + options.getAmount());
		}

		if (options.isSafeMode()) {
			parameters.add("safe-mode");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("/");

		if (!options.getCategories().isEmpty()) {
			sb.append(String.join(",", options.getCategories()));
		} else {
			sb.append("Any");
		}

		if (!parameters.isEmpty()) {
			sb.append("?");
			sb.append(String.join("&", parameters));
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("JokeRequest url: " + sb.toString());
		return sb.toString();
	}

}
