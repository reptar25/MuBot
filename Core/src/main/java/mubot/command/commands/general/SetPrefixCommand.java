package mubot.command.commands.general;

import discord4j.core.event.domain.message.MessageCreateEvent;
import mubot.api.API;
import mubot.command.Command;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static mubot.command.util.PermissionsHelper.requireNotPrivateMessage;

public class SetPrefixCommand extends Command {

    public SetPrefixCommand() {
        super("setprefix");
    }

    @Override
    public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
        return requireNotPrivateMessage(event).flatMap(ignored -> prefix(event, args));
    }

    private Mono<CommandResponse> prefix(MessageCreateEvent event, String[] args) {
        if (args.length >= 1) {
//            DatabaseManager.getInstance().getPrefixCache().addPrefix(event.getGuildId().orElseThrow().asLong(), args[0])
//                    .onErrorResume(error -> Mono.empty()).subscribe();
            String json = "{ \"guild_id\":" + event.getGuildId().orElseThrow().asLong() + ", \"prefix\":\"" + args[0] + "\"}";

            return Mono.just(API.getAPI().getPrefixService().add(json)).then(CommandResponse.create("Set guild command prefix to " + args[0]));
        }
        return getHelp(event);
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Set the command-prefix of this server.")
                .addArg("prefix", "New prefix for bot-commands.", false).addExample("$").addExample("!");
    }

}
