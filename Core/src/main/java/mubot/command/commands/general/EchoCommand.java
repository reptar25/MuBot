package mubot.command.commands.general;

import discord4j.core.event.domain.message.MessageCreateEvent;
import mubot.command.Command;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class EchoCommand extends Command {

    public EchoCommand() {
        super("echo");
    }

    @Override
    public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
        return echo();
    }

    /**
     * Bot replies with a simple echo message
     *
     * @return "echo!"
     */
    public Mono<CommandResponse> echo() {
        return CommandResponse.create("echo!");
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Bot replies with a simple echo message.");
    }

}
