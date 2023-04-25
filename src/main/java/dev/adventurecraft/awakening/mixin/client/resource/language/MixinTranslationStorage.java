package dev.adventurecraft.awakening.mixin.client.resource.language;

import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Mixin(TranslationStorage.class)
public abstract class MixinTranslationStorage implements ExTranslationStorage {

    @Shadow
    private Properties translations;

    @Overwrite
    public String translateNameOrEmpty(String var1) {
        String var2 = this.translations.getProperty(var1 + ".name", "");
        if (var2.equals("") && var1 != null) {
            String[] var3 = var1.split("\\.");
            var2 = var3[var3.length - 1];
            this.translations.setProperty(var1, var2);
        }

        return var2;
    }

    @Override
    public void loadMapTranslation(File var1) {
        try {
            this.translations.load(MixinTranslationStorage.class.getResourceAsStream("/lang/en_US.lang"));
        } catch (IOException var5) {
        }

        try {
            File var2 = new File(var1, "/lang/en_US.lang");
            if (var2.exists()) {
                FileInputStream var3 = new FileInputStream(var2);
                this.translations.load(var3);
            }
        } catch (IOException var4) {
        }
    }
}
