package com.github.MudPitBot.commandInterface;

import com.github.MudPitBot.botCommand.BotReceiver;

public abstract class Command implements CommandInterface {
	
	protected BotReceiver receiver;

	public Command(BotReceiver receiver) {
		this.receiver = receiver;
	}
}
