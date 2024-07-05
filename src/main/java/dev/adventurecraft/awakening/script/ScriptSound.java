package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import net.minecraft.client.sound.SoundHelper;

@SuppressWarnings("unused")
public class ScriptSound {

    SoundHelper soundMgr;

    ScriptSound(SoundHelper var1) {
        this.soundMgr = var1;
    }

    public void playSoundUI(String var1) {
        this.soundMgr.playSound(var1.toLowerCase(), 1.0F, 1.0F);
    }

    public void playSoundUI(String var1, float var2, float var3) {
        this.soundMgr.playSound(var1.toLowerCase(), var2, var3);
    }

    public void playSound3D(String var1, float var2, float var3, float var4) {
        this.soundMgr.playSound(var1.toLowerCase(), var2, var3, var4, 1.0F, 1.0F);
    }

    public void playSound3D(String var1, float var2, float var3, float var4, float var5, float var6) {
        this.soundMgr.playSound(var1.toLowerCase(), var2, var3, var4, var5, var6);
    }

    public void playMusic(String var1) {
        playMusic(var1, 0, 0);
    }

    public String getMusic() {
        return ((ExSoundHelper) this.soundMgr).getMusicFromStreaming();
    }

    public void playMusic(String var1, int var2, int var3) {
        ((ExSoundHelper) this.soundMgr).playMusicFromStreaming(var1.toLowerCase(), var2, var3);
    }

    public void stopMusic() {
        ((ExSoundHelper) this.soundMgr).stopMusic();
    }
}
