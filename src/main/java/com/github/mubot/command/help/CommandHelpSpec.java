package com.github.mubot.command.help;

import static com.github.mubot.command.util.CommandUtil.DEFAULT_COMMAND_PREFIX;
import static com.github.mubot.command.util.CommandUtil.DEFAULT_EMBED_COLOR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import discord4j.core.spec.EmbedCreateSpec;

public class CommandHelpSpec {

	private String commandName;
	private List<String> aliases;
	private String description;
	private List<String> examples;
	private List<Argument> arguments;

	public CommandHelpSpec(String commandName, List<String> aliases) {
		this.commandName = commandName;
		this.aliases = aliases;
		arguments = new ArrayList<>();
		examples = new ArrayList<>();
	}

	public CommandHelpSpec addArg(String name, String desc, boolean optional) {
		arguments.add(new Argument(name, desc, optional));
		return this;
	}

	public CommandHelpSpec addExample(String example) {
		examples.add(DEFAULT_COMMAND_PREFIX + commandName + " " + example);
		return this;
	}

	public CommandHelpSpec setDescription(String description) {
		this.description = description;
		return this;
	}

	public CommandHelpSpec setAliases(List<String> aliases) {
		this.aliases = aliases;
		return this;
	}

	public Consumer<? super EmbedCreateSpec> build() {
		return spec -> {
			spec.setColor(DEFAULT_EMBED_COLOR);

			spec.setTitle(DEFAULT_COMMAND_PREFIX + commandName);
			if (aliases != null && !this.aliases.isEmpty()) {
				spec.addField("Aliases", this.getAliases(), false);
			}

			spec.addField("Usage", getUsage(), false);

			if (description != null && !this.description.isBlank()) {
				spec.setDescription(description);
			}

			if (this.arguments != null && !this.getArguments().isEmpty()) {
				spec.addField("Arguments", this.getArguments(), false);
			}

			if (this.examples != null && !this.getExamples().isEmpty()) {
				spec.addField("Examples", this.getExamples(), false);
			}
		};
	}

	private String getUsage() {
		if (this.arguments.isEmpty()) {
			return String.format("`%s%s`", DEFAULT_COMMAND_PREFIX, this.commandName);
		}

		String usage = arguments.stream().map(arg -> String.format(arg.isOptional() ? "[<%s>]" : "<%s>", arg.getName()))
				.collect(Collectors.joining(" "));
		return String.format("`%s%s %s`", DEFAULT_COMMAND_PREFIX, this.commandName, usage);
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
		return aliases.stream().map(alias -> String.format("`%s%s`", DEFAULT_COMMAND_PREFIX, alias))
				.collect(Collectors.joining(", "));
	}
}
