package com.github.MudPitBot.command.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;

public abstract class Emoji {

	public static final ReactionEmoji.Unicode A_UNICODE = ReactionEmoji.unicode("🇦");
	public static final ReactionEmoji.Unicode B_UNICODE = ReactionEmoji.unicode("🇧");
	public static final ReactionEmoji.Unicode C_UNICODE = ReactionEmoji.unicode("🇨");
	public static final ReactionEmoji.Unicode D_UNICODE = ReactionEmoji.unicode("🇩");
	public static final ReactionEmoji.Unicode E_UNICODE = ReactionEmoji.unicode("🇪");
	public static final ReactionEmoji.Unicode F_UNICODE = ReactionEmoji.unicode("🇫");
	public static final ReactionEmoji.Unicode G_UNICODE = ReactionEmoji.unicode("🇬");
	public static final ReactionEmoji.Unicode H_UNICODE = ReactionEmoji.unicode("🇭");
	public static final ReactionEmoji.Unicode I_UNICODE = ReactionEmoji.unicode("🇮");
	public static final ReactionEmoji.Unicode J_UNICODE = ReactionEmoji.unicode("🇯");

	public static final ReactionEmoji.Unicode ZERO_UNICODE = ReactionEmoji.unicode("0️⃣");
	public static final ReactionEmoji.Unicode ONE_UNICODE = ReactionEmoji.unicode("1️⃣");
	public static final ReactionEmoji.Unicode TWO_UNICODE = ReactionEmoji.unicode("2️⃣");
	public static final ReactionEmoji.Unicode THREE_UNICODE = ReactionEmoji.unicode("3️⃣");
	public static final ReactionEmoji.Unicode FOUR_UNICODE = ReactionEmoji.unicode("4️⃣");
	public static final ReactionEmoji.Unicode FIVE_UNICODE = ReactionEmoji.unicode("5️⃣");
	public static final ReactionEmoji.Unicode SIX_UNICODE = ReactionEmoji.unicode("6️⃣");
	public static final ReactionEmoji.Unicode SEVEN_UNICODE = ReactionEmoji.unicode("7️⃣");
	public static final ReactionEmoji.Unicode EIGHT_UNICODE = ReactionEmoji.unicode("8️⃣");
	public static final ReactionEmoji.Unicode NINE_UNICODE = ReactionEmoji.unicode("9️⃣");
	public static final ReactionEmoji.Unicode RED_X_UNICODE = ReactionEmoji.unicode("❌");

	private static final Map<Integer, Unicode> UNICODE_NUM_MAP;

	static {
		UNICODE_NUM_MAP = new HashMap<Integer, Unicode>();
		UNICODE_NUM_MAP.put(0, ZERO_UNICODE);
		UNICODE_NUM_MAP.put(1, ONE_UNICODE);
		UNICODE_NUM_MAP.put(2, TWO_UNICODE);
		UNICODE_NUM_MAP.put(3, THREE_UNICODE);
		UNICODE_NUM_MAP.put(4, FOUR_UNICODE);
		UNICODE_NUM_MAP.put(5, FIVE_UNICODE);
		UNICODE_NUM_MAP.put(6, SIX_UNICODE);
		UNICODE_NUM_MAP.put(7, SEVEN_UNICODE);
		UNICODE_NUM_MAP.put(8, EIGHT_UNICODE);
		UNICODE_NUM_MAP.put(9, NINE_UNICODE);
	}

	public static final ReactionEmoji.Unicode LEFT_ARROW = ReactionEmoji.unicode("◀️");
	public static final ReactionEmoji.Unicode RIGHT_ARROW = ReactionEmoji.unicode("▶️");

	public static final String A_PLAIN = ":regional_indicator_a:";
	public static final String B_PLAIN = ":regional_indicator_b:";
	public static final String C_PLAIN = ":regional_indicator_c:";
	public static final String D_PLAIN = ":regional_indicator_d:";
	public static final String E_PLAIN = ":regional_indicator_e:";
	public static final String F_PLAIN = ":regional_indicator_f:";
	public static final String G_PLAIN = ":regional_indicator_g:";
	public static final String H_PLAIN = ":regional_indicator_h:";
	public static final String I_PLAIN = ":regional_indicator_i:";
	public static final String J_PLAIN = ":regional_indicator_j:";

