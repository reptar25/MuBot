package mubot.eventlistener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import mubot.command.Command;
import mubot.command.CommandExecutor;
import mubot.command.CommandResponse;
import mubot.command.CommandsHelper;
import mubot.command.exceptions.CommandException;
import mubot.command.util.EmojiHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Arrays;
import java.util.function.Predicate;

import static mubot.command.util.CommandUtil.getEscapedGuildPrefixFromEvent;
import static mubot.command.util.CommandUtil.sendReply;

public class CommandListener implements EventListener<MessageCreateEvent> {

    private static final Logger LOGGER = Loggers.getLogger(CommandListener.class);

    private static final int MAX_COMMANDS_PER_MESSAGE = 5;
    private static final CommandExecutor commandExecutor = new CommandExecutor();

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> consume(MessageCreateEvent e) {
        return Mono.just(e).filter(event -> !event.getMessage().getAuthor().map(User::isBot).orElse(true))
                .flatMap(this::receiveMessage).onErrorResume(error -> {
                    LOGGER.error("Error receiving message.\nMessage: " + e.getMessage().getContent() + "\nError: " + error.getMessage());
                    return Mono.empty();
                }).then();
    }

    /**
     * Called when a message is created that is not from a bot. This checks the
     * message for any commands and executes those commands
     *
     * @param event the MessageCreateEvent
     * @return Mono<Void>
     */
    private Mono<Void> receiveMessage(MessageCreateEvent event) {
        return parseCommands(event).flatMap(commandString -> Mono
                .justOrEmpty(CommandsHelper.get(commandString.split(" ")[0].toLowerCase()))
                .flatMap(command -> Mono.just(commandString.trim().split(" "))
                        .flatMap(splitCommand -> Mono.just(Arrays.copyOfRange(splitCommand, 1, splitCommand.length)))
                        .flatMap(commandArgs -> executeCommand(event, command, commandArgs)))
                .onErrorResume(CommandException.class, error -> onCommandException(event, error))).then();
    }

    private Flux<String> parseCommands(MessageCreateEvent event) {
        String prefix = getEscapedGuildPrefixFromEvent(event);
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" (?=" + prefix + ")"))
                .flatMapMany(Flux::fromArray).map(content -> {
                    if (content.startsWith(prefix))
                        return content.replaceAll(prefix, "");
                    else
                        return "";
                }).filter(Predicate.not(String::isBlank)).take(MAX_COMMANDS_PER_MESSAGE);
    }

    private Mono<Void> onCommandException(MessageCreateEvent event, CommandException error) {
        long guildId = 0;
        if (event.getGuildId().isPresent())
            guildId = event.getGuildId().get().asLong();
        LOGGER.error("GuildId: " + guildId + ", EventMessage: " + event.getMessage().getContent() + ", ErrorMessage: "
                + error.getMessage());

        // Send command errors back as a reply to the user who used the command
        return sendReply(event,
                CommandResponse.createFlat(
                        EmojiHelper.NO_ENTRY + " " + error.getUserFriendlyMessage() + " " + EmojiHelper.NO_ENTRY))
                .then();

    }

    /**
     * Processes the given command and returns the response if any
     *
     * @param event   the message event
     * @param command the command to process
     * @param args    the parameters of the command
     * @return the response to the command
     */
    private Mono<Void> executeCommand(MessageCreateEvent event, Command command, String[] args) {
        return commandExecutor.executeCommand(event, command, args).flatMap(response -> sendReply(event, response)).defaultIfEmpty(CommandResponse.emptyFlat()).elapsed().doOnNext(TupleUtils.consumer(
                (elapsed, response) -> LOGGER.info("{} took {} ms to complete", command.getPrimaryTrigger(), elapsed)))
                .then();
    }
}