package com.github.MudPitBot.botCommandTest;

import org.mockito.Mock;

import com.github.MudPitBot.botCommand.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TrackSchedulerTest {

	@Mock
	static AudioPlayer mockPlayer = mock(AudioPlayer.class);

	@Mock
	AudioTrack mockTrack = mock(AudioTrack.class);

	@Mock
	AudioPlaylist mockPlaylist = mock(AudioPlaylist.class);

	static TrackScheduler scheduler;

	@BeforeAll
	static void createTrackScheduler() {
		scheduler = new TrackScheduler(mockPlayer);
	}

	@Test
	void testQueue() {
		scheduler.queue(mockTrack);
	}

	@Test
	void testTrackLoaded() {
		scheduler.trackLoaded(mockTrack);
	}

	@Test
	void testPlaylistLoaded() {
		scheduler.playlistLoaded(mockPlaylist);
	}

	@Test
	void testGetQueue() {
		scheduler.queue(mockTrack);
		scheduler.queue(mockTrack);
		scheduler.queue(mockTrack);
		assertFalse(scheduler.getQueue().isEmpty());

		scheduler.clearQueue();
		assertTrue(scheduler.getQueue().isEmpty());
	}

	@Test
	void testNextTrack() {
		scheduler.nextTrack();
	}

	@Test
	void testPause() {
		scheduler.pause(true);
		assertFalse(scheduler.isPaused());
	}

}
