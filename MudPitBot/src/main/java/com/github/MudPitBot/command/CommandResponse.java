package com.github.MudPitBot.command;

import java.util.function.Consumer;

import com.github.MudPitBot.command.misc.Poll;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

public class CommandResponse {

	private final String content;
	private Poll poll;
	private final Consumer<? super MessageCreateSpec> spec;

	public CommandResponse(String content) {
		// if the message is longer than 2000 character, trim it so that its not over
		// the max character limit.
		if (content.length() >= Message.MAX_CONTENT_LENGTH)
			content = content.substring(0, Message.MAX_CONTENT_LENGTH - 1);
		this.content = content;
		this.spec = spec -> spec.setContent(this.content);
		this.poll = null;
	}

	public CommandResponse(Consumer<? super MessageCreateSpec> spec) {
		this.spec = spec;
		this.poll = null;
		this.content = null;
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

	public CommandResponse withPoll(Poll poll) {
		this.poll = poll;
		return this;
	}

}
