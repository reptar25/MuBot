package com.github.MudPitBot.botCommand.commandInterface;

import discord4j.core.event.domain.message.MessageCreateEvent;

public interface CommandInterface {

	public void execute(MessageCreateEvent event);
}
