package com.github.MudPitBot.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.MudPitBot.command.core.CommandClient;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import static org.mockito.Mockito.*;

import java.util.Optional;

public class CommandClientTest {

	@Mock
	static GatewayDiscordClient mockClient = mock(GatewayDiscordClient.class);

	@Mock
	Message mockMessage = mock(Message.class);

	@Mock
	User mockUser = mock(User.class);

	@SuppressWarnings("unchecked")
	@Mock
	Optional<User> mockUserOptional = mock(Optional.class);

	@Mock
	Member mockMember = mock(Member.class);

	@SuppressWarnings("unchecked")
	@Mock
	Optional<Member> mockMemberOptional = mock(Optional.class);

	@Mock
	MessageCreateEvent mockEvent = mock(MessageCreateEvent.class);

	static CommandClient client;

	@BeforeAll
	static void createClient() {
		client = CommandClient.create(mockClient);
	}

	@Test
	void processMessage() {
		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getAuthor()).thenReturn(mockUserOptional);
		when(mockUserOptional.orElse(null)).thenReturn(mockUser);
		client.processMessage(mockEvent, "!LeAvE");
	}

	@Test
	void logMessage() {
		when(mockEvent.getMember()).thenReturn(mockMemberOptional);
		when(mockUserOptional.orElse(null)).thenReturn(mockUser);

		when(mockEvent.getMessage()).thenReturn(mockMessage);
		when(mockMessage.getAuthor()).thenReturn(mockUserOptional);
		when(mockUserOptional.orElse(null)).thenReturn(mockUser);
		when(mockUser.getUsername()).thenReturn("Test User Name");

		client.logMessage(mockEvent, "Log me");
	}

}
