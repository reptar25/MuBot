package com.github.MudPitBot.command;

import java.util.function.Consumer;

import com.github.MudPitBot.command.misc.Poll;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public class CommandResponse {

	private String content;
	private Poll poll;
	private Consumer<? super MessageCreateSpec> spec;

	// empty constructor
	private CommandResponse() {
		this.content = "";
		this.spec = null;
	}

	private CommandResponse(String content) {
		this.content = "";
		this.spec = null;
		// if the message is longer than 2000 character, trim it so that its not over
		// the max character limit.
		if (content.length() >= Message.MAX_CONTENT_LENGTH)
			content = content.substring(0, Message.MAX_CONTENT_LENGTH - 1);
		this.content = content;
		this.spec = spec -> spec.setContent(this.content);
		this.poll = null;
	}

	private CommandResponse(Consumer<? super MessageCreateSpec> spec) {
		this.content = "";
		this.spec = null;
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

	private static final CommandResponse empty = new CommandResponse();
	public static CommandResponse emptyResponse() {
		return empty;
	}

	public static Mono<CommandResponse> empty() {
		return Mono.just(empty);
	}
	
	public static CommandResponse createFlat(String content) {
		return new CommandResponse(content);
	}

	public static Mono<CommandResponse> create(String content) {
		return Mono.just(new CommandResponse(content));
	}

	public static Mono<CommandResponse> create(String content, Poll p) {
		return Mono.just(new CommandResponse(content).withPoll(p));
	}
	
	public static Mono<CommandResponse> create(Consumer<? super MessageCreateSpec> spec, Poll p){
		return Mono.just(new CommandResponse(spec).withPoll(p));
	}

}
