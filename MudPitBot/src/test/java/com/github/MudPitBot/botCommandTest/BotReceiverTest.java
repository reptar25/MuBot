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
	MessageCreateEvent mockEvent = mock(MessageCreateEvent.class);
	
	@Mock
	Message mockMessage = mock(Message.class);

	@Test
	void nullEvent() {
		when(mockEvent).thenReturn(null);
		for (Command c : Commands.COMMANDS.values()) {
			c.execute(mockEvent);
		}
	}

	@Test
	void nullMessage() {
		when(mockEvent.getMessage()).thenReturn(null);
		for (Command c : Commands.COMMANDS.values()) {
			c.execute(mockEvent);
		}
	}

	@Test
	void nullMember() {
		when(mockEvent.getMember()).thenReturn(null);
		for (Command c : Commands.COMMANDS.values()) {
			c.execute(mockEvent);
		}
	}
	
//	@Test
//	void nullGuild() {
//		when(mockEvent.getGuild().block()).thenReturn(null);
//		for (Command c : Commands.COMMANDS.values()) {
//			c.execute(mockEvent);
//		}
//	}
//	
//	@Test
//	void nullChannel() {
//		when(mockEvent.getMessage()).thenReturn(mockMessage);
//		when(mockMessage.getChannel().block()).thenReturn(null);
//		for (Command c : Commands.COMMANDS.values()) {
//			c.execute(mockEvent);
//		}
//	}

}
