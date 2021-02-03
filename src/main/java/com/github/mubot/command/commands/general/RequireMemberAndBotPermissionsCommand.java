package com.github.mubot.command.commands.general;

import static com.github.mubot.command.util.PermissionsHelper.requireGuildPermissions;
import static com.github.mubot.command.util.PermissionsHelper.requireNotPrivateMessage;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class RequireMemberAndBotPermissionsCommand extends Command {

	private Permission[] permissions;

	public RequireMemberAndBotPermissionsCommand(String commandTrigger, Permission... permissions) {
		super(commandTrigger);
		this.permissions = permissions;
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireNotPrivateMessage(event).flatMap(ignored -> event.getMessage().getAuthorAsMember()
				.flatMap(member -> requireGuildPermissions(member, this.permissions)
						.flatMap(ignore -> event.getClient().getMemberById(event.getGuildId().get(),
								event.getClient().getSelfId()))
						.flatMap(botMember -> requireGuildPermissions(botMember, this.permissions)))
				.flatMap(ignore -> action(event, args)));
	}

	protected abstract Mono<CommandResponse> action(MessageCreateEvent event, String[] args);

}
