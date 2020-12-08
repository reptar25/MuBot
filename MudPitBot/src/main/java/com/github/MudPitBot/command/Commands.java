package com.github.MudPitBot.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.reflections.Reflections;

import com.github.MudPitBot.command.core.CommandReceiver;

import java.lang.reflect.*;

public final class Commands {

	public static final char COMMAND_PREFIX = '!';

	// Immutable structure that maps string commands to the concrete implementation
	// of that command.
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();
	private static final CommandReceiver RECEIVER = CommandReceiver.getInstance();

	static {
		buildCommandMap();
	}

	/**
	 * Use Reflections library to scan class path for subclasses of Command and add
	 * those to the Commands map. This way any new commands that are created that
	 * extend Command automatically get added to the map without any extra work
	 */
	private static void buildCommandMap() {
		// scan urls that contain 'com.github.MudPitBot.botCommand.commandImpl', include
		// inputs starting with'com.github.MudPitBot.botCommand.commandImpl', use the
		// default scanners
		Reflections reflections = new Reflections("com.github.MudPitBot.command.impl");
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
	 * returns a copy of the collection to prevent the COMMANDS hashmap from being
	 * mutated through this method
	 * 
	 * @return the literal String values of the command map
	 */
	public static final Collection<Command> values() {
		// make a defensive copy to prevent the actual map from being mutated
		final Collection<Command> commands = new ArrayList<Command>(COMMANDS.values());
		return commands;
	}

	/**
	 * returns a copy of the set to prevent the COMMANDS hashmap from being mutated
	 * through this method
	 * 
	 * @return the entries of the command map
	 */
	public static final Set<Entry<String, Command>> getEntries() {
		Set<Entry<String, Command>> entries = new HashSet<Entry<String, Command>>(COMMANDS.entrySet());
		return entries;
	}

}
