package com.github.MudPitBot.test;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.MudPitBot.command.misc.Poll;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.UserData;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class PollTest {

	@Mock
	MessageCreateEvent mockEvent = mock(MessageCreateEvent.class);

	@Mock
	Message mockMessage = mock(Message.class);

	@Mock
	UserData mockUserData = mock(UserData.class);

	@Test
	void createPoll() {
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getContent()).thenReturn("!poll \"Title\" \"Answer1\" \"Answer2\"");
		when(mockMessage.getUserData()).thenReturn(mockUserData);
		when(mockUserData.username()).thenReturn("Username");
		Poll p = new Poll(mockEvent);

		assertEquals(p.getAnswers().get(0), "Answer1");
	}

	@Test
	void nullParams() {
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		Poll p = new Poll(mockEvent);

		assertTrue(p.getAnswers().isEmpty());
	}

	@Test
	void nullEvent() {
		when(mockMessage.getContent()).thenReturn("\"Title\" \"Answer1\" \"Answer2\"");
		Poll p = new Poll(mockEvent);

		assertTrue(p.getAnswers().isEmpty());
	}

}
