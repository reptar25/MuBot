package com.github.mubot.command.exceptions;

public class CommandException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String userFriendlyMessage;

	public CommandException(String message) {
		super(message, null, false, false);
		userFriendlyMessage = message;
	}

	public CommandException(String message, String userFriendlyMessage) {
		super(message, null, false, false);
		this.userFriendlyMessage = userFriendlyMessage;
	}

	public void setUserFriendlyMessage(String userFriendlyMessage) {
		this.userFriendlyMessage = userFriendlyMessage;
	}

	public String getUserFriendlyMessage() {
		return userFriendlyMessage;
	}

}
