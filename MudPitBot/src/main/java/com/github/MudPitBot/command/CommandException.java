package com.github.MudPitBot.command;

public class CommandException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	boolean sendMessageError;

	public CommandException(String message) {
		super(message, null, false, false);
		this.sendMessageError = false;
	}

	public CommandException(String message, boolean sendMessageError) {
		super(message, null, false, false);
		this.sendMessageError = sendMessageError;
	}

	public boolean isSendError() {
		return sendMessageError;
	}

}
