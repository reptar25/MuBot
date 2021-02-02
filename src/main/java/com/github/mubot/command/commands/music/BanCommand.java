package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireGuildPermissions;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.exceptions.CommandException;
import com.github.mubot.command.help.CommandHelpSpec;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class BanCommand extends Command {

	public BanCommand() {
		super("ban");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return event.getMessage().getAuthorAsMember()
				.flatMap(member -> requireGuildPermissions(member, Permission.BAN_MEMBERS)
						.flatMap(ignored -> event.getClient().getMemberById(event.getGuildId().get(),
								event.getClient().getSelfId()))
						.flatMap(botMember -> requireGuildPermissions(botMember, Permission.BAN_MEMBERS)))
				.flatMap(ignored -> ban(event, args));

	}

	private Mono<CommandResponse> ban(MessageCreateEvent event, String[] args) {

		StringBuilder sb = new StringBuilder();
		if (args.length >= 2) {
			for (String param : Arrays.copyOfRange(args, 1, args.length)) {
				sb.append(param).append(" ");
			}
		}

		String reason = sb.toString().isEmpty() ? "Reason not specified" : sb.toString().trim();

		return event.getMessage().getGuild()
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
