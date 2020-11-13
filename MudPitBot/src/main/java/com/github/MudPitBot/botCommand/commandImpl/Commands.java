package com.github.MudPitBot.botCommand.commandImpl;

import java.util.HashMap;

import com.github.MudPitBot.botCommand.BotReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

public abstract class Commands {
	
	// Structure that maps string commands to the concrete implementation of that command
	public static final HashMap<String, Command> COMMANDS = new HashMap<>();
	private static final BotReceiver RECEIVER = BotReceiver.getInstance();
	
	/*
	 * Command names should always be in lower-case here since we do .toLowerCase() on the command to make them non case sensitive.
	 */
	static {
		COMMANDS.put("join", new JoinVoiceCommand(RECEIVER));
		COMMANDS.put("echo", new EchoCommand(RECEIVER));
		COMMANDS.put("leave", new LeaveVoiceCommand(RECEIVER));
		COMMANDS.put("roll", new RollCommand(RECEIVER));
		COMMANDS.put("play", new PlayCommand(RECEIVER));
	}

}
