package com.github.mubot.command.menu;

import java.util.function.Consumer;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;

public abstract class Menu {

	protected Message message;

	public abstract Consumer<? super MessageCreateSpec> createMessage();

	public void setMessage(Message message) {
		this.message = message;
	}

}
