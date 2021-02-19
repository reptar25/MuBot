package mubot.music;

import discord4j.common.util.Snowflake;

public class GuildMusic {

    private final long guildId;
    private final TrackScheduler trackScheduler;
    private final LavaPlayerAudioProvider audioProvider;

    public GuildMusic(Snowflake guildId, TrackScheduler trackScheduler, LavaPlayerAudioProvider audioProvider) {
        this.guildId = guildId.asLong();
        this.trackScheduler = trackScheduler;
        this.audioProvider = audioProvider;
    }

    public LavaPlayerAudioProvider getAudioProvider() {
        return audioProvider;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public void destroy() {
        trackScheduler.destroy();
    }

    public long getGuildId() {
        return guildId;
    }

}
