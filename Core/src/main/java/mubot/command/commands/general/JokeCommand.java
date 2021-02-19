package mubot.command.commands.general;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import jokeapi.JokeClient;
import mubot.command.CommandResponse;
import mubot.command.help.CommandHelpSpec;
import mubot.command.menu.menus.JokeMenu;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JokeCommand extends RequireBotPermissionsCommand {

    public JokeCommand() {
        super("joke", Permission.MANAGE_MESSAGES);
    }

    @Override
    protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args) {
        return joke(args);
    }

    private Mono<CommandResponse> joke(@NonNull String[] args) {
        boolean unsafe = false;
        JokeMenu menu = null;
        if (args.length > 0) {
            List<String> argList = Arrays.asList(args);
            unsafe = argList.contains("unsafe");
            List<String> categories = JokeClient.getJokeService().getCategories().map(List::stream)
                    .map(s -> s.map(String::toLowerCase).collect(Collectors.toList())).block();
            for (String arg : argList) {
                if (Objects.requireNonNull(categories).contains(arg.toLowerCase())) {
                    // no safe-dark jokes, so ignore
                    if (arg.equals("dark") && !unsafe)
                        break;

                    menu = new JokeMenu(unsafe, arg);
                    break;
                }
            }
        }

        if (menu == null)
            menu = new JokeMenu(unsafe);

        return CommandResponse.create(menu.createMessage(), menu);
    }

    @Override
    public Consumer<? super CommandHelpSpec> createHelpSpec() {
        return spec -> spec.setDescription("Tells a random joke from the chosen category of jokes.")
                .addArg("unsafe", "Allows \"unsafe\" jokes to be returned by the bot.", true)
                .addArg("category", "Gets a joke of only the given category.", true).addExample("pun")
                .addExample("unsafe").addExample("unsafe any").addExample("misc unsafe");
    }

}
