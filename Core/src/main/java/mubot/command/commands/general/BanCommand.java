package mubot.command.commands.general;

import java.util.Arrays;
import java.util.function.Consumer;

import mubot.command.CommandResponse;
import mubot.command.exceptions.CommandException;
import mubot.command.help.CommandHelpSpec;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class BanCommand extends RequireMemberAndBotPermissionsCommand {

	public BanCommand() {
		super("ban", Permission.BAN_MEMBERS);
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
				.flatMap(guild -> event.getMessage().getUserMentions().take(1)
						.switchIfEmpty(Mono.error(new CommandException("User not found on server.")))
						.flatMap(user -> guild.ban(user.getId(), s -> s.setReason(reason).setDeleteMessageDays(7)))
						.then(CommandResponse.create("User " + args[0] + " has been banned.")))
				.onErrorResume(ClientException.class, error -> Mono.error(new CommandException("Error banning user")));
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Ban a user and delete their messages from the last 7 days.")
				.addArg("@user", "The user's mention", false).addArg("reason", "Reason for banning", true)
				.addExample("@JohnDoe I don't know who this is.");
	}

}
