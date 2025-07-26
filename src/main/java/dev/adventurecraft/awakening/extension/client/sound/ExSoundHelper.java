package dev.adventurecraft.awakening.extension.client.sound;

import net.minecraft.world.level.Level;

import java.net.URL;

public interface ExSoundHelper {

    void playMusicFromStreaming(Level level, String id, int var2, int var3);

    String getMusicFromStreaming(Level level);

    void stopMusic(Level level);

    void addSound(String id, URL url);

    void addStreaming(String id, URL url);

    void addMusic(String id, URL url);
}
