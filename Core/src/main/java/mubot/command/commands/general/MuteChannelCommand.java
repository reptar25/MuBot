package mubot.command.commands.general;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import mubot.command.Command;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.command.util.EmojiHelper;
import mubot.eventlistener.MuteOnJoinListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static mubot.command.util.PermissionsHelper.requireBotChannelPermissions;
import static mubot.command.util.PermissionsHelper.requireVoiceChannel;

public class MuteChannelCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(MuteChannelCommand.class);

	public MuteChannelCommand() {
		super("mutechannel");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireVoiceChannel(event).flatMap(
				channel -> requireBotChannelPermissions(channel, Permission.MUTE_MEMBERS).flatMap(ignored -> mute(channel)));
	}


	/**
	 * Mutes all {@link Member} in the channel besides bots
	 * @param channel the channel to mute
	 * @return the response to muting
	 */
	public Mono<CommandResponse> mute(VoiceChannel channel) {
		/*
		 * gets the member's channel who sent the message, and then all the VoiceStates
		 * connected to that channel. From there we can get the Member of the VoiceState
		 */
		final Snowflake guildId = channel.getGuildId();
		final Snowflake id = channel.getId();
		Flux<VoiceState> users = channel.getVoiceStates();

		boolean mute = true;
		ArrayList<Snowflake> channelIds = MuteOnJoinListener.mutedChannels.get(guildId);
		if (channelIds != null) {
			// channel is muted, so unmute
			if (channelIds.contains(id)) {
				mute = false;
				channelIds.remove(id);
			} else {
				channelIds.add(id);
			}
		} else {
			// channel should be muted
			ArrayList<Snowflake> ids = new ArrayList<>();
			ids.add(id);
			MuteOnJoinListener.mutedChannels.put(guildId, ids);
		}

		String response;
		Flux<Void> doMute;
		if (mute) {
			doMute = muteUsers(users, true);
			response = EmojiHelper.MUTE + " Muting " + channel.getName() + " " + EmojiHelper.MUTE;
		} else {
			doMute = muteUsers(users, false);
			response = EmojiHelper.SOUND + " Unmuting " + channel.getName() + " " + EmojiHelper.SOUND;
		}
		return doMute.then(CommandResponse.create(response));
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription(
				"Mutes the voice channel of the user who used the command. Will also mute any new users that join that channel until this command is used again to unmute the channel.");
	}

	private Flux<Void> muteUsers(Flux<VoiceState> users, boolean mute) {
		return users.flatMap(VoiceState::getMember).filter(Predicate.not(Member::isBot)).flatMap(member -> {
			LOGGER.info("Unmuting " + member.getUsername());
			return member.edit(spec -> spec.setMute(mute));
		});
	}

}
