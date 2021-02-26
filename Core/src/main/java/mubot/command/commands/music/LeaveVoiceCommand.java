package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Consumer;

public class LeaveVoiceCommand extends MusicCommand {

    public LeaveVoiceCommand() {
        super("leave", Arrays.asList("quit", "l"));
    }

    @Override
    protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
                                           VoiceChannel channel) {
        return leave(channel);
    }

    /**
     * Bot leaves any voice channel it is connected to in the same guild. Also
     * clears the queue of items.
     *
     * @param channel the channel to leave
     * @return the response to leaving
     */
    public Mono<CommandResponse> leave(VoiceChannel channel) {
        return channel.getVoiceConnection().flatMap(VoiceConnection::disconnect).then(CommandResponse.empty());
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Requests the bot to leave its' current voice channel.");
    }

}
