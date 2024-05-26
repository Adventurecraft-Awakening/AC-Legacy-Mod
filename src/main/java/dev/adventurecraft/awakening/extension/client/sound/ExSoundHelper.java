package dev.adventurecraft.awakening.extension.client.sound;

import java.net.URL;

public interface ExSoundHelper {

    void playMusicFromStreaming(String id, int var2, int var3);

    String getMusicFromStreaming();

    void stopMusic();

    void addSound(String id, URL url);

    void addStreaming(String id, URL url);

    void addMusic(String id, URL url);
}
