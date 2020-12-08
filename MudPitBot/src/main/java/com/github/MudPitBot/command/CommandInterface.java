package com.github.MudPitBot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Interface for Command
 */
public interface CommandInterface {

	public CommandResponse execute(MessageCreateEvent event, String[] params);
	
}
