package mubot.command.help;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static mubot.command.util.CommandUtil.getEscapedGuildPrefixFromId;
import static mubot.command.util.CommandUtil.getRawGuildPrefixFromId;

public class CommandHelpSpec {
	public static final Color DEFAULT_HELP_EMBED_COLOR = Color.of(23, 53, 77);
	private final String commandName;
	private final List<String> aliases;
	private String description;
	private final List<String> examples;
	private final List<CommandArgument> arguments;
	private final long guildId;

	public CommandHelpSpec(String commandName, List<String> aliases, long guildId) {
		this.commandName = commandName;
		this.aliases = aliases;
		this.guildId = guildId;
		arguments = new ArrayList<>();
		examples = new ArrayList<>();
	}

	public CommandHelpSpec addArg(String name, String desc, boolean optional) {
		arguments.add(new CommandArgument(name, desc, optional));
		return this;
	}

	public CommandHelpSpec addExample(String example) {
		examples.add(getEscapedGuildPrefixFromId(guildId) + commandName + " " + example);
		return this;
	}

	public CommandHelpSpec setDescription(String description) {
		this.description = description;
		return this;
	}

	public Consumer<? super EmbedCreateSpec> build() {
		return spec -> {
			spec.setColor(DEFAULT_HELP_EMBED_COLOR);

			spec.setTitle(getEscapedGuildPrefixFromId(guildId) + commandName);
			if (aliases != null && !this.aliases.isEmpty()) {
				spec.addField("Aliases", this.getAliases(), false);
			}

			spec.addField("Usage", getUsage(), false);

			if (description != null && !this.description.isBlank()) {
				spec.setDescription(description);
			}

			if (!this.getArguments().isEmpty()) {
				spec.addField("Arguments", this.getArguments(), false);
			}

			if (!this.getExamples().isEmpty()) {
				spec.addField("Examples", this.getExamples(), false);
			}
		};
	}

	private String getUsage() {
		if (this.arguments.isEmpty()) {
			return String.format("`%s%s`", getRawGuildPrefixFromId(guildId), this.commandName);
		}

		String usage = arguments.stream().map(arg -> String.format(arg.isOptional() ? "[<%s>]" : "<%s>", arg.getName()))
				.collect(Collectors.joining(" "));
		return String.format("`%s%s %s`", getRawGuildPrefixFromId(guildId), this.commandName, usage);
	}

	private String getArguments() {
		return arguments.stream().filter(arg -> arg != null && !arg.getName().isBlank()).map(arg -> String
				.format("%n**%s** %s - %s", arg.getName(), arg.isOptional() ? "[optional]" : "", arg.getDescription()))
				.collect(Collectors.joining());
	}

	private String getExamples() {
		return examples.stream().map(example -> String.format("%n%s", example)).collect(Collectors.joining());
	}

	private String getAliases() {
		return aliases.stream().map(alias -> String.format("`%s%s`", getRawGuildPrefixFromId(guildId), alias))
				.collect(Collectors.joining(", "));
	}
}
