package com.github.MudPitBot.jokeAPI;

public class JokeEnums {
	enum BlacklistFlag {
		NSFW("nsfw"), RELIGIOUS("religious"), POLITICAL("political"), RACIST("racist"), SEXIST("sexist"),
		EXPLICIT("explicit");

		private String flag;

		BlacklistFlag(String flag) {
			this.flag = flag;
		}

		@Override
		public String toString() {
			return flag;
		}
	}

	enum JokeType {
		SINGLE("single"), TWO_PART("twopart"), DEFAULT("");

		private String type;

		JokeType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return "type=" + type;
		}
	}

	enum JokeLanguage {
		ENGLISH("en"), CZECH("cs"), RUSSIAN("ru"), GERMAN("de"), DEFAULT("");

		private String code;

		JokeLanguage(String code) {
			this.code = code;
		}

		@Override
		public String toString() {
			return "lang="+code;
		}
	}

	enum ResponseFormat {
		JSON("json"), XML("xml"), YAML("yaml"), TXT("txt"), DEFAULT("");

		private String format;

		ResponseFormat(String format) {
			this.format = format;
		}

		@Override
		public String toString() {
			return "format="+format;
		}
	}
}
