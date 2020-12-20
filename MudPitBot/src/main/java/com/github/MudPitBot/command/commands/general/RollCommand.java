package com.github.MudPitBot.command.commands.general;

import java.util.Random;
import java.util.regex.Pattern;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.Emoji;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class RollCommand extends Command {

	private static Random rand = new Random();

	public RollCommand() {
		super("roll");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return roll(args);
	}

	/**
	 * Bot rolls dice and returns results
	 * 
	 * @param args The number and type of dice to roll, eg "1d20"
	 * @return The results of the dice roll
	 */
	public Mono<CommandResponse> roll(String[] args) {

		if (args == null || args.length <= 0) {
			return CommandResponse.empty();
		}

		String dice = args[0];

		// only roll if 2nd part of command matches the reg ex
		if (Pattern.matches("[1-9][0-9]*[Dd][1-9][0-9]*", dice)) {

			StringBuilder sb = new StringBuilder(Emoji.DICE + " Rolling " + dice + Emoji.DICE + "\n");

			String[] splitDiceString = dice.split("[Dd]");
			int numOfDice = Integer.parseInt(splitDiceString[0]);
			int numOfSides = Integer.parseInt(splitDiceString[1]);
			int diceSum = 0;

			for (int i = 0; i < numOfDice; i++) {
				int roll = rand.nextInt(numOfSides) + 1;
				sb.append("Dice ").append((i + 1)).append(" was a ").append(roll).append("\n");
				diceSum += roll;
			}

			sb.append("Rolled a total of " + diceSum + "\n");
			return CommandResponse.create(sb.toString());
		}

		return CommandResponse.empty();
	}

}
