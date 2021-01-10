package com.github.mudpitbot.command.commands.music;

import static com.github.mudpitbot.command.util.PermissionsHelper.requireBotPermissions;
import static com.github.mudpitbot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.help.CommandHelpSpec;
import com.github.mudpitbot.command.menu.menus.SearchMenu;
import com.github.mudpitbot.music.GuildMusicManager;
import com.github.mudpitbot.music.TrackScheduler;

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
				.flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> search(scheduler, args));
	}

	private Mono<CommandResponse> search(@NonNull TrackScheduler scheduler,
			@NonNull String[] args) {
		SearchMenu menu = new SearchMenu(scheduler, unsplitArgs(args));
		return CommandResponse.create(menu.createMessage(), menu);
	}

	private String unsplitArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String param : args) {
			sb.append(param).append(" ");
		}
		return sb.toString();
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Searches YouTube for the given terms and returns the top 5 results as choices that can be added to the queue of songs.")
				.addArg("terms", "terms to search YouTube with", false).addExample("something the beatles");
	}
}
