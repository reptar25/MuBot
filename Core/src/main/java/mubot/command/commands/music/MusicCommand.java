package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import mubot.command.Command;
import mubot.command.CommandResponse;
import mubot.music.GuildMusicManager;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;

import java.util.List;

import static mubot.command.util.PermissionsHelper.requireBotChannelPermissions;
import static mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

public abstract class MusicCommand extends Command {

    public MusicCommand(String commandTrigger) {
        super(commandTrigger);
    }

    public MusicCommand(String commandTrigger, List<String> aliases) {
        super(commandTrigger, aliases);
    }

    @Override
    public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
        return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel)
                .flatMap(scheduler -> action(event, args, scheduler, channel)));
    }

    protected abstract Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
                                                    VoiceChannel channel);

    public Mono<VoiceChannel> withPermissions(Mono<VoiceChannel> channelMono, Permission... permissions) {
        return channelMono.flatMap(channel -> requireBotChannelPermissions(channel, permissions).thenReturn(channel));
    }
}
