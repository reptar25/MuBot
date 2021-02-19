package mubot.command.commands.music;

import mubot.command.help.CommandHelpSpec;
import mubot.music.TrackScheduler;

import java.util.Collections;
import java.util.function.Consumer;

public class RewindCommand extends TrackSeekingCommand {

    public RewindCommand() {
        super("rewind", Collections.singletonList("rw"));
    }

    @Override
    protected void doSeeking(TrackScheduler scheduler, int amountInSeconds) {
        scheduler.rewind(amountInSeconds);
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Rewinds the currently playing song by the given amount of seconds.")
                .addArg("time", "amount of time in seconds to rewind", false).addExample("60");
    }


}