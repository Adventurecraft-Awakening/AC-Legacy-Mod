package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class ScriptSound {

    final Level level;
    final SoundEngine soundMgr;

    ScriptSound(Level level, SoundEngine engine) {
        this.level = level;
        this.soundMgr = engine;
    }

    public void playSoundUI(String fileName) {
        this.soundMgr.playUI(fileName.toLowerCase(), 1.0F, 1.0F);
    }

    public void playSoundUI(String fileName, float volume, float pitch) {
        this.soundMgr.playUI(fileName.toLowerCase(), volume, pitch);
    }

    public void playSound3D(String fileName, float x, float y, float z) {
        this.soundMgr.play(fileName.toLowerCase(), x, y, z, 1.0F, 1.0F);
    }

    public void playSound3D(String fileName, float x, float y, float z, float volume, float pitch) {
        this.soundMgr.play(fileName.toLowerCase(), x, y, z, volume, pitch);
    }

    public void playMusic(String fileName) {
        playMusic(fileName, 0, 0);
    }

    public String getMusic() {
        return ((ExSoundHelper) this.soundMgr).getMusicFromStreaming(this.level);
    }

    public void playMusic(String fileName, int fadeIn, int fadeOut) {
        ((ExSoundHelper) this.soundMgr).playMusicFromStreaming(this.level, fileName.toLowerCase(), fadeIn, fadeOut);
    }

    public void stopMusic() {
        ((ExSoundHelper) this.soundMgr).stopMusic(this.level);
    }
}