	public static final String ZERO = ":zero:";
	public static final String ONE = ":one:";
	public static final String TWO = ":two:";
	public static final String THREE = ":three:";
	public static final String FOUR = ":four:";
	public static final String FIVE = ":five:";
	public static final String SIX = ":six:";
	public static final String SEVEN = ":seven:";
	public static final String EIGHT = ":eight:";
	public static final String NINE = ":nine:";

	private static final Map<Character, String> NUM_MAP;

	static {
		NUM_MAP = new HashMap<Character, String>();
		NUM_MAP.put('0', ZERO);
		NUM_MAP.put('1', ONE);
		NUM_MAP.put('2', TWO);
		NUM_MAP.put('3', THREE);
		NUM_MAP.put('4', FOUR);
		NUM_MAP.put('5', FIVE);
		NUM_MAP.put('6', SIX);
		NUM_MAP.put('7', SEVEN);
		NUM_MAP.put('8', EIGHT);
		NUM_MAP.put('9', NINE);
	}

	public static final String NEXT_TRACK = ":next_track:";
	public static final String STOP_SIGN = ":stop_sign:";
	public static final String CHECK_MARK = ":white_check_mark:";
	public static final String NO_ENTRY = ":no_entry_sign:";
	public static final String MUTE = ":mute:";
	public static final String SOUND = ":sound:";
	public static final String RED_X = ":x:";
	public static final String DICE = ":game_die:";
	public static final String MEMO = ":memo:";
	public static final String NOTES = ":notes:";
	public static final String LOOP = ":loop:";
	public static final String SHUFFLE = ":twisted_rightwards_arrows:";
	public static final String REPEAT = ":repeat:";
	public static final String LEFT = ":arrow_left:";
	public static final String RIGHT = ":arrow_right:";

	public static final Unicode numToUnicode(int num) {
		return UNICODE_NUM_MAP.get(num);
	}

	public static final int unicodeToNum(Unicode unicode) {
		return keys(UNICODE_NUM_MAP, unicode).findFirst().orElse(-1);
	}

	private static <K, V> Stream<K> keys(Map<K, V> map, V value) {
		return map.entrySet().stream().filter(entry -> value.equals(entry.getValue())).map(Map.Entry::getKey);
	}

	public static final String numToEmoji(String num) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num.length(); i++) {
			sb.append(NUM_MAP.get(num.charAt(i)));
		}
		return sb.toString();
	}

	public static final String numToEmoji(int num) {
		return numToEmoji(Integer.toString(num));
	}

	public static final ReactionEmoji.Unicode getUnicodeFromNum(int num) {

		switch (num) {
		case 0:
			return A_UNICODE;
		case 1:
			return B_UNICODE;
		case 2:
			return C_UNICODE;
		case 3:
			return D_UNICODE;
		case 4:
			return E_UNICODE;
		case 5:
			return F_UNICODE;
		case 6:
			return G_UNICODE;
		case 7:
			return H_UNICODE;
		case 8:
			return I_UNICODE;
		case 9:
			return J_UNICODE;
		default:
		}

		throw new IllegalArgumentException("No unicode character found for " + num);
	}

	public static final String getPlainLetterFromNum(int num) {
		switch (num) {
		case 0:
			return A_PLAIN;
		case 1:
			return B_PLAIN;
		case 2:
			return C_PLAIN;
		case 3:
			return D_PLAIN;
		case 4:
			return E_PLAIN;
		case 5:
			return F_PLAIN;
		case 6:
			return G_PLAIN;
		case 7:
			return H_PLAIN;
		case 8:
			return I_PLAIN;
		case 9:
			return J_PLAIN;
		default:
		}
		throw new IllegalArgumentException("No plain letter found for " + num);
	}

}
