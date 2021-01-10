package com.github.mudpitbot.jokeapi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mudpitbot.jokeapi.util.JokeEnums.BlacklistFlag;
import com.github.mudpitbot.jokeapi.util.JokeEnums.JokeLanguage;
import com.github.mudpitbot.jokeapi.util.JokeEnums.JokeType;
import com.github.mudpitbot.jokeapi.util.JokeEnums.ResponseFormat;

import reactor.util.Logger;
import reactor.util.Loggers;

public class JokeRequest {

	private static final Logger LOGGER = Loggers.getLogger(JokeRequest.class);
	private final List<String> categories;
	private final List<String> blacklistFlags;
	private final JokeLanguage language;
	private final ResponseFormat responseFormat;
	private final JokeType jokeType;
	private final String contains;
	private final int amount;
	private final boolean safeMode;
	private final ArrayList<String> parameters;

	public static JokeRequest createDefaultRequest() {
		return new JokeRequest.Builder().build();
	}

	private JokeRequest(Builder b) {
		this.categories = b.categories;
		this.blacklistFlags = b.blacklistFlags;
		this.language = b.language;
		this.responseFormat = b.responseFormat;
		this.jokeType = b.jokeType;
		this.contains = b.contains;
		this.amount = b.amount;
		this.safeMode = b.safeMode;

		parameters = new ArrayList<String>();
		buildParameters();
	}

	private void buildParameters() {
		if (!blacklistFlags.isEmpty()) {
			parameters.add("blacklistFlags=" + blacklistFlags.stream().collect(Collectors.joining(",")));
		}

		if (!language.equals(JokeLanguage.DEFAULT)) {
			parameters.add(language.toString());
		}

		if (!responseFormat.equals(ResponseFormat.DEFAULT)) {
			parameters.add(responseFormat.toString());
		}

		if (!jokeType.equals(JokeType.DEFAULT)) {
			parameters.add(jokeType.toString());
		}

		if (!contains.isEmpty()) {
			parameters.add("contains=" + contains);
		}

		if (amount > 1) {
			parameters.add("amount=" + amount);
		}

		if (safeMode) {
			parameters.add("safe-mode");
		}
	}

	public static class Builder {
		private List<String> categories = new ArrayList<String>();
		private List<String> blacklistFlags = new ArrayList<String>();
		private JokeLanguage language = JokeLanguage.DEFAULT;
		private ResponseFormat responseFormat = ResponseFormat.DEFAULT;
		private JokeType jokeType = JokeType.DEFAULT;
		private String contains = "";
		private int amount = 1;
		private boolean safeMode = false;

		public Builder addCategory(String category) {
			this.categories.add(category);
			return this;
		}

		public Builder addBlacklistFlag(BlacklistFlag flag) {
			blacklistFlags.add(flag.toString());
			return this;
		}

		public Builder withJokeLanguage(JokeLanguage language) {
			this.language = language;
			return this;
		}

		public Builder withResponseFormat(ResponseFormat responseFormat) {
			this.responseFormat = responseFormat;
			return this;
		}

		public Builder withJokeType(JokeType jokeType) {
			this.jokeType = jokeType;
			return this;
		}

		public Builder withContains(String contains) {
			this.contains = contains;
			return this;
		}

		public Builder withAmount(int amount) {
			this.amount = amount;
			return this;
		}

		public Builder safeMode(boolean safeMode) {
			this.safeMode = safeMode;
			return this;
		}

		public JokeRequest build() {
			return new JokeRequest(this);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("/");

		if (!categories.isEmpty()) {
			sb.append(categories.stream().collect(Collectors.joining(",")));
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
