package mubot.command.commands.general;

import discord4j.core.event.domain.message.MessageCreateEvent;
import mubot.command.Command;
import mubot.command.CommandResponse;
import mubot.command.CommandsHelper;
import mubot.command.help.CommandHelpSpec;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static mubot.command.util.CommandUtil.getEscapedGuildPrefixFromEvent;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", Arrays.asList("commands", "h"));
    }

    @Override
    public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
        return help(event);
    }

    /**
     * Returns a list of all commands in the channel the message was sent.
     *
     * @param event the event of the message
     * @return List of available commands
     */
    public Mono<CommandResponse> help(MessageCreateEvent event) {
        Set<Entry<String, Command>> entries = CommandsHelper.getEntries();
        String commands = entries.stream()
                .map(entry -> String.format("%n%s%s", getEscapedGuildPrefixFromEvent(event),
                        entry.getValue().getPrimaryTrigger()))
                .distinct().sorted().collect(Collectors.joining());

        return CommandResponse.create(message -> message.setEmbed(
                embed -> embed.setTitle(String.format("Use ***help*** with any command to get more information on that command, for example `%splay help`.", getEscapedGuildPrefixFromEvent(event)))
                        .addField("Available commands: ", commands, false)));
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Displays a list of available commands you can use with the bot.");
    }

}
