package com.github.MudPitBot.command.util;

import java.time.Duration;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Paginator {

	private static final Logger LOGGER = Loggers.getLogger(Paginator.class);

	int currentPageNum = 1;
	int totalPages;
	int itemsPerPage;
	String[] entries;
	MessageChannel channel;

	String content;
	String description;

	private Paginator(Builder b) {
		this.itemsPerPage = b.itemsPerPage;
		this.content = b.content;
		this.description = b.description;
		this.entries = b.entries;

		this.totalPages = (int) Math.ceil((double) entries.length / itemsPerPage);
	}

	private Consumer<? super EmbedCreateSpec> createEmbed() {
		buildDescription();
		Consumer<? super EmbedCreateSpec> spec = embed -> embed.setDescription(description)
				.setFooter("Page " + currentPageNum + "/" + totalPages, null).setColor(Color.of(23, 53, 77));

		return spec;
	}

	public Consumer<? super MessageCreateSpec> createMessage() {
		return spec -> spec.setEmbed(createEmbed()).setContent(content);
	}

	private void buildDescription() {
		int start = (currentPageNum - 1) * itemsPerPage;
		int end = entries.length < currentPageNum * itemsPerPage ? entries.length : currentPageNum * itemsPerPage;

		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++) {
			sb.append(entries[i]);
		}
		description = sb.toString();
	}

	public void addReactions(Message message) {
		message.addReaction(Emoji.LEFT_REACTION).subscribe();
		message.addReaction(Emoji.RIGHT_REACTION).subscribe();

		addReactionListener(message);
	}

	private void addReactionListener(Message message) {
		message.getClient().on(ReactionAddEvent.class).filter(e -> e.getMessageId() != message.getId())
				.filter(e -> !e.getMember().map(Member::isBot).orElse(false)).take(Duration.ofMinutes(1))
				.doOnTerminate(() -> message.removeAllReactions().subscribe()).subscribe(event -> {

					if (event.getEmoji().asUnicodeEmoji().isEmpty())
						return;

					if (event.getEmoji().asUnicodeEmoji().get().equals(Emoji.LEFT_REACTION)) {
						if (currentPageNum > 1)
							currentPageNum--;
					} else if (event.getEmoji().asUnicodeEmoji().get().equals(Emoji.RIGHT_REACTION)) {
						if (currentPageNum < totalPages)
							currentPageNum++;
					}
					message.removeReaction(event.getEmoji(), event.getUserId()).subscribe(null,
							error -> LOGGER.error(error.getMessage()));

					message.edit(edit -> edit.setEmbed(createEmbed())).subscribe();

				}, error -> LOGGER.error(error.getMessage()));
	}

	public static class Builder {
		private int itemsPerPage = 10;
		String content = "";
		String description = "";
		String[] entries;

		public Builder withItemsPerPage(int itemsPerPage) {
			this.itemsPerPage = itemsPerPage;
			return this;
		}

		public Builder withMessageContent(String content) {
			this.content = content;
			return this;
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder withEntries(String[] entries) {
			this.entries = entries;
			return this;
		}

		public Paginator build() {
			return new Paginator(this);
		}
	}

}
