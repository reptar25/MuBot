package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireGuildPermissions;

import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.exceptions.CommandException;
import com.github.mubot.command.help.CommandHelpSpec;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class UnbanCommand extends Command {

	public UnbanCommand() {
		super("unban");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return event.getMessage().getAuthorAsMember()
				.flatMap(member -> requireGuildPermissions(false, member, Permission.BAN_MEMBERS)
						.flatMap(ignored -> event.getClient().getMemberById(event.getGuildId().get(),
								event.getClient().getSelfId()))
						.flatMap(botMember -> requireGuildPermissions(true, botMember, Permission.BAN_MEMBERS)))
				.flatMap(ignored -> unban(event, args));

	}

	private Mono<CommandResponse> unban(MessageCreateEvent event, String[] args) {
		return event.getGuild().flatMapMany(guild -> guild.getBans().map(ban -> {
			if (ban.getUser().getMention().equals(args[0]))
				return ban.getUser();
			return null;
		}).switchIfEmpty(Mono.error(new CommandException("User not found in ban list.")))
				.flatMap(user -> guild.unban(user.getId())))
				.then(CommandResponse.create("User " + args[0] + " has been unbanned.")).onErrorResume(
						ClientException.class, error -> Mono.error(new CommandException("Error unbanning user")));
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Unban a user.").addArg("@user", "The user's mention", false);
	}

}
