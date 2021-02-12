package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireBotChannelPermissions;
import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.List;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.music.GuildMusicManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class MusicPermissionCommand extends MusicCommand {
	private Permission[] permissions;

	public MusicPermissionCommand(String commandTrigger, Permission... permissions) {
		super(commandTrigger);
		this.permissions = permissions;
	}

	public MusicPermissionCommand(String commandTrigger, List<String> aliases, Permission... permissions) {
		super(commandTrigger, aliases);
		this.permissions = permissions;
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event)
				.flatMap(channel -> requireBotChannelPermissions(channel, this.permissions).thenReturn(channel))
				.flatMap(channel -> GuildMusicManager.getScheduler(channel)
						.flatMap(scheduler -> action(event, args, scheduler, channel)));
	}
}
