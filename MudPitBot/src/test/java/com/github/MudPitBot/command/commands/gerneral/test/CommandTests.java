package com.github.MudPitBot.command.commands.gerneral.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.commands.general.CommandsCommand;
import com.github.MudPitBot.command.commands.general.EchoCommand;
import com.github.MudPitBot.command.commands.general.PollCommand;
import com.github.MudPitBot.command.commands.general.RollCommand;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

class CommandTests {

	@Mock
	static GatewayDiscordClient mockClient = mock(GatewayDiscordClient.class);

	@Mock
	MessageCreateEvent mockEvent = mock(MessageCreateEvent.class);

	@Mock
	Message mockMessage = mock(Message.class);

	@Mock
	MessageChannel mockChannel = mock(MessageChannel.class);

	@Mock
	Guild mockGuild = mock(Guild.class);

	@Mock
	Mono<MessageChannel> monoChannel = mock(Mono.class);

	@Mock
	Mono<Guild> monoGuild = mock(Mono.class);

	@Mock
	Mono<Message> monoMessage = mock(Mono.class);

	@Mock
	Snowflake mockSnowflake = mock(Snowflake.class);

	@Mock
	Member mockMember = mock(Member.class);

	TrackScheduler mockScheduler = GuildMusicManager.createTrackScheduler(mockSnowflake.asLong());

	@Test
	void echoTest() {
		String[] args = null;
		CommandResponse response = new EchoCommand().execute(mockEvent, args).block();

		assertEquals(response.getContent(), "echo!");
	}

	@Test
	void rollTest() {
		String[] args = { "1d20" };
		CommandResponse response = new RollCommand().execute(mockEvent, args).block();

		assertTrue(response.getContent().startsWith("Rolling 1d20"));

		String[] args1 = { "1d" };
		response = new RollCommand().execute(mockEvent, args1).block();
		assertTrue(response.getContent().isEmpty());

		String[] args2 = { "" };
		response = new RollCommand().execute(mockEvent, args2).block();
		assertTrue(response.getContent().isEmpty());
	}

	@Test
	void commandsTest() {
		String[] args = null;
		CommandResponse response = new CommandsCommand().execute(mockEvent, args).block();

		assertTrue(response.getContent().startsWith("Available commands"));
	}

	@Test
	void pollTest() {
		String[] args = { "\"Question\"", "\"Answer 1\"", "\"Answer 2\"", "\"Answer 3\"" };
		when(mockEvent.getMember()).thenReturn(Optional.of(mockMember));
		when(mockMember.getUsername()).thenReturn("Test Username");
		CommandResponse response = new PollCommand().execute(mockEvent, args).block();

		assertTrue(response.getPoll().getTitle().equals("Question"));
		assertTrue(response.getPoll().getAnswers().get(0).equals("Answer 1"));
		assertTrue(response.getPoll().getAnswers().get(1).equals("Answer 2"));
		assertTrue(response.getPoll().getAnswers().get(2).equals("Answer 3"));
	}

}
