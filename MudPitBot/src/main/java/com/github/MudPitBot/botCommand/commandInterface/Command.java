package com.github.MudPitBot.botCommand.commandInterface;

import com.github.MudPitBot.botCommand.CommandReceiver;

/*
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {
	
	protected CommandReceiver receiver;

	public Command(CommandReceiver receiver) {
		this.receiver = receiver;
	}
}
