package mubot.command.commands.general;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import mubot.command.Command;
import mubot.command.CommandResponse;
import reactor.core.publisher.Mono;

import java.util.List;

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
