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

    public void playSoundUI(String var1) {
        this.soundMgr.playUI(var1.toLowerCase(), 1.0F, 1.0F);
    }

    public void playSoundUI(String var1, float var2, float var3) {
        this.soundMgr.playUI(var1.toLowerCase(), var2, var3);
    }

    public void playSound3D(String var1, float var2, float var3, float var4) {
        this.soundMgr.play(var1.toLowerCase(), var2, var3, var4, 1.0F, 1.0F);
    }

    public void playSound3D(String var1, float var2, float var3, float var4, float var5, float var6) {
        this.soundMgr.play(var1.toLowerCase(), var2, var3, var4, var5, var6);
    }

    public void playMusic(String var1) {
        playMusic(var1, 0, 0);
    }

    public String getMusic() {
        return ((ExSoundHelper) this.soundMgr).getMusicFromStreaming(this.level);
    }

    public void playMusic(String var1, int var2, int var3) {
        ((ExSoundHelper) this.soundMgr).playMusicFromStreaming(this.level, var1.toLowerCase(), var2, var3);
    }

    public void stopMusic() {
        ((ExSoundHelper) this.soundMgr).stopMusic(this.level);
    }
}
