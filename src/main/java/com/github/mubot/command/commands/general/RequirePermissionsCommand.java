package com.github.mubot.command.commands.general;

import java.util.List;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class RequirePermissionsCommand extends Command {

	protected Permission[] permissions;

	public RequirePermissionsCommand(String commandTrigger, Permission... permissions) {
		super(commandTrigger);
		this.permissions = permissions;
	}

	public RequirePermissionsCommand(String commandTrigger, List<String> aliases, Permission... permissions) {
		super(commandTrigger, aliases);
		this.permissions = permissions;
	}

	protected abstract Mono<CommandResponse> action(MessageCreateEvent event, String[] args);
}
