package com.github.mubot.jokeapi;

import java.util.ArrayList;
import java.util.List;

import com.github.mubot.jokeapi.util.JokeEnums.BlacklistFlag;
import com.github.mubot.jokeapi.util.JokeEnums.JokeLanguage;
import com.github.mubot.jokeapi.util.JokeEnums.JokeType;
import com.github.mubot.jokeapi.util.JokeEnums.ResponseFormat;

public class JokeRequestOptions {

	private List<String> categories = new ArrayList<String>();
	private List<String> blacklistFlags = new ArrayList<String>();
	private JokeLanguage language = JokeLanguage.DEFAULT;
	private ResponseFormat responseFormat = ResponseFormat.DEFAULT;
	private JokeType jokeType = JokeType.DEFAULT;
	private String contains = "";
	private int amount = 1;
	private boolean safeMode = false;

	public JokeRequestOptions addCategory(String category) {
		this.categories.add(category);
		return this;
	}

	public JokeRequestOptions addBlacklistFlag(BlacklistFlag flag) {
		blacklistFlags.add(flag.toString());
		return this;
	}

	public JokeRequestOptions withJokeLanguage(JokeLanguage language) {
		this.language = language;
		return this;
	}

	public JokeRequestOptions withResponseFormat(ResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
		return this;
	}

	public JokeRequestOptions withJokeType(JokeType jokeType) {
		this.jokeType = jokeType;
		return this;
	}

	public JokeRequestOptions withContains(String contains) {
		this.contains = contains;
		return this;
	}

	public JokeRequestOptions withAmount(int amount) {
		this.amount = amount;
		return this;
	}

	public JokeRequestOptions safeMode(boolean safeMode) {
		this.safeMode = safeMode;
		return this;
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<String> getBlacklistFlags() {
		return blacklistFlags;
	}

	public JokeLanguage getLanguage() {
		return language;
	}

	public ResponseFormat getResponseFormat() {
		return responseFormat;
	}

	public JokeType getJokeType() {
		return jokeType;
	}

	public String getContains() {
		return contains;
	}

	public int getAmount() {
		return amount;
	}

	public boolean isSafeMode() {
		return safeMode;
	}
}
