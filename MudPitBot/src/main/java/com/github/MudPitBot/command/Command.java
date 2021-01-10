package com.github.MudPitBot.command;

import java.util.function.Consumer;

import com.github.MudPitBot.command.help.CommandHelpSpec;
import reactor.core.publisher.Mono;

public abstract class Command implements CommandInterface {

	protected String commandTrigger;

	public Command(String commandTrigger) {
		this.commandTrigger = commandTrigger;
	}

	/**
	 * The String that would cause this command to trigger if typed in a message to
	 * a channel the bot can see
	 * 
	 * @return the literal String of what triggers this command.
	 */
	public String getCommandTrigger() {
		return commandTrigger;
	}

	/**
	 * 
	 * @param spec the CommandHelpSpec to use to create the embed
	 * @return the help embed as a CommandResponse
	 */
	protected final Mono<CommandResponse> createCommandHelpEmbed(Consumer<? super CommandHelpSpec> spec) {
		CommandHelpSpec mutatedSpec = new CommandHelpSpec(getCommandTrigger());
		spec.accept(mutatedSpec);
		return CommandResponse.create(s -> s.setEmbed(mutatedSpec.build()));
	}

}
