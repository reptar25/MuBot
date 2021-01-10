package com.github.MudPitBot.command.help;

public class Argument {

	String name;
	String description;
	boolean optional;

	public Argument(String name, String description, boolean optional) {
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
