package com.github.mudpitbot.command.menu.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.mudpitbot.command.menu.SingleChoiceActionMenu;
import com.github.mudpitbot.command.util.CommandUtil;
import com.github.mudpitbot.command.util.EmojiHelper;
import com.github.mudpitbot.music.GuildMusicManager;
import com.github.mudpitbot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class SearchMenu extends SingleChoiceActionMenu implements AudioLoadResultHandler {

	private static final Logger LOGGER = Loggers.getLogger(SearchMenu.class);
	private final int RESULT_LENGTH = 5;
	private final String SEARCH_PREFIX = "ytsearch:";

	private List<AudioTrack> results;
	private TrackScheduler scheduler;
	private String identifier;

	public SearchMenu(TrackScheduler scheduler, String identifier) {
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
			sb.append(EmojiHelper.numToEmoji(i + 1)).append(" [").append(CommandUtil.convertMillisToTime(track.getDuration()))
					.append("] ").append(CommandUtil.trackInfo(track)).append("\n");
		}
		return sb.toString();
	}

	@Override
	public Consumer<? super MessageCreateSpec> createMessage() {
		Consumer<? super MessageCreateSpec> spec = s -> s.setContent("Searching...");
		return spec;
	}

	@Override
	protected Mono<Void> addReactions() {
		Mono<Void> ret = Mono.empty();
		for (int i = 1; i <= results.size(); i++) {
			ret = ret.then(message.addReaction(EmojiHelper.numToUnicode(i)));
		}

		ret = ret.then(message.addReaction(EmojiHelper.RED_X_UNICODE));

		return ret;
	}

	@Override
	protected Mono<Void> loadSelection(ReactionAddEvent event) {
		if (event.getEmoji().asUnicodeEmoji().get().equals(EmojiHelper.RED_X_UNICODE)) {
			return message.delete();
		}
		int selection = EmojiHelper.unicodeToNum(event.getEmoji().asUnicodeEmoji().get());
		if (selection < 1)
			return Mono.empty();
		LOGGER.info("Selected track: " + selection);
		String queueResponse = scheduler.queue(results.get(selection - 1));
		return message.edit(spec -> spec.setContent(queueResponse).setEmbed(null)).then().onErrorResume(error -> {
			LOGGER.error("Error selecting track.", error);
			return Mono.empty();
		});
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
			createResultsMessage().then(addReactions()).then(addReactionListener()).then().subscribe(null,
					error -> LOGGER.error(error.getMessage()));
		}
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		LOGGER.info("Search loaded playlist: " + playlist.getTracks().get(0).getInfo().title);
		results = playlist.getTracks().stream().limit(RESULT_LENGTH).collect(Collectors.toList());

		if (message != null) {
			createResultsMessage().then(addReactions()).then(addReactionListener()).subscribe(null,
					error -> LOGGER.error(error.getMessage()));
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
