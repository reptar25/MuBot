package com.github.MudPitBot.command.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	private String[] params;
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

	public Poll(String[] params, Member member) {
		this.params = params;
		this.member = member;
		createPoll();
	}

	public void addReactions(Message message) {
		for (int i = 0; i < getAnswers().size(); i++) {
			message.addReaction(Emoji.getUnicodeFromNum(i)).subscribe();
		}
	}

	private void createPoll() {

		createAnswers(params);

		if (answers.isEmpty())
			return;

		createFooter();

		createDescription();
	}

	private void createAnswers(String[] params) {
		// String[] splitCommand = command.split(" \"");

		if (params == null || params.length < 3) {
			LOGGER.info("Not enough arguments for poll command");
			return;
		}

		title = params[0];

		for (int i = 1; i < params.length; i++) {
			answers.add(params[i]);
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
