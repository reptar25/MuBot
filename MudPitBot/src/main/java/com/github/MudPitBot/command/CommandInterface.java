package com.github.MudPitBot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Interface for Command
 */
public interface CommandInterface {

	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args);
	
}
