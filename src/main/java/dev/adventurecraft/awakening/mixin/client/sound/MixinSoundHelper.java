package dev.adventurecraft.awakening.mixin.client.sound;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundMap;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundRepository;
import net.minecraft.world.entity.Mob;
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

@Mixin(SoundEngine.class)
public abstract class MixinSoundHelper implements ExSoundHelper {

    @Shadow
    private static SoundSystem soundSystem;
    @Shadow
    private SoundRepository sounds;
    @Shadow
    private SoundRepository streamingSounds;
    @Shadow
    private SoundRepository songs;
    @Shadow
    private Options options;
    @Shadow
    private static boolean loaded;

    @Unique
    private String currentSoundName;

    @Inject(method = "playMusicTick", at = @At("HEAD"), cancellable = true)
    private void disableRandomBackgroundMusic(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lpaulscode/sound/SoundSystem;setListenerOrientation(FFFFFF)V",
            shift = At.Shift.AFTER,
            remap = false))
    private void setBgMusicPosition(
        Mob var1,
        float var2,
        CallbackInfo ci,
        @Local(ordinal = 0) double var4,
        @Local(ordinal = 1) double var6,
        @Local(ordinal = 2) double var8) {
        soundSystem.setPosition("BgMusic", (float) var4, (float) var6, (float) var8);
    }

    public String getMusicFromStreaming() {
        if (Minecraft.instance.level != null) {
            // getPlayingMusic with substring 6 to get rid of "music." at the beginning the music name
            String curMusic = ((ExWorldProperties) Minecraft.instance.level.levelData).getPlayingMusic();
            curMusic = curMusic.length() <= 0 ? "" : curMusic.substring(6);
            return curMusic;
        } else {
            return "";
        }
    }

    @Override
    public void playMusicFromStreaming(String id, int var2, int var3) {
        if (!loaded) {
            return;
        }

        if (id.isEmpty()) {
            this.stopMusic();
        }

        Sound entry = this.streamingSounds.get(id);
        if (entry == null) {
            return;
        }

        if (soundSystem.playing("BgMusic")) {
            if (this.currentSoundName.equals(entry.name)) {
                return;
            }
            // In case there is no fadeIn and fadeOut value, just start playing the music (fadeOutIn doesn't work with 0 values)
            if (var2 == 0 && var3 == 0) {
                soundSystem.backgroundMusic("BgMusic", entry.url, entry.name, true);
            } else {
                soundSystem.fadeOutIn("BgMusic", entry.url, entry.name, var2, var3);
            }
        } else {
            soundSystem.backgroundMusic("BgMusic", entry.url, entry.name, true);
        }

        soundSystem.setVolume("BgMusic", this.options.music);
        soundSystem.play("BgMusic");
        this.currentSoundName = entry.name;
        if (Minecraft.instance.level != null) {
            ((ExWorldProperties) Minecraft.instance.level.levelData).setPlayingMusic(id);
        }
    }

    @Override
    public void stopMusic() {
        if (!loaded) {
            return;
        }

        if (soundSystem != null && soundSystem.playing("BgMusic")) {
            soundSystem.stop("BgMusic");
            if (Minecraft.instance.level != null) {
                ((ExWorldProperties) Minecraft.instance.level.levelData).setPlayingMusic("");
            }
        }
    }

    @Override
    public void addSound(String id, URL url) {
        ((ExSoundMap) this.sounds).addSound(id, url);
    }

    @Override
    public void addStreaming(String id, URL url) {
        ((ExSoundMap) this.streamingSounds).addSound(id, url);
    }

    @Override
    public void addMusic(String id, URL url) {
        ((ExSoundMap) this.songs).addSound(id, url);
    }

    @ModifyArgs(
        method = "play*", // TODO: check what this actually applies to
        at = @At(
            value = "INVOKE",
            target = "Lpaulscode/sound/SoundSystem;newSource(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V",
            remap = false))
    private void useUrlForSourceName(Args args) {
        URL url = args.get(2);
        args.set(3, url.toString());
    }
}
