package com.github.mubot.jokeapi;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.github.mubot.jokeapi.util.JokeEnums.JokeLanguage;
import com.github.mubot.jokeapi.util.JokeEnums.JokeType;
import com.github.mubot.jokeapi.util.JokeEnums.ResponseFormat;

import reactor.util.Logger;
import reactor.util.Loggers;

public class JokeRequest {

	private static final Logger LOGGER = Loggers.getLogger(JokeRequest.class);
	private final ArrayList<String> parameters;
	private final JokeRequestOptions options;

	public static JokeRequest createDefaultRequest() {
		return new JokeRequest(new JokeRequestOptions());
	}

	public JokeRequest(JokeRequestOptions options) {
		this.options = options;
		parameters = new ArrayList<String>();
		buildParameters();
	}

	private void buildParameters() {
		if (!options.getBlacklistFlags().isEmpty()) {
			parameters.add("blacklistFlags=" + options.getBlacklistFlags().stream().collect(Collectors.joining(",")));
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
			sb.append(options.getCategories().stream().collect(Collectors.joining(",")));
		} else {
			sb.append("Any");
		}

		if (!parameters.isEmpty()) {
			sb.append("?");
			sb.append(parameters.stream().collect(Collectors.joining("&")));
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("JokeRequest url: " + sb.toString());
		return sb.toString();
	}

}
