package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.function.Consumer;

public class PauseCommand extends MusicCommand {

    public PauseCommand() {
        super("pause");
    }

    @Override
    protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
                                           VoiceChannel channel) {
        return pause(scheduler);
    }

    /**
     * Pauses/unpauses the player
     *
     * @param scheduler the track scheduler
     * @return null
     */
    public Mono<CommandResponse> pause(@NonNull TrackScheduler scheduler) {
        scheduler.pause(scheduler.isPaused());
        return CommandResponse.empty();
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Pauses currently playing track.");
    }

}
