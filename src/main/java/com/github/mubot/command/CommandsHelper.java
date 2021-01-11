package com.github.mubot.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;

import reactor.util.Logger;
import reactor.util.Loggers;

public final class CommandsHelper {

	private static final Logger LOGGER = Loggers.getLogger(CommandsHelper.class);

	// Immutable structure that maps string commands to the concrete implementation
	// of that command.
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();

	static {
		buildCommandMap();
	}

	/**
	 * Use Reflections library to scan class path for subclasses of {@link Command}
	 * and adds those to the commands map. This way any new commands that are
	 * created that extend {@link Command} automatically get added to the map
	 * without any extra work
	 */
	private static void buildCommandMap() {
		// scan urls that contain 'com.github.mubot.command.commands.', include
		// inputs starting with'com.github.mubot.command.commands.', use the
		// default scanners
		Reflections reflections = new Reflections("com.github.mubot.command.commands.");
		// get a set of all the subclasses of Command
		Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);

		// loop through list of subclasses and instantiate each one and add it to the
		// map with the command trigger as the key
		for (Class<? extends Command> c : subTypes) {
			try {

				Class<?> clazz = Class.forName(c.getName());
				Constructor<?> constructor = clazz.getConstructor();
				Command instance = (Command) constructor.newInstance();
				COMMANDS.put(instance.getCommandTrigger().toLowerCase(), instance);

			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
				LOGGER.error(e.getMessage(), e);
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
		return new ArrayList<Command>(COMMANDS.values());
	}

	/**
	 * returns a copy of the set to prevent the COMMANDS hashmap from being mutated
	 * through this method
	 * 
	 * @return the entries of the command map
	 */
	public static final Set<Entry<String, Command>> getEntries() {
		return new HashSet<Entry<String, Command>>(COMMANDS.entrySet());
	}

	public static final Optional<Command> get(String key) {
		return Optional.ofNullable(COMMANDS.get(key));
	}

}
