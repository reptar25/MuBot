package mubot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.command.util.CommandUtil;
import mubot.command.util.EmojiHelper;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Arrays;
import java.util.function.Consumer;

public class NowPlayingCommand extends MusicCommand {

    public NowPlayingCommand() {
        super("nowplaying", Arrays.asList("np", "playing"));
    }

    @Override
    protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
                                           VoiceChannel channel) {
        return nowPlaying(scheduler);
    }

    /**
     * Return the info for the currently playing song
     *
     * @param scheduler the track scheduler
     * @return the response to now playing
     */
    public Mono<CommandResponse> nowPlaying(@NonNull TrackScheduler scheduler) {
        // get the track that's currently playing
        AudioTrack track = scheduler.getNowPlaying();
        if (track != null) {
            String response = EmojiHelper.NOTES + " Now playing " + CommandUtil.trackInfoWithCurrentTime(track) + " "
                    + EmojiHelper.NOTES;
            return CommandResponse.create(response);
        }
        return CommandResponse.create("No track is currently playing");
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Displays currently playing song.");
    }

}
