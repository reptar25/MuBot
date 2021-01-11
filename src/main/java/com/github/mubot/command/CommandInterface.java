package com.github.mubot.command;

import java.util.function.Consumer;

import com.github.mubot.command.help.CommandHelpSpec;

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
	 * Used to create the {@link CommandHelpSpec} for this command
	 * 
	 * @return returns the created {@link CommandHelpSpec} for this command
	 */
	public Consumer<? super CommandHelpSpec> createHelpSpec();

}
