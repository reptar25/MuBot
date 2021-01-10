package com.github.mudpitbot.command.commands.music;

import static com.github.mudpitbot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.help.CommandHelpSpec;
import com.github.mudpitbot.command.util.EmojiHelper;
import com.github.mudpitbot.music.GuildMusicManager;
import com.github.mudpitbot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class RemoveCommand extends Command {

	public RemoveCommand() {
		super("remove");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> remove(scheduler, args));
	}

	public Mono<CommandResponse> remove(@NonNull TrackScheduler scheduler, @NonNull String[] args) {
		if (args.length >= 1) {
			try {
				int index = Integer.parseInt(args[0]);
				AudioTrack removed = scheduler.removeFromQueue(index - 1);
				if (removed != null)
					return CommandResponse.create(
							EmojiHelper.RED_X + " Removed \"" + removed.getInfo().title + "\" from the queue " + EmojiHelper.RED_X);
				else
					return CommandResponse.empty();
			} catch (NumberFormatException ignored) {
				return CommandResponse.empty();
			}
		}
		return CommandResponse.empty();
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Removes the song at the given position number from the queue.").addArg(
				"position",
				"The song to be remove's number position in the queue i.e. \"1\" to remove the song at the top of the queue.",
				false).addExample("1");
	}

}
