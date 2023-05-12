package dev.adventurecraft.awakening.common;

public class AC_MusicScriptEntry {
	public String musicKey;
	public String songName;
	public String scriptFile;

	AC_MusicScriptEntry(String musicKey, String scriptFile, String songName) {
		this.musicKey = musicKey;
		this.songName = songName;
		this.scriptFile = scriptFile;
	}
}
