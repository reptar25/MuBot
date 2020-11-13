package com.github.MudPitBot.botCommand.commandInterface;

import com.github.MudPitBot.botCommand.BotReceiver;

/*
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {
	
	protected BotReceiver receiver;

	public Command(BotReceiver receiver) {
		this.receiver = receiver;
	}
}
