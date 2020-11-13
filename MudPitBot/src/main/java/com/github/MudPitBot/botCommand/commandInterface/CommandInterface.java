package com.github.MudPitBot.botCommand.commandInterface;

import discord4j.core.event.domain.message.MessageCreateEvent;

/*
 * Implementation of the Command design pattern.
 */

public interface CommandInterface {

	public void execute(MessageCreateEvent event);
}
