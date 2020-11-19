package com.github.MudPitBot.botCommand.commandInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.reflections.Reflections;

import com.github.MudPitBot.botCommand.CommandReceiver;

import java.lang.reflect.*;

public abstract class Commands {
	// Immutable structure that maps string commands to the concrete implementation
	// of that command.
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();
	private static final CommandReceiver RECEIVER = CommandReceiver.getInstance();

	/*
	 * Command names should always be in lower-case here since we do .toLowerCase()
	 * when checking the command to make them non case sensitive.
	 */
	static {
//		COMMANDS.put("join", new JoinVoiceCommand(RECEIVER));
//		COMMANDS.put("echo", new EchoCommand(RECEIVER));
//		COMMANDS.put("leave", new LeaveVoiceCommand(RECEIVER));
//		COMMANDS.put("roll", new RollCommand(RECEIVER));
//		COMMANDS.put("play", new PlayCommand(RECEIVER));
//		COMMANDS.put("volume", new VolumeCommand(RECEIVER));
//		COMMANDS.put("stop", new StopCommand(RECEIVER));
//		COMMANDS.put("skip", new SkipCommand(RECEIVER));
//		COMMANDS.put("next", new SkipCommand(RECEIVER));
//		COMMANDS.put("mute", new MuteCommand(RECEIVER));
//		COMMANDS.put("clear", new ClearCommand(RECEIVER));
//		COMMANDS.put("viewqueue", new ViewQueueCommand(RECEIVER));
//		COMMANDS.put("nowplaying", new NowPlayingCommand(RECEIVER));
//		COMMANDS.put("poll", new PollCommand(RECEIVER));
		buildCommandMap();
	}

	/**
	 * @return the entries of the command map
	 */
	public static final Set<Entry<String, Command>> entries() {
		return COMMANDS.entrySet();
	}

	/*
	 * Use Reflections library to scan class path for subclasses of Command and add
	 * those to the Commands map. This way any new commands that are created that
	 * extend Command automatically get added to the map without any extra work
	 */
	private static void buildCommandMap() {
		// scan urls that contain 'com.github.MudPitBot.botCommand.commandImpl', include
		// inputs starting with'com.github.MudPitBot.botCommand.commandImpl', use the
		// default scanners
		Reflections reflections = new Reflections("com.github.MudPitBot.botCommand.commandImpl");
		// get a set of all the subclasses of Command
		Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);

		// loop through list of subclasses and instantiate each one and add it to the
		// map with the command trigger as the key
		for (Class<? extends Command> c : subTypes) {
			try {

				Class<?> clazz = Class.forName(c.getName());
				Constructor<?> constructor = clazz.getConstructor(CommandReceiver.class);
				Command instance = (Command) constructor.newInstance(RECEIVER);
				COMMANDS.put(instance.getCommandTrigger().toLowerCase(), instance);

			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the values of the command map
	 */
	public static final Collection<Command> values() {
		return COMMANDS.values();
	}

}
