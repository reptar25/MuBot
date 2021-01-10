package com.github.mudpitbot.command.menu.menus;

import static com.github.mudpitbot.command.util.CommandUtil.DEFAULT_EMBED_COLOR;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.mudpitbot.command.menu.Menu;
import com.github.mudpitbot.command.util.EmojiHelper;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import reactor.util.Logger;
import reactor.util.Loggers;

public final class PollMenu extends Menu {

	private static final Logger LOGGER = Loggers.getLogger(PollMenu.class);
	private final int MAX_ANSWERS = 11;
	private String[] args;
	private Member member;
	private String authorName;
	private String authorIcon;
	private String title;
	private ArrayList<String> answers = new ArrayList<String>();
	private String description;

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
			message.addReaction(EmojiHelper.getUnicodeFromNum(i)).subscribe();
		}
	}

	private void createPoll() {

		setAnswers(args);

		if (answers.isEmpty())
			return;

		setAuthor();

		setDescription();
	}

	private void setAnswers(String[] args) {
		if (args == null || args.length < 3) {
			LOGGER.info("Not enough arguments for poll command");
			return;
		}
		// only allow of 1 question and 10 answers as arguments
		List<String> arguments = Arrays.stream(args).limit(MAX_ANSWERS).collect(Collectors.toList());
		title = arguments.get(0);
		for (int i = 1; i < arguments.size(); i++) {
			answers.add(arguments.get(i));
		}
	}

	private void setAuthor() {
		if (member != null) {
			this.authorName = member.getUsername();
			this.authorIcon = member.getAvatarUrl();
		}
	}

//	private void createFooter() {
//		Date date = new Date(System.currentTimeMillis());
//		DateFormat df = new SimpleDateFormat("EEEE, MMMM dd");// , 'at' hh:mm a
//		String timeStamp = df.format(date);
//
//		StringBuilder sb = new StringBuilder("Poll created by ");
//		if (member != null)
//			sb.append(member.getUsername());
//		sb.append(" on ");
//		sb.append(timeStamp);
//		this.author = sb.toString();
//		if (member != null)
//			this.authorIcon = member.getAvatarUrl();
//	}

	private void setDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < answers.size(); i++) {
			String answer = answers.get(i);
			sb.append(EmojiHelper.getPlainLetterFromNum(i)).append(" ").append(answer).append("\r\n\n");
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
		return embed -> embed.setColor(DEFAULT_EMBED_COLOR).setFooter("Poll created by " + authorName, authorIcon)
				.setTimestamp(Instant.now()).setDescription(description);
	}

}
