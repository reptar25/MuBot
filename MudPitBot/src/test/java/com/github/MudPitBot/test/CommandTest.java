package com.github.MudPitBot.test;

import org.mockito.Mock;

import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.impl.*;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class CommandTest {

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

	TrackScheduler mockScheduler = new TrackScheduler(mockSnowflake.asLong());

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

//	@Test
//	void nullEvent() {
//		when(mockEvent).thenReturn(null);
//		for (Command c : Commands.values()) {
//			c.execute(mockEvent, args);
//		}
//	}
//
//	@Test
//	void nullMessage() {
//		when(mockEvent.getMessage()).thenReturn(null);
//		for (Command c : Commands.values()) {
//			c.execute(mockEvent, args);
//		}
//	}
//
//	@Test
//	void emptyMember() {
//		when(mockEvent.getMember()).thenReturn(Optional.empty());
//		for (Command c : Commands.values()) {
//			c.execute(mockEvent, args);
//		}
//	}
//
//	@Test
//	void nullGuild() {
//		when(mockEvent.getGuild()).thenReturn(monoGuild);
//		when(monoGuild.block()).thenReturn(null);
//		for (Command c : Commands.values()) {
//			c.execute(mockEvent, args);
//		}
//	}
//
//	@Test
//	void nullChannel() {
//		// when(mockEvent.getMessage()).thenReturn(mockMessage);
//		when(mockEvent.getMessage()).thenReturn(mockMessage);
//		when(mockMessage.getChannel()).thenReturn(monoChannel);
//		when(monoChannel.block()).thenReturn(null);
//		for (Command c : Commands.values()) {
//			c.execute(mockEvent, args);
//		}
//	}

//	@Test
//	void testEcho() {
//		String response = receiver.echo().getContent();
//		assertEquals("echo!", response);
//	}
//
//	@Test
//	void testPlay() {
//		String[] params = { "https://youtu.be/5qap5aO4i9A" };
//		receiver.play(mockScheduler, params);
//
//		params[0] = "";
//		receiver.play(mockScheduler, params);
//	}
//
//	@Test
//	void testRoll() {
//		String[] params = new String[1];
//
//		params[0] = "1d20";
//		String response = receiver.roll(params).getContent();
//		assertTrue(response.startsWith("Rolling 1d20"));
//
//		params[0] = "0d20";
//		CommandResponse response2 = receiver.roll(params);
//		assertNull(response2);
//
//		params[0] = "d20";
//		response2 = receiver.roll(params);
//		assertNull(response2);
//
//		response2 = receiver.roll(null);
//		assertNull(response2);
//	}
//
//	@Test
//	void testVolume() {
//		String[] params = new String[1];
//
//		params[0] = "50";
//		String response = receiver.volume(mockScheduler, params).getContent();
//		assertEquals("Changing volume from 10 to 50", response);
//
//		params[0] = "-1";
//		CommandResponse response2 = receiver.volume(mockScheduler, params);
//		assertNull(response2);
//
//		params[0] = "100";
//		response = receiver.volume(mockScheduler, params).getContent();
//		assertEquals("Changing volume from 50 to 100", response);
//
//		params[0] = "101";
//		response2 = receiver.volume(mockScheduler, params);
//		assertNull(response2);
//
//		params[0] = "reset";
//		response = receiver.volume(mockScheduler, params).getContent();
//		assertEquals("Volume reset to default", response);
//	}
//
//	@Test
//	void testNowPlaying() {
//		String response = receiver.nowPlaying(mockScheduler).getContent();
//		assertTrue(response.startsWith("Now playing:"));
//	}
//
//	@Test
//	void testStop() {
//		String response = receiver.stop(mockScheduler).getContent();
//		assertEquals(response, "Player stopped");
//
//		CommandResponse response2 = receiver.stop(null);
//		assertNull(response2);
//	}
//
//	@Test
//	void testSkip() {
//		CommandResponse response = receiver.skip(mockScheduler);
//		assertNull(response);
//
//		response = receiver.skip(null);
//		assertNull(response);
//	}
//
//	@Test
//	void testClearQueue() {
//		String response = receiver.clearQueue(mockScheduler).getContent();
//		assertEquals(response, "Queue cleared");
//
//		CommandResponse response2 = receiver.clearQueue(null);
//		assertNull(response2);
//	}
//
//	@Test
//	void testViewQueue() {
//		String response = receiver.viewQueue(mockScheduler).getContent();
//		assertEquals(response, "The queue is empty");
//
//		CommandResponse response2 = receiver.viewQueue(null);
//		assertNull(response2);
//	}
//
//	@Test
//	void testShuffle() {
//		CommandResponse response = receiver.shuffleQueue(mockScheduler);
//		assertNull(response);
//
//		response = receiver.shuffleQueue(null);
//		assertNull(response);
//	}
//
//	@Test
//	void testPause() {
//		CommandResponse response = receiver.pause(mockScheduler);
//		assertNull(response);
//
//		response = receiver.pause(null);
//		assertNull(response);
//	}
//
//	@Test
//	void testSeek() {
//		String[] params = new String[1];
//
//		params[0] = "50";
//
//		CommandResponse response = receiver.seek(mockScheduler, params);
//		assertNull(response);
//
//		response = receiver.seek(null, null);
//		assertNull(response);
//	}
//
//	@Test
//	void testRewind() {
//		String[] params = new String[1];
//
//		params[0] = "50";
//
//		CommandResponse response = receiver.rewind(mockScheduler, params);
//		assertNull(response);
//
//		response = receiver.rewind(null, null);
//		assertNull(response);
//	}
//
//	@Test
//	void testFastForward() {
//		String[] params = new String[1];
//
//		params[0] = "50";
//
//		CommandResponse response = receiver.fastForward(mockScheduler, params);
//		assertNull(response);
//
//		response = receiver.fastForward(null, null);
//		assertNull(response);
//	}

}