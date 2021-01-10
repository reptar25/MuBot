package com.github.MudPitBot.command.exceptions;

public class BotPermissionException extends CommandException {

	private static final long serialVersionUID = 1L;

	public BotPermissionException(String message) {
		super(message);
	}

	public BotPermissionException(String message, String userFriendlyMessage) {
		super(message, userFriendlyMessage);
	}

}
