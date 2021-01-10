package com.github.mudpitbot.command.menu.menus;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import com.github.mudpitbot.command.menu.SingleChoiceActionMenu;
import com.github.mudpitbot.command.util.EmojiHelper;
import com.github.mudpitbot.jokeapi.JokeClient;
import com.github.mudpitbot.jokeapi.JokeRequest;
import com.github.mudpitbot.jokeapi.util.JokeEnums.BlacklistFlag;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JokeMenu extends SingleChoiceActionMenu {

	private static final Logger LOGGER = Loggers.getLogger(JokeMenu.class);
	private List<String> categories;
	private boolean unsafe = false;
	private String selectedCategory;

	public JokeMenu(boolean unsafe) {
		this.unsafe = unsafe;
	}

	public JokeMenu(boolean unsafe, String category) {
		this.unsafe = unsafe;
		this.selectedCategory = category;
	}

	private Consumer<? super EmbedCreateSpec> createEmbed() {
		return spec -> spec.setDescription(createDescription());
	}

	private String createDescription() {

		return JokeClient.getJokeService().getCategories().map(categories -> {
			StringBuilder sb = new StringBuilder();
			this.categories = categories;
			// there are no safe dark jokes, so remove it if safe mode is on
			if (!unsafe)
				categories.remove("Dark");
			// spooky only has 1 joke, so just remove it
			categories.remove("Spooky");
			for (int i = 0; i < categories.size(); i++) {
				String category = categories.get(i);
				sb.append(EmojiHelper.numToEmoji(i + 1)).append(" ").append(category).append("\n");
			}
			return sb.toString();
		}).block();
	}

	@Override
	public Consumer<? super MessageCreateSpec> createMessage() {
		Consumer<? super MessageCreateSpec> spec = null;
		if (selectedCategory == null) {
			spec = s -> s.setContent("**What kind of joke do you want to hear?**").setEmbed(createEmbed());
		} else {
			spec = s -> s.setContent("Loading joke...");
		}
		return spec;
	}

	@Override
	protected Mono<Void> loadSelection(ReactionAddEvent event) {
		int selection = EmojiHelper.unicodeToNum(event.getEmoji().asUnicodeEmoji().get()) - 1;
		return loadJoke(categories.get(selection));
	}

	private Mono<Void> loadJoke(String category) {
		// no racist or sexist jokes allowed
		JokeRequest request = new JokeRequest.Builder().safeMode(!unsafe).addBlacklistFlag(BlacklistFlag.RACIST)
				.addBlacklistFlag(BlacklistFlag.SEXIST).addCategory(category).build();
		return JokeClient.getJokeService().getJoke(request).flatMap(jokeLines -> {
			if (jokeLines.size() == 1)
				return message.edit(spec -> spec.setContent(jokeLines.get(0)).setEmbed(null)).then();
			else
				return message.edit(spec -> spec.setContent(jokeLines.get(0)).setEmbed(null))
						.then(Mono.delay(Duration.ofSeconds(5L)))
						.then(message.edit(
								spec -> spec.setContent(jokeLines.get(0) + "\n" + jokeLines.get(1)).setEmbed(null)))
						.then();
		});

	}

	@Override
	public void setMessage(Message message) {
		this.message = message;
		checkSelectedCategory().subscribe(null, error -> LOGGER.error(error.getMessage(), error));

	}

	private Mono<Void> checkSelectedCategory() {
		if (selectedCategory == null)
			return addReactions().then(addReactionListener()).then();
		else
			return loadJoke(selectedCategory);
	}

	@Override
	protected Mono<Void> addReactions() {
		Mono<Void> ret = Mono.empty();
		for (int i = 1; i <= categories.size(); i++) {
			ret = ret.then(message.addReaction(EmojiHelper.numToUnicode(i)));
		}

		return ret;
	}

}
