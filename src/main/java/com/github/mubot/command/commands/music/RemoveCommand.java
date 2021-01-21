package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.util.EmojiHelper;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;
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
				.flatMap(scheduler -> remove(event, scheduler, args));
	}

	public Mono<CommandResponse> remove(MessageCreateEvent event, @NonNull TrackScheduler scheduler,
			@NonNull String[] args) {
		if (args.length >= 1) {
			try {
				int index = Integer.parseInt(args[0]);
				AudioTrack removed = scheduler.removeFromQueue(index - 1);
				if (removed != null)
					return CommandResponse.create(EmojiHelper.RED_X + " Removed \"" + removed.getInfo().title
							+ "\" from the queue " + EmojiHelper.RED_X);
				else
					return getHelp(event);
			} catch (NumberFormatException ignored) {
				return getHelp(event);
			}
		}
		return getHelp(event);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Removes the song at the given position number from the queue.").addArg(
				"position",
				"The song to be remove's number position in the queue i.e. \"1\" to remove the song at the top of the queue.",
				false).addExample("1");
	}

}
