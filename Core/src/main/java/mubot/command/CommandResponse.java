package mubot.command;

import java.util.function.Consumer;

import mubot.command.menu.Menu;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

public class CommandResponse {

	private static final CommandResponse empty = new CommandResponse();
	private String content;
	private Consumer<? super MessageCreateSpec> spec;
	private Menu menu;

	// empty constructor
	private CommandResponse() {
		this.content = "";
		this.spec = null;
	}

	private CommandResponse(String content) {
		this.content = "";
		this.spec = null;
		// if the message is longer than 2000 character, trim it so that its not over
		// the max character limit.
		this.content = content.length() >= Message.MAX_CONTENT_LENGTH
				? content.substring(0, Message.MAX_CONTENT_LENGTH - 1)
				: content;
		this.spec = spec -> spec.setContent(this.content);
		this.menu = null;
	}

	private CommandResponse(Consumer<? super MessageCreateSpec> spec) {
		this.content = null;
		this.spec = spec;
		this.menu = null;
	}

	private CommandResponse(Builder b) {
		this.content = b.content;
		this.spec = b.spec;
		this.menu = b.menu;
	}

	public Consumer<? super MessageCreateSpec> getSpec() {
		return spec;
	}

	public String getContent() {
		return content;
	}

	public Menu getMenu() {
		return menu;
	}

	public static CommandResponse emptyFlat() {
		return empty;
	}

	public static Mono<CommandResponse> empty() {
		return Mono.just(empty);
	}

	public static CommandResponse createFlat(String content) {
		return new CommandResponse(content);
	}

	public static Mono<CommandResponse> create(String content) {
		return new CommandResponse.Builder().withContent(content).build();
	}

	public static Mono<CommandResponse> create(String content, Menu m) {
		return new CommandResponse.Builder().withContent(content).withMenu(m).build();
	}

	public static Mono<CommandResponse> create(Consumer<? super MessageCreateSpec> spec, Menu m) {
		return new CommandResponse.Builder().withCreateSpec(spec).withMenu(m).build();
	}

	public static Mono<CommandResponse> create(Consumer<? super MessageCreateSpec> spec) {
		return new CommandResponse.Builder().withCreateSpec(spec).build();
	}

	public static class Builder {
		private String content;
		private Consumer<? super MessageCreateSpec> spec;
		private Menu menu;

		public Builder withContent(String content) {
			this.content = content;
			this.spec = spec -> spec.setContent(content);
			return this;
		}

		public Builder withCreateSpec(Consumer<? super MessageCreateSpec> spec) {
			this.spec = spec;
			return this;
		}

		public Builder withMenu(Menu menu) {
			this.menu = menu;
			return this;
		}

		public Mono<CommandResponse> build() {
			return Mono.just(new CommandResponse(this));
		}
	}

}
