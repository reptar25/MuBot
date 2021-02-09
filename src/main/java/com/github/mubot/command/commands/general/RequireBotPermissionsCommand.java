package com.github.mubot.command.commands.general;

import static com.github.mubot.command.util.PermissionsHelper.requireBotGuildPermissions;
import static com.github.mubot.command.util.PermissionsHelper.requireNotPrivateMessage;

import java.util.List;

import com.github.mubot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class RequireBotPermissionsCommand extends RequirePermissionsCommand {

	public RequireBotPermissionsCommand(String commandTrigger, Permission... permissions) {
		super(commandTrigger, permissions);
	}

	public RequireBotPermissionsCommand(String commandTrigger, List<String> aliases, Permission[] permissions) {
		super(commandTrigger, aliases, permissions);
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireNotPrivateMessage(event).flatMap(ignore -> requireBotGuildPermissions(event, this.permissions))
				.flatMap(ignore -> action(event, args));
	}

}
