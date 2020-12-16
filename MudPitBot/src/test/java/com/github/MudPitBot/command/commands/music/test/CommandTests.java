package com.github.MudPitBot.command.commands.music.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.commands.music.*;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;

public class CommandTests {

	TrackScheduler scheduler = GuildMusicManager.createTrackScheduler(1l);

	@Test
	void clearTest() {
		CommandResponse response = new ClearCommand().clearQueue(scheduler).block();

		assertEquals(response.getContent(), "Queue cleared");
	}

	@Test
	void fastForwardTest() {
		String[] args = { "1" };
		CommandResponse response = new FastForwardCommand().fastForward(scheduler, args).block();

		assertEquals(response, CommandResponse.emptyResponse());
	}

	@Test
	void nowPlayingTest() {
		CommandResponse response = new NowPlayingCommand().nowPlaying(scheduler).block();

		assertEquals(response.getContent(), "No track is currently playing");
	}

	@Test
	void pauseTest() {
		CommandResponse response = new PauseCommand().pause(scheduler).block();

		assertEquals(response, CommandResponse.emptyResponse());
	}

	@Test
	void playTest() {
		String[] args = { "1" };
		CommandResponse response = new PlayCommand().play(scheduler, args).block();

		assertEquals(response, CommandResponse.emptyResponse());
	}

	@Test
	void removeTest() {
		String[] args = { "1" };
		CommandResponse response = new RemoveCommand().remove(scheduler, args).block();

		assertEquals(response, CommandResponse.emptyResponse());
	}

	@Test
	void rewindTest() {
		String[] args = { "1" };
		CommandResponse response = new RewindCommand().rewind(scheduler, args).block();

		assertEquals(response, CommandResponse.emptyResponse());
	}

	@Test
	void seekTest() {
		String[] args = { "1" };
		CommandResponse response = new SeekCommand().seek(scheduler, args).block();

		assertEquals(response, CommandResponse.emptyResponse());
	}

	@Test
	void shuffleTest() {
		CommandResponse response = new ShuffleCommand().shuffleQueue(scheduler).block();

		assertEquals(response.getContent(), "Queue shuffled");
	}

	@Test
	void skipTest() {
		CommandResponse response = new SkipCommand().skip(scheduler).block();

		assertEquals(response.getContent(), "No song is currently playing");
	}

	@Test
	void stopTest() {
		CommandResponse response = new StopCommand().stop(scheduler).block();

		assertEquals(response.getContent(), "Player stopped");
	}

	@Test
	void viewQueueTest() {
		CommandResponse response = new ViewQueueCommand().viewQueue(scheduler).block();

		assertEquals(response.getContent(), "The queue is empty");
	}

	@Test
	void volumeTest() {
		String[] args = { "1" };
		CommandResponse response = new VolumeCommand().volume(scheduler, args).block();

		assertTrue(response.getContent().startsWith("Changing volume from"));
	}

}
