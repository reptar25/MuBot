package com.github.mubot.command.menu.menus;

import java.util.function.Consumer;

import com.github.mubot.command.menu.ChoiceActionMenu;
import com.github.mubot.command.util.EmojiHelper;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public class Paginator extends ChoiceActionMenu {

	private int itemsPerPage;
	private String content;
	private String[] entries;

	private int currentPageNum = 1;
	private int totalPages;
	private String description;

	private Paginator(Builder b) {
		this.itemsPerPage = b.itemsPerPage;
		this.content = b.content;
		this.entries = b.entries;

		this.totalPages = (int) Math.ceil((double) entries.length / itemsPerPage);
	}

	private Consumer<? super EmbedCreateSpec> createEmbed() {
		buildDescription();
		return embed -> embed.setDescription(description).setFooter("Page " + currentPageNum + "/" + totalPages, null);
	}

	@Override
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

	public static class Builder {
		private int itemsPerPage = 10;
		private String content = "";
		private String[] entries;

		public Builder withItemsPerPage(int itemsPerPage) {
			this.itemsPerPage = itemsPerPage;
			return this;
		}

		public Builder withMessageContent(String content) {
			this.content = content;
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

	@Override
	protected Mono<Void> addReactions() {
		return message.addReaction(EmojiHelper.LEFT_ARROW).then(message.addReaction(EmojiHelper.RIGHT_ARROW));
	}

	@Override
	protected Mono<Void> loadSelection(ReactionAddEvent event) {
		if (event.getEmoji().asUnicodeEmoji().get().equals(EmojiHelper.LEFT_ARROW) && currentPageNum > 1) {
			currentPageNum--;
		} else if (event.getEmoji().asUnicodeEmoji().get().equals(EmojiHelper.RIGHT_ARROW)
				&& currentPageNum < totalPages) {
			currentPageNum++;
		}
		return message.removeReaction(event.getEmoji(), event.getUserId())
				.then(message.edit(edit -> edit.setEmbed(createEmbed()))).then();
	}

}
