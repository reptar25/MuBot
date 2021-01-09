package com.github.MudPitBot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Interface for Command
 */
public interface CommandInterface {

	/**
	 * Executes the command
	 * 
	 * @param event the MessageCreateEvent
	 * @param args  the arguments for the command
	 * @return the response of the command
	 */
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args);

	/**
	 * 
	 * @return the help embed for this command as a CommandResponse
	 */
	public Mono<CommandResponse> getHelp();
}
