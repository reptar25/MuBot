package com.github.MudPitBot.command.help;

import static com.github.MudPitBot.command.util.CommandUtil.DEFAULT_COMMAND_PREFIX;
import static com.github.MudPitBot.command.util.CommandUtil.DEFAULT_EMBED_COLOR;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.MudPitBot.command.util.CommandUtil;

import discord4j.core.spec.EmbedCreateSpec;

public class CommandHelpSpec {

	private String commandName;
	private String description;
	private List<String> examples;
	private List<Argument> arguments;

	public CommandHelpSpec(String commandName) {
		this.commandName = commandName;
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

	public Consumer<? super EmbedCreateSpec> build() {
		return spec -> {
			spec.setColor(DEFAULT_EMBED_COLOR);

			spec.setTitle(DEFAULT_COMMAND_PREFIX + commandName);
			spec.addField("Usage", getUsage(), false);

			if (description != null && !this.description.isBlank()) {
				spec.setDescription(description);
			}

			if (!this.getArguments().isEmpty()) {
				spec.addField("Arguments", this.getArguments(), false);
			}

			if (!this.getArguments().isEmpty()) {
				spec.addField("Examples", this.getExamples(), false);
			}
		};
	}

	private String getUsage() {
		if (this.arguments.isEmpty()) {
			return String.format("`%s%s`", CommandUtil.DEFAULT_COMMAND_PREFIX, this.commandName);
		}

		String usage = arguments.stream().map(arg -> String.format(arg.isOptional() ? "[<%s>]" : "<%s>", arg.getName()))
				.collect(Collectors.joining(" "));
		return String.format("`%s%s %s`", CommandUtil.DEFAULT_COMMAND_PREFIX, this.commandName, usage);
	}

	private String getArguments() {
		return arguments
				.stream().filter(arg -> arg != null && !arg.name.isBlank()).map(arg -> String.format("%n**%s** %s - %s",
						arg.getName(), arg.optional ? "[optional]" : "", arg.getDescription()))
				.collect(Collectors.joining());
	}

	private String getExamples() {
		return examples.stream().map(example -> String.format("%n%s", example)).collect(Collectors.joining());
	}
}
