package com.github.MudPitBot.botCommandTest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.MudPitBot.botCommand.BotReceiver;
import discord4j.core.event.domain.message.MessageCreateEvent;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class BotReceiverTest {
	
	@Mock
    MessageCreateEvent event = mock(MessageCreateEvent.class);
	
	private final BotReceiver receiver = BotReceiver.getInstance();
	
	@Test
	void nullEvent() {
		when(event).thenReturn(null);
		receiver.join(event);
		receiver.leave(event);
		receiver.echo(event);
	}
	
	@Test
	void nullMessage() {
		when(event.getMessage()).thenReturn(null);
		receiver.join(event);
		receiver.leave(event);
		receiver.echo(event);
	}
	
	@Test
	void nullMember() {
		when(event.getMember()).thenReturn(null);
		receiver.join(event);
		receiver.leave(event);
		receiver.echo(event);
	}

}
