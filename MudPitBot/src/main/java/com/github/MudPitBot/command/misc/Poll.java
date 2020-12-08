package com.github.MudPitBot.command.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.github.MudPitBot.util.Emoji;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Data class for polls.
 */
public final class Poll {

	/**
	 * Builder for polls. Not really needed now, but can be used to expand polls
	 * easier later on.
	 */
//	public static class Builder {
//
//		private MessageCreateEvent event;
//		private String[] params;
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

	private static final Logger LOGGER = Loggers.getLogger(Poll.class);
	private MessageCreateEvent event;
	private String[] params;
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

	public Poll(MessageCreateEvent event) {
		this.event = event;
		createPoll();
	}

	public void addReactions(Message message) {
		for (int i = 0; i < getAnswers().size(); i++) {
			message.addReaction(Emoji.getUnicodeFromNum(i)).block();
		}
	}

	private void createPoll() {

		if (event == null || event.getMessage() == null) {
			return;
		}

		setParams();

		createAnswers(params);

		if (answers.isEmpty())
			return;

		createFooter();

		createDescription();
	}

	private void setParams() {
		if (event != null && event.getMessage() != null && event.getMessage().getContent() != null) {
			String command = event.getMessage().getContent().replace("!poll", "");
			params = command.split(" \"");
		}
	}

	private void createAnswers(String[] params) {
		// String[] splitCommand = command.split(" \"");

		if (params == null || params.length < 3) {
			LOGGER.info("Not enough arguments for poll command");
			return;
		}

		title = params[1].replaceAll("\"", "");

		for (int i = 2; i < params.length; i++) {
			answers.add(params[i].replaceAll("\"", ""));
		}
	}

	private void createFooter() {
		Date date = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("EEEE, MMMM dd");// , 'at' hh:mm a
		String timeStamp = df.format(date);

		StringBuilder sb = new StringBuilder("Poll created by ");
		sb.append(event.getMessage().getUserData().username());
		sb.append(" on ");
		sb.append(timeStamp);
		this.footer = sb.toString();
		Member member = event.getMember().orElse(null);
		if (member != null)
			this.footerURL = member.getAvatarUrl();
	}

	private void createDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < answers.size(); i++) {
			String answer = answers.get(i);
			sb.append(Emoji.getPlainFromNum(i)).append(" ").append(answer).append("\r\n\n");
		}

		description = sb.toString();
	}

	public String getFooter() {
		return footer;
	}

	public String getTitle() {
		return title;
	}

	public ArrayList<String> getAnswers() {
		return answers;
	}

	public String getDescription() {
		return description;
	}

	public String getFooterURL() {
		return footerURL;
	}

	@Override
	public String toString() {
		return title + " " + description + " " + answers;
	}

}