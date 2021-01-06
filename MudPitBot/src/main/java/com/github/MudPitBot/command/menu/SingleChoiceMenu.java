package com.github.MudPitBot.command.menu;

import java.time.Duration;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public abstract class SingleChoiceMenu extends Menu {

	private static final Logger LOGGER = Loggers.getLogger(Menu.class);
	private final Duration TIMEOUT = Duration.ofMinutes(5L);

	/**
	 * Adds a ReactionAddEvent listener to the menu to allow some action to be
	 * performed when a non-bot member adds a reaction to this menu's message
	 * 
	 * @return
	 */
	protected Flux<Void> addReactionListener() {
		return message.getClient().on(ReactionAddEvent.class)
				.filter(e -> !e.getMember().map(Member::isBot).orElse(false))
				.filter(e -> e.getMessageId().asLong() == message.getId().asLong())
				.filter(e -> !e.getEmoji().asUnicodeEmoji().isEmpty()).take(TIMEOUT).doOnTerminate(() -> {
					message.delete("Timeout").subscribe();
				}).flatMap(event -> {
					return loadSelection(event);
				}).onErrorResume(error -> {
					LOGGER.error("Error in reaction listener.", error);
					return Mono.empty();
				});
	}

	@Override
	public void setMessage(Message message) {
		super.setMessage(message);
		addReactions().onErrorResume(error -> {
			LOGGER.error("Error adding reactions.", error);
			return Mono.empty();
		}).subscribe();
	}

	/**
	 * Adds reactions to the message as choices for the user to select
	 * 
	 * @return
	 */
	protected abstract Mono<Void> addReactions();

	/**
	 * Called when a reaction is added to this message, this is where the "action"
	 * of the menu should happen
	 * 
	 * @param event the ReactionAddEvent event
	 * @return
	 */
	protected abstract Mono<Void> loadSelection(ReactionAddEvent event);
}
