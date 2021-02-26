package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.command.util.CommandUtil;
import mubot.command.util.EmojiHelper;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Collections;
import java.util.function.Consumer;

public class SkipCommand extends MusicCommand {

    public SkipCommand() {
        super("skip", Collections.singletonList("next"));
    }

    @Override
    protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
                                           VoiceChannel channel) {
        return skip(args, scheduler);
    }

    /**
     * Stops the current song and plays the next in queue if there is any
     *
     * @param args      the track to skip to or nothing to skip the current track
     * @param scheduler the track scheduler
     * @return the response to skipping
     */
    public Mono<CommandResponse> skip(@NonNull String[] args, @NonNull TrackScheduler scheduler) {
        if (scheduler.getNowPlaying() != null) {

            if (args.length > 0 && !args[0].isBlank()) {
                try {
                    int element = Integer.parseInt(args[0]);
                    return CommandResponse.create(EmojiHelper.NEXT_TRACK + " Skipping to "
                            + scheduler.skipQueue(element) + " " + EmojiHelper.NEXT_TRACK);
                } catch (NumberFormatException ignored) {
                    // if there isn't an int as the args than just skip the current song
                }
            }

            String response = EmojiHelper.NEXT_TRACK + " Skipping " + CommandUtil.trackInfo(scheduler.getNowPlaying())
                    + " " + EmojiHelper.NEXT_TRACK;
            scheduler.nextTrack();
            return CommandResponse.create(response);
        } else {
            return CommandResponse.create("No song is currently playing");
        }
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription(
                "Skips the currently playing song and plays the next song in the queue or skips to the specific song number in the queue.")
                .addArg("skipTo", "skips to the specific number in the queue", true).addExample("").addExample("3");
    }

}
