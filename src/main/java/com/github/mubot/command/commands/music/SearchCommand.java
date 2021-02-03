package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireBotChannelPermissions;
import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.menu.menus.SearchMenu;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class SearchCommand extends MusicCommand {

	public SearchCommand() {
		super("search", Arrays.asList("find"));
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event)
				.flatMap(channel -> requireBotChannelPermissions(channel, Permission.SPEAK, Permission.MANAGE_MESSAGES)
						.thenReturn(channel))
				.flatMap(channel -> GuildMusicManager.getScheduler(channel)
						.flatMap(scheduler -> action(event, args, scheduler, channel)));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return search(args, scheduler);
	}

	private Mono<CommandResponse> search(@NonNull String[] args, @NonNull TrackScheduler scheduler) {
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
