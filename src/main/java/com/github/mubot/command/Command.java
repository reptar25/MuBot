package com.github.mubot.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.mubot.command.help.CommandHelpSpec;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public abstract class Command implements CommandInterface {

	protected String commandTrigger;
	protected List<String> aliases;
	private List<String> triggers = new ArrayList<String>();

	public Command(String commandTrigger) {
		this.commandTrigger = commandTrigger;
		triggers.add(commandTrigger);
	}

	public Command(String commandTrigger, List<String> aliases) {
		this.commandTrigger = commandTrigger;
		this.aliases = aliases;

		triggers.add(commandTrigger);
		triggers.addAll(aliases);
	}

	/**
	 * The String that would cause this command to trigger if typed in a message to
	 * a channel the bot can see
	 * 
	 * @return the literal String of what triggers this command.
	 */
	public List<String> getCommandTriggers() {
		return triggers;
	}

	public String getPrimaryTrigger() {
		return commandTrigger;
	}

	/**
	 * 
	 * @param spec the CommandHelpSpec to use to create the embed
	 * @return the help embed as a CommandResponse
	 */
	private final Mono<CommandResponse> createCommandHelpEmbed(Consumer<? super CommandHelpSpec> spec, long guildId) {
		CommandHelpSpec mutatedSpec = new CommandHelpSpec(getPrimaryTrigger(), aliases, guildId);
		spec.accept(mutatedSpec);
		return CommandResponse.create(s -> s.setEmbed(mutatedSpec.build()));
	}

	/**
	 * @param guildId
	 * @return the help embed for this command as a CommandResponse
	 */
	public Mono<CommandResponse> getHelp(long guildId) {
		return createCommandHelpEmbed(createHelpSpec(), guildId);

	}
	
	/**
	 * @param event
	 * @return the help embed for this command as a CommandResponse
	 */
	public Mono<CommandResponse> getHelp(MessageCreateEvent event) {
		return getHelp(event.getGuildId().orElse(Snowflake.of(-1)).asLong());

	}
	

}
