package dev.adventurecraft.awakening.mixin.client.sound;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundMap;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundHelper;
import net.minecraft.client.sound.SoundMap;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import paulscode.sound.SoundSystem;

import java.net.URL;

@Mixin(SoundHelper.class)
public abstract class MixinSoundHelper implements ExSoundHelper {

    @Shadow
    private static SoundSystem soundSystem;
    @Shadow
    private SoundMap sounds;
    @Shadow
    private SoundMap streaming;
    @Shadow
    private SoundMap music;
    @Shadow
    private GameOptions gameOptions;
    @Shadow
    private static boolean initialized;

    @Unique
    private String currentSoundName;

    @Inject(method = "handleBackgroundMusic", at = @At("HEAD"), cancellable = true)
    private void disableRandomBackgroundMusic(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
        method = "setSoundPosition",
        at = @At(
            value = "INVOKE",
            target = "Lpaulscode/sound/SoundSystem;setListenerOrientation(FFFFFF)V",
            shift = At.Shift.AFTER,
            remap = false))
    private void setBgMusicPosition(
        LivingEntity var1,
        float var2,
        CallbackInfo ci,
        @Local(ordinal = 0) double var4,
        @Local(ordinal = 1) double var6,
        @Local(ordinal = 2) double var8) {
        soundSystem.setPosition("BgMusic", (float) var4, (float) var6, (float) var8);
    }

    public String getMusicFromStreaming() {
        if (Minecraft.instance.world != null) {
            // getPlayingMusic with substring 6 to get rid of "music." at the beginning the music name
            return ((ExWorldProperties) Minecraft.instance.world.properties).getPlayingMusic().substring(6);
        }
        else {
            return "";
        }
    }

    @Override
    public void playMusicFromStreaming(String id, int var2, int var3) {
        if (!initialized) {
            return;
        }

        if (id.equals("")) {
            this.stopMusic();
        }

        SoundEntry entry = this.streaming.getRandomSoundForId(id);
        if (entry == null) {
            return;
        }

        if (soundSystem.playing("BgMusic")) {
            if (this.currentSoundName.equals(entry.soundName)) {
                return;
            }
            // In case there is no fadeIn and fadeOut value, just start playing the music (fadeOutIn doesn't work with 0 values)
            if(var2 == 0 && var3 == 0){
                soundSystem.backgroundMusic("BgMusic", entry.soundUrl, entry.soundName, true);
            } else {
                soundSystem.fadeOutIn("BgMusic", entry.soundUrl, entry.soundName, var2, var3);
            }
        } else {
            soundSystem.backgroundMusic("BgMusic", entry.soundUrl, entry.soundName, true);
        }

        soundSystem.setVolume("BgMusic", this.gameOptions.musicVolume);
        soundSystem.play("BgMusic");
        this.currentSoundName = entry.soundName;
        if (Minecraft.instance.world != null) {
            ((ExWorldProperties) Minecraft.instance.world.properties).setPlayingMusic(id);
        }
    }

    @Override
    public void stopMusic() {
        if (!initialized) {
            return;
        }

        if (soundSystem != null && soundSystem.playing("BgMusic")) {
            soundSystem.stop("BgMusic");
            if (Minecraft.instance.world != null) {
                ((ExWorldProperties) Minecraft.instance.world.properties).setPlayingMusic("");
            }
        }
    }

    @Override
    public void addSound(String id, URL url) {
        ((ExSoundMap) this.sounds).addSound(id, url);
    }

    @Override
    public void addStreaming(String id, URL url) {
        ((ExSoundMap) this.streaming).addSound(id, url);
    }

    @Override
    public void addMusic(String id, URL url) {
        ((ExSoundMap) this.music).addSound(id, url);
    }

    @ModifyArgs(
        method = "playSound*",
        at = @At(
            value = "INVOKE",
            target = "Lpaulscode/sound/SoundSystem;newSource(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V"))
    private void useUrlForSourceName(Args args) {
        URL url = args.get(2);
        args.set(3, url.toString());
    }
}
