package dev.adventurecraft.awakening.mixin.client.resource.language;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Mixin(TranslationStorage.class)
public abstract class MixinTranslationStorage implements ExTranslationStorage {

    private Properties initialTranslations;

    @Shadow
    private Properties translations;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void copyInitialTranslations(CallbackInfo ci) {
        this.initialTranslations = (Properties) this.translations.clone();

        this.loadAcTranslations();
    }

    @ModifyArg(
        method = "translateNameOrEmpty",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Properties;getProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
        index = 1)
    private String returnKeyOnEmpty(String value, @Local(argsOnly = true) String key) {
        return key;
    }

    private void loadAcTranslations(String name) {
        try {
            String acName = "/assets/adventurecraft" + name;
            InputStream stream = ACMod.class.getResourceAsStream(acName);
            this.translations.load(stream);
        } catch (IOException e) {
            // TODO: ACMod.LOGGER.warn about resource load
            e.printStackTrace();
        }
    }

    private void loadAcTranslations() {
        this.loadAcTranslations("/lang/en_US.lang");
    }

    private void reset() {
        this.translations.clear();
        this.translations.putAll(this.initialTranslations);

        this.loadAcTranslations();
    }

    @Override
    public void loadMapTranslation(File mapPath) {
        this.reset();

        try {
            File file = new File(mapPath, "/lang/en_US.lang");
            if (file.exists()) {
                FileInputStream stream = new FileInputStream(file);
                this.translations.load(stream);
            }
        } catch (IOException var4) {
            // TODO: ACMod.LOGGER.warn about resource load
        }
    }
}
