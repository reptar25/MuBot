package com.github.MudPitBot.botCommand.commandImpl;

import java.util.HashMap;

import com.github.MudPitBot.botCommand.BotReceiver;
import com.github.MudPitBot.botCommand.commandInterface.CommandInterface;

public abstract class Commands {
	
	public static final HashMap<String, CommandInterface> COMMANDS = new HashMap<>();
	private static final BotReceiver RECEIVER = BotReceiver.getInstance();
	
	/*
	 * Command names should always be in lower-case here since we do .toLowerCase() on the command to make them none case sensitive.
	 */
	static {
		COMMANDS.put("join", new JoinVoiceCommand(RECEIVER));
		COMMANDS.put("echo", new EchoCommand(RECEIVER));
		COMMANDS.put("leave", new LeaveVoiceCommand(RECEIVER));
		COMMANDS.put("roll", new RollCommand(RECEIVER));
	}

}
