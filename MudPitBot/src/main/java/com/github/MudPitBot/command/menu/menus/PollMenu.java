package com.github.MudPitBot.command.menu.menus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

import com.github.MudPitBot.command.menu.Menu;
import com.github.MudPitBot.command.util.Emoji;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Data class for polls.
 */
public final class PollMenu extends Menu {

	/**
	 * Builder for polls. Not really needed now, but can be used to expand polls
	 * easier later on.
	 */
//	public static class Builder {
//
//		private MessageCreateEvent event;
//		private String[] args;
//		private String footer;
//		private String footerURL;
//		private String title;
//		private ArrayList<String> answers = new ArrayList<String>();
//		private String description;
//
//		public Builder(MessageCreateEvent event) {
//			this.event = event;
//		}
//
//		public Poll build() {
//			return new Poll(this);
//		}
//	}

	private static final Logger LOGGER = Loggers.getLogger(PollMenu.class);
	private String[] args;
	private Member member;
	private String footer;
	private String footerURL;
	private String title;
	private ArrayList<String> answers = new ArrayList<String>();
	private String description;

//	private Poll(Builder builder) {
//		// this.event = builder.event;
//		this.footer = builder.footer;
//		this.footerURL = builder.footerURL;
//		this.title = builder.title;
//		this.answers = builder.answers;
//		this.description = builder.description;
//	}

	public PollMenu(String[] args, Member member) {
		this.args = args;
		this.member = member;
		createPoll();
	}

	@Override
	public void setMessage(Message message) {
		super.setMessage(message);
		addReactions();
	}

	private void addReactions() {
		for (int i = 0; i < answers.size(); i++) {
			message.addReaction(Emoji.getUnicodeFromNum(i)).subscribe();
		}
	}

	private void createPoll() {

		createAnswers(args);

		if (answers.isEmpty())
			return;

		createFooter();

		createDescription();
	}

	private void createAnswers(String[] args) {
		// String[] splitCommand = command.split(" \"");

		if (args == null || args.length < 3) {
			LOGGER.info("Not enough arguments for poll command");
			return;
		}

		title = args[0];

		for (int i = 1; i < args.length; i++) {
			answers.add(args[i]);
		}
	}

	private void createFooter() {
		Date date = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("EEEE, MMMM dd");// , 'at' hh:mm a
		String timeStamp = df.format(date);

		StringBuilder sb = new StringBuilder("Poll created by ");
		if (member != null)
			sb.append(member.getUsername());
		sb.append(" on ");
		sb.append(timeStamp);
		this.footer = sb.toString();
		if (member != null)
			this.footerURL = member.getAvatarUrl();
	}

	private void createDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < answers.size(); i++) {
			String answer = answers.get(i);
			sb.append(Emoji.getPlainLetterFromNum(i)).append(" ").append(answer).append("\r\n\n");
		}

		description = sb.toString();
	}

	@Override
	public String toString() {
		return title + " " + description + " " + answers;
	}

	@Override
	public Consumer<? super MessageCreateSpec> createMessage() {
		return spec -> spec.setEmbed(createEmbed()).setContent("**" + title + "**");
	}

	private Consumer<? super EmbedCreateSpec> createEmbed() {
		return embed -> embed.setColor(Color.of(23, 53, 77)).setFooter(footer, footerURL).setDescription(description);
	}

}
