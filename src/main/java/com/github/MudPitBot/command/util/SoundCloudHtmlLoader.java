package com.github.MudPitBot.command.util;

import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudHtmlDataLoader;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;

/**
 * 
 * Work around class until the lavaPlayer for D4J gets updated
 *
 */
public class SoundCloudHtmlLoader extends DefaultSoundCloudHtmlDataLoader {
	@Override
	protected String extractJsonFromHtml(String html) {
		return DataFormatTools.extractBetween(html, "catch(e){}})},", ");</script>");
	}
}