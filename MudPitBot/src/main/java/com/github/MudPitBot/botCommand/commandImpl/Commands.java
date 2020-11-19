package com.github.MudPitBot.botCommand.commandImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.commandInterface.Command;

public abstract class Commands {

	// TODO: There is probably a better way to handle this other than a static map,
	// but this works for now.

	// Immutable structure that maps string commands to the concrete implementation of that
	// command.
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();
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
		COMMANDS.put("poll", new PollCommand(RECEIVER));
	}
	
	
	/**
	 * @return the entries of the command map
	 */
	public static final Set<Entry<String, Command>> entries (){
		return COMMANDS.entrySet();
	}
	
	/**
	 * @return the values of the command map
	 */
	public static final Collection<Command> values () {
		return COMMANDS.values();
	}

}
