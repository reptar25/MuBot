package com.github.MudPitBot.botCommandTest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import com.github.MudPitBot.botCommand.commandImpl.Commands;
import com.github.MudPitBot.botCommand.commandInterface.Command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ImmutableMessageData;
import discord4j.discordjson.json.MessageData;

import static org.mockito.Mockito.*;

class BotReceiverTest {

	@Mock
	MessageCreateEvent event = mock(MessageCreateEvent.class);

	@Test
	void nullEvent() {
		when(event).thenReturn(null);
		for (Command c : Commands.COMMANDS.values()) {
			c.execute(event);
		}
	}

	@Test
	void nullMessage() {
		when(event.getMessage()).thenReturn(null);
		for (Command c : Commands.COMMANDS.values()) {
			c.execute(event);
		}
	}

	@Test
	void nullMember() {
		when(event.getMember()).thenReturn(null);
		for (Command c : Commands.COMMANDS.values()) {
			c.execute(event);
		}
	}

}
