package com.github.MudPitBot.botCommand.commandInterface;

import com.github.MudPitBot.botCommand.CommandReceiver;

/*
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {
	
	protected CommandReceiver receiver;
	protected String commandTrigger;

	public Command(CommandReceiver receiver) {
		this.receiver = receiver;
	}
	
	
	/**
	 * This enforces users to implement what the command trigger should be when subclassing. If it was just a varible subclasses inherited then
	 * there would be no way to enforce it being set.
	 * @return the string literal of what triggers this command.
	 */
	public abstract String getCommandTrigger();
}
