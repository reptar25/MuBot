package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.CommandUtil.requireBotPermissions;
import static com.github.MudPitBot.command.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.menu.SearchMenu;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class SearchCommand extends Command {

	public SearchCommand() {
		super("search");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event)
				.flatMap(channel -> requireBotPermissions(channel, Permission.SPEAK, Permission.MANAGE_MESSAGES).thenReturn(channel))
				.flatMap(channel -> getScheduler(channel)).flatMap(scheduler -> search(event, scheduler, args));
	}

	private Mono<CommandResponse> search(MessageCreateEvent event, TrackScheduler scheduler, String[] args) {
		String param = unsplitArgs(args);
		SearchMenu menu = new SearchMenu(event, scheduler, param);
		return new CommandResponse.Builder().withCreateSpec(menu.createMessage()).withMenu(menu).build();
	}

	private String unsplitArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String param : args) {
			sb.append(param).append(" ");
		}
		return sb.toString();
	}

}
