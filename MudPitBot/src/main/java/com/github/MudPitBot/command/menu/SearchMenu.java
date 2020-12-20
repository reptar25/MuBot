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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class SearchMenu extends Menu implements AudioLoadResultHandler {

	private static final Logger LOGGER = Loggers.getLogger(SearchMenu.class);
	private static final int RETRY_AMOUNT = Integer.MAX_VALUE - 1;
	private final Duration TIMEOUT = Duration.ofMinutes(5l);
	private final int RESULT_LENGTH = 5;
	private final String SEARCH_PREFIX = "ytsearch:";

	static SearchSubscription sub;
	private List<AudioTrack> results;
	private TrackScheduler scheduler;
	private String identifier;

	public SearchMenu(MessageCreateEvent event, TrackScheduler scheduler, String identifier) {
		this.scheduler = scheduler;
		this.identifier = identifier;
		GuildMusicManager.getPlayerManager().loadItemOrdered(event.getGuild(), SEARCH_PREFIX + identifier, this);
	}

	private void createResultsMessage() {
		Mono.justOrEmpty(results).repeatWhenEmpty(RETRY_AMOUNT, Flux::repeat)
				.flatMap(ignored -> Mono.justOrEmpty(message).flatMap(message -> message.edit(
						spec -> spec.setContent("Search resutls for **" + identifier + "**").setEmbed(createEmbed()))))
				.subscribe();

//		Mono.justOrEmpty(message).repeatWhenEmpty(RETRY_AMOUNT, Flux::repeat)
//				.map(message -> message.edit(
//						spec -> spec.setContent("Search resutls for **" + identifier + "**").setEmbed(createEmbed()))
//						.subscribe())
//				.subscribe();
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

//	@Override
//	public void setMessage(Message message) {
//		super.setMessage(message);
//
//
//	}

	private void addReactions() {
		for (int i = 1; i <= results.size(); i++) {
			message.addReaction(Emoji.numToUnicode(i)).subscribe();
		}

		addReactionListener();
	}

	private void addReactionListener() {
		if (sub != null)
			sub.dispose();
		sub = new SearchSubscription(message);
		sub.subscription = message.getClient().on(ReactionAddEvent.class)
				.filter(e -> e.getMessageId() != message.getId())
				.filter(e -> !e.getMember().map(Member::isBot).orElse(false)).take(TIMEOUT)
				.doOnTerminate(() -> message.removeAllReactions().subscribe()).subscribe(event -> {

					if (event.getEmoji().asUnicodeEmoji().isEmpty())
						return;

//					message.removeReaction(event.getEmoji(), event.getUserId()).subscribe(null,
//							error -> LOGGER.error(error.getMessage()));
					message.removeAllReactions().subscribe();

					int selection = Emoji.unicodeToNum(event.getEmoji().asUnicodeEmoji().get());
					loadSelection(selection);

				}, error -> LOGGER.error(error.getMessage()));
	}

	private void loadSelection(int selection) {
		if (selection < 1)
			return;
		LOGGER.info("Selected track: " + selection);
		String queueResponse = scheduler.queue(results.get(selection - 1));
		message.edit(spec -> spec.setContent(queueResponse).setEmbed(null)).subscribe();
		sub.subscription.dispose();
		sub = null;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		LOGGER.info("Search loaded track: " + track.getInfo().title);
		String queueResponse = scheduler.queue(track);
		message.edit(spec -> spec.setContent(queueResponse)).subscribe();
		results = new ArrayList<AudioTrack>();
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		LOGGER.info("Search loaded playlist: " + playlist.getTracks().get(0).getInfo().title);
		results = playlist.getTracks().stream().limit(RESULT_LENGTH).collect(Collectors.toList());

		createResultsMessage();
		addReactions();
		LOGGER.info("RESULTS SET " + results);
	}

	@Override
	public void noMatches() {
		LOGGER.info("No results found");
		message.edit(spec -> spec.setContent("No results found for " + identifier)).subscribe();
		results = new ArrayList<AudioTrack>();
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		LOGGER.info("Something went wrong: " + exception.getMessage());
		message.edit(spec -> spec.setContent("Something went wrong: " + exception.getMessage())).subscribe();
		results = new ArrayList<AudioTrack>();
	}

	private class SearchSubscription {
		public Disposable subscription;
		private Message message;

		public SearchSubscription(Message message) {
			this.message = message;
		};

		public void dispose() {
			message.removeAllReactions().subscribe();
			// message.delete().subscribe(null, error -> LOGGER.error(error.getMessage(),
			// error));
			subscription.dispose();
		}
	}

}
