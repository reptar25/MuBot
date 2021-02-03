package com.github.mubot.command.help;

public class CommandArgument {

	private String name;
	private String description;
	private boolean optional;

	public CommandArgument(String name, String description, boolean optional) {
		super();
		this.name = name;
		this.description = description;
		this.optional = optional;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isOptional() {
		return optional;
	}

}
