package dev.adventurecraft.awakening.mixin.client.sound;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundHelper;
import net.minecraft.client.sound.SoundMap;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

@Mixin(SoundHelper.class)
public abstract class MixinSoundHelper implements ExSoundHelper {

    @Shadow
    private static SoundSystem soundSystem;
    @Shadow
    private SoundMap streaming;
    @Shadow
    private GameOptions gameOptions;
    @Shadow
    private static boolean initialized;

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
            soundSystem.fadeOutIn("BgMusic", entry.soundUrl, entry.soundName, var2, var3);
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
}
