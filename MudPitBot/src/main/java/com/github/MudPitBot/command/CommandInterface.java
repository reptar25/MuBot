package com.github.MudPitBot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * Interface for Command
 */
public interface CommandInterface {

	public CommandResponse execute(MessageCreateEvent event, String[] params);
	

	/**
	 * The String that would cause this command to trigger if typed in a message to
	 * a channel the bot can see
	 * 
	 * @return the literal String of what triggers this command.
	 */
	/*
	 * This enforces users to implement what the command trigger should be when
	 * making a subclass. If we just used a protected variable then there would be
	 * no way to enforce it being set.
	 */
	public abstract String getCommandTrigger();
}
