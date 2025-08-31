package dev.adventurecraft.awakening.mixin.client.resource.language;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import net.minecraft.locale.I18n;

@Mixin(I18n.class)
public abstract class MixinTranslationStorage implements ExTranslationStorage {

    private Properties initialTranslations;

    @Shadow
    private Properties keys;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void copyInitialTranslations(CallbackInfo ci) {
        this.initialTranslations = (Properties) this.keys.clone();

        this.loadAcTranslations();
    }

    @Environment(EnvType.CLIENT)
    @Redirect(
        method = "getDescriptionString",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Properties;getProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
        ))
    private String returnKeyNameOnEmpty(
        Properties instance,
        String key,
        String defaultValue,
        @Local(argsOnly = true) String originalKey) {
        // TODO: remove behavior of returning key when it's missing from translation files?
        String value = instance.getProperty(key, defaultValue);
        if (originalKey != null && value.isEmpty()) {
            String[] parts = originalKey.split("\\.");
            value = parts[parts.length - 1];
            instance.setProperty(key, value);
            ACMod.LOGGER.warn("Missing language entry for key \"{}\", falling back with value \"{}\".", key, value);
        }
        return value;
    }

    private void loadAcTranslations(String name) {
        try {
            String acName = "/assets/" + ACMod.MOD_ID + name;
            InputStream stream = ACMod.class.getResourceAsStream(acName);
            if (stream != null) {
                this.keys.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // TODO: ACMod.LOGGER.warn about resource load
            e.printStackTrace();
        }
    }

    private void loadAcTranslations() {
        this.loadAcTranslations("/lang/en_US.lang");
    }

    private void reset() {
        this.keys.clear();
        this.keys.putAll(this.initialTranslations);

        this.loadAcTranslations();
    }

    @Override
    public void loadMapTranslation(File mapPath) {
        this.reset();

        try {
            var file = new File(mapPath, "/lang/en_US.lang");
            if (file.exists()) {
                var stream = new FileInputStream(file);
                this.keys.load(new InputStreamReader(stream, StandardCharsets.ISO_8859_1));
            }
        } catch (IOException var4) {
            // TODO: ACMod.LOGGER.warn about resource load
        }
    }
}
