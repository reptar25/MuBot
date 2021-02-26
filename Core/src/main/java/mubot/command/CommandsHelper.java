package mubot.command;

import org.reflections.Reflections;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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
        // scan urls that contain 'mubot.command.commands.', include
        // inputs starting with 'mubot.command.commands.', use the
        // default scanners
        Reflections reflections = new Reflections("mubot.command.commands.");
        // get a set of all the subclasses of Command
        Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);

        // loop through list of subclasses and instantiate each one and add it to the
        // map with the command trigger as the key
        for (Class<? extends Command> c : subTypes) {
            try {

                Class<?> clazz = Class.forName(c.getName());
                Constructor<?> constructor = clazz.getConstructor();
                Command instance = (Command) constructor.newInstance();
                for (String trigger : instance.getCommandTriggers()) {
                    COMMANDS.put(trigger, instance);
                }
            } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (NoSuchMethodException ignored) {
            }
        }
    }

    /**
     * returns a copy of the set to prevent the COMMANDS hashmap from being mutated
     * through this method
     *
     * @return the entries of the command map
     */
    public static Set<Entry<String, Command>> getEntries() {
        return new HashSet<>(COMMANDS.entrySet());
    }

    public static Optional<Command> get(String key) {
        return Optional.ofNullable(COMMANDS.get(key));
    }

}
