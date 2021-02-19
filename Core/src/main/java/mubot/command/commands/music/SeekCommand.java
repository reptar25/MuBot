package mubot.command.commands.music;

import mubot.command.help.CommandHelpSpec;
import mubot.music.TrackScheduler;

import java.util.function.Consumer;

public class SeekCommand extends TrackSeekingCommand {

    public SeekCommand() {
        super("seek");
    }

    @Override
    protected void doSeeking(TrackScheduler scheduler, int amountInSeconds) {
        scheduler.seek(amountInSeconds);
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Moves the currently playing song to the given time.").addArg("time",
                "amount of time in seconds to set the song to i.e. \"60\" will set the song to the 1 minute mark, and \"0\" would set the song back to the beginning.",
                false).addExample("60").addExample("0");
    }

}
