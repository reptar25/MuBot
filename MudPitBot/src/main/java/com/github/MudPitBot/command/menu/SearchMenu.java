package com.github.MudPitBot.command.menu;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.MudPitBot.command.CommandUtil;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class SearchMenu extends Menu implements AudioLoadResultHandler {

	private static final Logger LOGGER = Loggers.getLogger(SearchMenu.class);
	private final Duration TIMEOUT = Duration.ofMinutes(5L);
	private final int RESULT_LENGTH = 5;
	private final String SEARCH_PREFIX = "ytsearch:";

	private List<AudioTrack> results;
	private TrackScheduler scheduler;
	private String identifier;

	public SearchMenu(MessageCreateEvent event, TrackScheduler scheduler, String identifier) {
		this.scheduler = scheduler;
		this.identifier = identifier;

	}

	private Mono<Message> createResultsMessage() {
		return Mono.justOrEmpty(message).flatMap(message -> message
				.edit(spec -> spec.setContent("Search resutls for **" + identifier + "**").setEmbed(createEmbed())));
	}

	private Consumer<? super EmbedCreateSpec> createEmbed() {
		return spec -> spec.setDescription(createDescription());
	}

	private String createDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < results.size(); i++) {
			AudioTrack track = results.get(i);
			sb.append(Emoji.numToEmoji(i + 1)).append(" [").append(CommandUtil.convertMillisToTime(track.getDuration()))
					.append("] ").append(CommandUtil.trackInfoString(track)).append("\n");
		}
		return sb.toString();
	}

	@Override
	public Consumer<? super MessageCreateSpec> createMessage() {
		Consumer<? super MessageCreateSpec> spec = s -> s.setContent("Searching...");
		return spec;
	}

	private Mono<Void> addReactions() {
		Mono<Void> ret = Mono.empty();
		for (int i = 1; i <= results.size(); i++) {
			LOGGER.info("Adding Reaction " + i);
			ret = ret.then(message.addReaction(Emoji.numToUnicode(i)));
		}

		addReactionListener();
		return ret;
	}

	private void addReactionListener() {
		message.getClient().on(ReactionAddEvent.class).filter(e -> !e.getMember().map(Member::isBot).orElse(false))
				.filter(e -> e.getMessageId().asLong() == message.getId().asLong())
				.filter(e -> !e.getEmoji().asUnicodeEmoji().isEmpty()).take(TIMEOUT)
				.doOnTerminate(() -> message.removeAllReactions().subscribe()).flatMap(event -> {

					int selection = Emoji.unicodeToNum(event.getEmoji().asUnicodeEmoji().get());
					loadSelection(selection);
					return message.removeAllReactions();

				}).onErrorResume(error -> {
					LOGGER.error("Error in reaction listener.", error);
					return Mono.empty();
				}).subscribe();
	}

	private void loadSelection(int selection) {
		if (selection < 1)
			return;
		LOGGER.info("Selected track: " + selection);
		String queueResponse = scheduler.queue(results.get(selection - 1));
		message.edit(spec -> spec.setContent(queueResponse).setEmbed(null)).subscribe();
	}

	@Override
	public void setMessage(Message message) {
		this.message = message;
		GuildMusicManager.getPlayerManager().loadItemOrdered(message.getGuild(), SEARCH_PREFIX + identifier, this);
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		LOGGER.info("Search loaded track: " + track.getInfo().title);
		results = new ArrayList<AudioTrack>();
		results.add(track);

		if (message != null) {
			createResultsMessage().then(addReactions()).subscribe(null, error -> LOGGER.error(error.getMessage()));
		}
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		LOGGER.info("Search loaded playlist: " + playlist.getTracks().get(0).getInfo().title);
		results = playlist.getTracks().stream().limit(RESULT_LENGTH).collect(Collectors.toList());

		if (message != null) {
			createResultsMessage().then(addReactions()).subscribe(null, error -> LOGGER.error(error.getMessage()));
		}
	}

	@Override
	public void noMatches() {
		LOGGER.info("No results found");
		results = new ArrayList<AudioTrack>();
		if (message != null)
			message.edit(spec -> spec.setContent("No results found for " + identifier)).subscribe();
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		LOGGER.info("Something went wrong: " + exception.getMessage());
		message.edit(spec -> spec.setContent("Something went wrong: " + exception.getMessage())).subscribe();
	}

}
