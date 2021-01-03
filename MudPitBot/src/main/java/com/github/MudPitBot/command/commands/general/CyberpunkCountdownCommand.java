package com.github.MudPitBot.command.commands.general;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CyberpunkCountdownCommand extends Command {

	public CyberpunkCountdownCommand() {
		super("Cyberpunk");

	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return cyberpunk();
	}

	/**
	 * Calculate and responds with the time left until release of Cyberpunk
	 * 
	 * @return the time left in days, hours, minutes, seconds left until Cyberpunk
	 *         releases or that it's already out if its past.
	 */
	public Mono<CommandResponse> cyberpunk() {

		// gets current time in EST
		LocalDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York")).toLocalDateTime();

		// release date in EST
		LocalDateTime cprelease = LocalDateTime.of(2020, 12, 9, 19, 0);

		Duration duration = Duration.ofMillis(ChronoUnit.MILLIS.between(now, cprelease));

		long days = duration.toDaysPart();
		long hours = duration.toHoursPart();
		long minutes = duration.toMinutesPart();
		long seconds = duration.toSecondsPart();

		if (days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0)
			return CommandResponse.create("Cyberpunk is out dumbass, the wait is over!");

		StringBuilder sb = new StringBuilder("Cyberpunk will release in: ").append(days).append(" days ").append(hours)
				.append(" hours ").append(minutes).append(" minutes ").append(seconds).append(" seconds");

		return CommandResponse.create(sb.toString());
	}

}
