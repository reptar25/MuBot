package com.github.MudPitBot.botCommand.commandImpl;

import java.util.HashMap;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

public abstract class Commands {

	// Structure that maps string commands to the concrete implementation of that
	// command
	public static final HashMap<String, Command> COMMANDS = new HashMap<>();
	private static final CommandReceiver RECEIVER = CommandReceiver.getInstance();

	/*
	 * Command names should always be in lower-case here since we do .toLowerCase()
	 * on the command to make them non case sensitive.
	 */
	static {
		COMMANDS.put("join", new JoinVoiceCommand(RECEIVER));
		COMMANDS.put("echo", new EchoCommand(RECEIVER));
		COMMANDS.put("leave", new LeaveVoiceCommand(RECEIVER));
		COMMANDS.put("roll", new RollCommand(RECEIVER));
		COMMANDS.put("play", new PlayCommand(RECEIVER));
		COMMANDS.put("volume", new VolumeCommand(RECEIVER));
		COMMANDS.put("stop", new StopCommand(RECEIVER));
		COMMANDS.put("skip", new SkipCommand(RECEIVER));
		COMMANDS.put("next", new SkipCommand(RECEIVER));
		COMMANDS.put("mute", new MuteCommand(RECEIVER));
		COMMANDS.put("clear", new ClearCommand(RECEIVER));
		COMMANDS.put("viewqueue", new ViewQueueCommand(RECEIVER));
		COMMANDS.put("nowplaying", new NowPlayingCommand(RECEIVER));
	}

}
