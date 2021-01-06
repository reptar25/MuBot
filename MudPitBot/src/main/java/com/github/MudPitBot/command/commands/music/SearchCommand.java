package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.Permissions.requireBotPermissions;
import static com.github.MudPitBot.command.util.Permissions.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.menu.menus.SearchMenu;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class SearchCommand extends Command {

	public SearchCommand() {
		super("search");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event)
				.flatMap(channel -> requireBotPermissions(channel, Permission.SPEAK, Permission.MANAGE_MESSAGES)
						.thenReturn(channel))
				.flatMap(channel -> getScheduler(channel)).flatMap(scheduler -> search(event, scheduler, args));
	}

	private Mono<CommandResponse> search(MessageCreateEvent event, @NonNull TrackScheduler scheduler,
			@NonNull String[] args) {
		SearchMenu menu = new SearchMenu(event, scheduler, unsplitArgs(args));
		return CommandResponse.create(menu.createMessage(), menu);
	}

	private String unsplitArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String param : args) {
			sb.append(param).append(" ");
		}
		return sb.toString();
	}

}
