package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireGuildPermissions;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class AbstractBanCommand extends Command {

	public AbstractBanCommand(String commandTrigger) {
		super(commandTrigger);
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return event.getMessage().getAuthorAsMember()
				.flatMap(member -> requireGuildPermissions(member, Permission.BAN_MEMBERS)
						.flatMap(ignored -> event.getClient().getMemberById(event.getGuildId().get(),
								event.getClient().getSelfId()))
						.flatMap(botMember -> requireGuildPermissions(botMember, Permission.BAN_MEMBERS)))
				.flatMap(ignored -> action(event, args));
	}

	protected abstract Mono<CommandResponse> action(MessageCreateEvent event, String[] args);

}
