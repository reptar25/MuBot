package com.github.MudPitBot.command.menu;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.jokeAPI.JokeClient;
import com.github.MudPitBot.jokeAPI.JokeRequest;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JokeMenu extends Menu {

	private static final Logger LOGGER = Loggers.getLogger(JokeMenu.class);
	List<String> categories;
	private final Duration TIMEOUT = Duration.ofMinutes(5L);
	private boolean unsafe = false;

	public JokeMenu(boolean unsafe) {
		this.unsafe = unsafe;
	}

	private Consumer<? super EmbedCreateSpec> createEmbed() {
		return spec -> spec.setDescription(createDescription());
	}

	private String createDescription() {
		StringBuilder sb = new StringBuilder();
		categories = JokeClient.getJokeService().getCategories().block();
		if (unsafe)
			categories.remove("Dark");
		categories.remove("Spooky");
		for (int i = 0; i < categories.size(); i++) {
			String category = categories.get(i);
			sb.append(Emoji.numToEmoji(i + 1)).append(" ").append(category).append("\n");
		}
		return sb.toString();
	}

	@Override
	public Consumer<? super MessageCreateSpec> createMessage() {
		Consumer<? super MessageCreateSpec> spec = s -> s.setContent("**What kind of joke do you want to hear?**")
				.setEmbed(createEmbed());
		return spec;
	}

	private Mono<Void> addReactions() {
		Mono<Void> ret = Mono.empty();
		for (int i = 1; i <= categories.size(); i++) {
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

					int selection = Emoji.unicodeToNum(event.getEmoji().asUnicodeEmoji().get()) - 1;
					String category = categories.get(selection);
					JokeRequest request = new JokeRequest.Builder().safeMode(unsafe).addCategory(category).build();

					JokeClient.getJokeService().getJoke(request).subscribe(jokeLines -> {
						if (jokeLines.size() == 1)
							message.edit(spec -> spec.setContent(jokeLines.get(0)).setEmbed(null)).subscribe();
						else
							message.edit(spec -> spec.setContent(jokeLines.get(0)).setEmbed(null))
									.then(Mono.delay(Duration.ofSeconds(5L)))
									.then(message.edit(spec -> spec
											.setContent(jokeLines.get(0) + "\n" + jokeLines.get(1)).setEmbed(null)))
									.subscribe();
					}, error -> LOGGER.error(error.getMessage(), error));
					return message.removeAllReactions();
				}).onErrorResume(error -> {
					LOGGER.error("Error in reaction listener.", error);
					return Mono.empty();
				}).subscribe();
	}

	@Override
	public void setMessage(Message message) {
		this.message = message;
		addReactions().subscribe(null, error -> LOGGER.error(error.getMessage(), error));
	}

}
