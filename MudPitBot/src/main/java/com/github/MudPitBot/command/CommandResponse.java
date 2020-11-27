package com.github.MudPitBot.command;

import java.util.function.Consumer;

import com.github.MudPitBot.command.poll.Poll;

import discord4j.core.spec.MessageCreateSpec;

public class CommandResponse {

	private final String content;
	private final Poll poll;
	private final Consumer<? super MessageCreateSpec> spec;

	public CommandResponse(String content) {
		this.content = content;
		this.spec = spec -> spec.setContent(content);
		this.poll = null;
	}

	public CommandResponse(Consumer<? super MessageCreateSpec> spec) {
		this.spec = spec;
		this.poll = null;
		this.content = null;
	}

	private CommandResponse(Builder b) {
		this.spec = b.spec;
		this.poll = b.poll;
		this.content = b.content;
	}

	public Consumer<? super MessageCreateSpec> getSpec() {
		return spec;
	}

	public Poll getPoll() {
		return poll;
	}

	public String getContent() {
		return content;
	}

	public static class Builder {

		// optional parameters
		private Consumer<? super MessageCreateSpec> spec;
		private Poll poll;
		private String content;

		public Builder() {
		};

		public Builder spec(Consumer<? super MessageCreateSpec> spec) {
			this.spec = spec;
			return this;
		}

		public Builder poll(Poll poll) {
			this.poll = poll;
			return this;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public CommandResponse build() {
			return new CommandResponse(this);
		}
	}

}
