package mubot.command.commands.general;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Permission;
import mubot.command.CommandResponse;
import mubot.command.exceptions.CommandException;
import mubot.command.help.CommandHelpSpec;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Consumer;

public class KickCommand extends RequireMemberAndBotPermissionsCommand {

    public KickCommand() {
        super("kick", Permission.KICK_MEMBERS);
    }

    @Override
    protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args.length >= 2) {
            for (String param : Arrays.copyOfRange(args, 1, args.length)) {
                sb.append(param).append(" ");
            }
        }

        String reason = sb.toString().isEmpty() ? "Reason not specified" : sb.toString().trim();

        return event.getGuild()
                .flatMap(guild -> event.getMessage().getUserMentions().take(1).singleOrEmpty()
                        .switchIfEmpty(Mono.error(new CommandException("User not found on server.")))
                        .flatMap(user -> guild.kick(user.getId(), reason))
                        .then(CommandResponse.create("User " + args[0] + " has been kicked.")))
                .onErrorResume(ClientException.class, error -> Mono.error(new CommandException("Error kicking user")));
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Kick a user from the server but not ban them.")
                .addArg("@user", "The user's mention", false).addArg("reason", "Reason for kicking", true)
                .addExample("@JohnDoe I don't know who this is.");
    }

}
