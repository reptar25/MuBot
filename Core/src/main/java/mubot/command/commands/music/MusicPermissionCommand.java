package mubot.command.commands.music;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import mubot.command.CommandResponse;
import mubot.music.GuildMusicManager;
import reactor.core.publisher.Mono;

import java.util.List;

import static mubot.command.util.PermissionsHelper.requireBotChannelPermissions;
import static mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

public abstract class MusicPermissionCommand extends MusicCommand {
    private final Permission[] permissions;

    public MusicPermissionCommand(String commandTrigger, Permission... permissions) {
        super(commandTrigger);
        this.permissions = permissions;
    }

    public MusicPermissionCommand(String commandTrigger, List<String> aliases, Permission... permissions) {
        super(commandTrigger, aliases);
        this.permissions = permissions;
    }

    @Override
    public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
        return requireSameVoiceChannel(event)
                .flatMap(channel -> requireBotChannelPermissions(channel, this.permissions).thenReturn(channel))
                .flatMap(channel -> GuildMusicManager.getScheduler(channel)
                        .flatMap(scheduler -> action(event, args, scheduler, channel)));
    }
}
