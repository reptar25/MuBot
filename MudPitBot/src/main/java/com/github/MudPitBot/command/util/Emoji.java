package com.github.MudPitBot.command.util;

import discord4j.core.object.reaction.ReactionEmoji;

public abstract class Emoji {

	public static final ReactionEmoji.Unicode A = ReactionEmoji.unicode("\uD83C\uDDE6");
	public static final ReactionEmoji.Unicode B = ReactionEmoji.unicode("\uD83C\uDDE7");
	public static final ReactionEmoji.Unicode C = ReactionEmoji.unicode("\uD83C\uDDE8");
	public static final ReactionEmoji.Unicode D = ReactionEmoji.unicode("\uD83C\uDDE9");
	public static final ReactionEmoji.Unicode E = ReactionEmoji.unicode("\uD83C\uDDEA");
	public static final ReactionEmoji.Unicode F = ReactionEmoji.unicode("\uD83C\uDDEB");
	public static final ReactionEmoji.Unicode G = ReactionEmoji.unicode("\uD83C\uDDEC");
	public static final ReactionEmoji.Unicode H = ReactionEmoji.unicode("\uD83C\uDDED");
	public static final ReactionEmoji.Unicode I = ReactionEmoji.unicode("\uD83C\uDDEE");
	public static final ReactionEmoji.Unicode J = ReactionEmoji.unicode("\uD83C\uDDEF");

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

	public static final String NEXT_TRACK = ":next_track:";
	public static final String STOP_SIGN = ":stop_sign:";
	public static final String CHECK_MARK = ":white_check_mark:";
	public static final String NO_ENTRY = ":no_entry_sign:";
	public static final String MUTE = ":mute:";
	public static final String SOUND = ":sound:";
	public static final String RED_X = ":x:";
	public static final String DICE = ":game_die:";

	public static final ReactionEmoji.Unicode getUnicodeFromNum(int num) {

		switch (num) {
		case 0:
			return A;
		case 1:
			return B;
		case 2:
			return C;
		case 3:
			return D;
		case 4:
			return E;
		case 5:
			return F;
		case 6:
			return G;
		case 7:
			return H;
		case 8:
			return I;
		case 9:
			return J;
		}

		return null;
	}

	public static final String getPlainFromNum(int num) {
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
		}
		return null;
	}

}
