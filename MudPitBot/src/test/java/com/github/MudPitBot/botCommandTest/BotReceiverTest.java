package com.github.MudPitBot.botCommandTest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import com.github.MudPitBot.botCommand.commandInterface.Command;
import com.github.MudPitBot.botCommand.commandInterface.Commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class BotReceiverTest {

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
	
	String[] args = null;

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
		when(mockEvent.getGuild().block()).thenReturn(mockGuild);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

	@Test
	void nullChannel() {
		// when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockEvent.getMessage().getChannel()).thenReturn(monoChannel);
		when(mockEvent.getMessage().getChannel().block()).thenReturn(mockChannel);
		for (Command c : Commands.values()) {
			c.execute(mockEvent, args);
		}
	}

}
