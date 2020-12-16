package com.github.MudPitBot.command.util;

import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudHtmlDataLoader;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;

public class SoundCloudHtmlLoader extends DefaultSoundCloudHtmlDataLoader {
	@Override
	protected String extractJsonFromHtml(String html) {
		return DataFormatTools.extractBetween(html, "catch(e){}})},", ");</script>");
	}
}