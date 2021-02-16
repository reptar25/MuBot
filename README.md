# MuBot
[![codebeat badge](https://codebeat.co/badges/873a6429-f29d-4fb1-8e21-064723b5fd3d)](https://codebeat.co/projects/github-com-reptar25-mubot-dev)
[![CodeFactor](https://www.codefactor.io/repository/github/reptar25/mubot/badge)](https://www.codefactor.io/repository/github/reptar25/mubot)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/reptar25/MuBot.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/reptar25/MuBot/context:java)
![build](https://github.com/reptar25/MuBot/workflows/build/badge.svg)

A discord built with <a href="https://github.com/Discord4J/Discord4J">Discord4J</a> reactive framework.<br/><br/>
Can play music from YouTube/SoundCloud/Twitch/ect and even search YouTube for songs. Intuitive, reaction based menus for things like viewing the queue or search for music.
Can tell you jokes!
A full list of commands can be acquired using the `!help` command. 
You can use `help` after any command to get more information on how that command works, for example `!ban help`. Any feedback/issues/feature request are appreciated. 
Thanks for checking it out!

List of current commands:<br/>
<li>`!ban` - Ban a user and delete their messages from the last 7 days.<br/>
<li>`!clear` - Clears the queue of all songs.<br/>
<li>`!echo` - Bot replies with a simple echo message.<br/>
<li>`!fastforward` - Fast fowards the currently playing song by the given amount of seconds.<br/>
<li>`!help` - Displays a list of available commands you can use with the bot.<br/>
<li>`!join` - Requests the bot to join the same voice channel as user who used the command.<br/>
<li>`!joke` - Tells a random joke from the chosen category of jokes.<br/>
<li>`!kick` - Kick a user from the server but not ban them.<br/>
<li>`!leave` - Requests the bot to leave its' current voice channel.<br/>
<li>`!mutechannel` - Mutes the voice channel of the user who used the command. Will also mute any new users that join that channel until this command is used again to unmute the channel.<br/>
<li>`!nowplaying` - Displays currently playing song.<br/>
<li>`!pause` - Pauses currently playing track.<br/>
<li>`!play` - Plays the song(s) from the given url.<br/>
<li>`!poll` - Creates a simple poll in the channel the command was used in. Allows up to 10 choices. All arguments must be contained in quotes to allow for spaces.<br/>
<li>`!remove` - Removes the song at the given position number from the queue.<br/>
<li>`!repeat` - Toggles repeating the currently playing song. Use this command again to enable/disable repeating.<br/>
<li>`!rewind` - Rewinds the currently playing song by the given amount of seconds.<br/>
<li>`!roll` - Rolls a dice of the given amount.<br/>
<li>`!search` - Searches YouTube for the given terms and returns the top 5 results as choices that can be added to the queue of songs.<br/>
<li>`!seek` - Moves the currently playing song to the given time.<br/>
<li>`!setprefix` - Set the command-prefix of this server.<br/>
<li>`!shuffle` - Shuffles the songs that are in the queue.<br/>
<li>`!skip` - Skips the currently playing song and plays the next song in the queue or skips to the specific song number in the queue.<br/>
<li>`!stop` - Stops the currently playing song and clears all songs from the queue.<br/>
<li>`!unban` - Unban a user.<br/>
<li>`!viewqueue` - Displays all the songs currently in the queue.<br/>
<li>`!volume` - Changes the volume to the given amount, or to the default amount if reset is given, or no argument to get the current volume.<br/>