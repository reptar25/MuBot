package com.github.MudPitBot.botCommandTest;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.Commands;
import com.github.MudPitBot.core.CommandReceiver;
import com.github.MudPitBot.sound.PlayerManager;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CommandReceiverTest {

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

	TrackScheduler mockScheduler = new TrackScheduler();

	String[] args = null;

	CommandReceiver receiver = CommandReceiver.getInstance();

	@Test
	void nullEvent() {
		when(mockEvent).thenReturn(null);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

	@Test
	void nullMessage() {
		when(mockEvent.getMessage()).thenReturn(null);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

	@Test
	void nullMember() {
		when(mockEvent.getMember()).thenReturn(null);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

	@Test
	void nullGuild() {
		when(mockEvent.getGuild()).thenReturn(monoGuild);
		when(monoGuild.block()).thenReturn(null);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

	@Test
	void nullChannel() {
		// when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getChannel()).thenReturn(monoChannel);
		when(monoChannel.block()).thenReturn(null);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

	@Test
	void testEcho() {
		String response = receiver.echo().getContent();
		assertEquals("echo!", response);
	}

	@Test
	void testPlay() {
		String[] params = { "https://youtu.be/5qap5aO4i9A" };
		receiver.play(mockScheduler, params);

		params[0] = "";
		receiver.play(mockScheduler, params);
	}

	@Test
	void testRoll() {
		String[] params = new String[1];

		params[0] = "1d20";
		String response = receiver.roll(params).getContent();
		assertTrue(response.startsWith("Rolling 1d20"));

		params[0] = "0d20";
		CommandResponse response2 = receiver.roll(params);
		assertNull(response2);

		params[0] = "d20";
		response2 = receiver.roll(params);
		assertNull(response2);

		response2 = receiver.roll(null);
		assertNull(response2);
	}

	@Test
	void testVolume() {
		String[] params = new String[1];

		params[0] = "50";
		String response = receiver.volume(mockScheduler, params).getContent();
		assertEquals("Changing volume from 10 to 50", response);

		params[0] = "-1";
		CommandResponse response2 = receiver.volume(mockScheduler, params);
		assertNull(response2);

		params[0] = "100";
		response = receiver.volume(mockScheduler, params).getContent();
		assertEquals("Changing volume from 50 to 100", response);

		params[0] = "101";
		response2 = receiver.volume(mockScheduler, params);
		assertNull(response2);

		params[0] = "reset";
		response = receiver.volume(mockScheduler, params).getContent();
		assertEquals("Volume reset to default", response);
	}

	@Test
	void testNowPlaying() {
		String response = receiver.nowPlaying(mockScheduler).getContent();
		assertTrue(response.startsWith("Now playing:"));
	}

	@Test
	void testStop() {
		String response = receiver.stop(mockScheduler).getContent();
		assertEquals(response, "Player stopped");

		CommandResponse response2 = receiver.stop(null);
		assertNull(response2);
	}

	@Test
	void testSkip() {
		CommandResponse response = receiver.skip(mockScheduler);
		assertNull(response);

		response = receiver.skip(null);
		assertNull(response);
	}

	@Test
	void testClearQueue() {
		String response = receiver.clearQueue(mockScheduler).getContent();
		assertEquals(response, "Queue cleared");

		CommandResponse response2 = receiver.clearQueue(null);
		assertNull(response2);
	}

	@Test
	void testViewQueue() {
		String response = receiver.viewQueue(mockScheduler).getContent();
		assertEquals(response, "The queue is empty");

		CommandResponse response2 = receiver.viewQueue(null);
		assertNull(response2);
	}

	@Test
	void testShuffle() {
		CommandResponse response = receiver.shuffleQueue(mockScheduler);
		assertNull(response);

		response = receiver.shuffleQueue(null);
		assertNull(response);
	}

	@Test
	void testPause() {
		CommandResponse response = receiver.pause(mockScheduler);
		assertNull(response);

		response = receiver.pause(null);
		assertNull(response);
	}

	@Test
	void testSeek() {
		String[] params = new String[1];

		params[0] = "50";

		CommandResponse response = receiver.seek(mockScheduler, params);
		assertNull(response);

		response = receiver.seek(null, null);
		assertNull(response);
	}

	@Test
	void testRewind() {
		String[] params = new String[1];

		params[0] = "50";

		CommandResponse response = receiver.rewind(mockScheduler, params);
		assertNull(response);

		response = receiver.rewind(null, null);
		assertNull(response);
	}

	@Test
	void testFastForward() {
		String[] params = new String[1];

		params[0] = "50";

		CommandResponse response = receiver.fastForward(mockScheduler, params);
		assertNull(response);

		response = receiver.fastForward(null, null);
		assertNull(response);
	}

}
