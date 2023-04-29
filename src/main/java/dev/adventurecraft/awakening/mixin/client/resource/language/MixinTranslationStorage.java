package dev.adventurecraft.awakening.mixin.client.resource.language;

import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Mixin(TranslationStorage.class)
public abstract class MixinTranslationStorage implements ExTranslationStorage {

    @Shadow
    private Properties translations;

    private static InputStream loadResource(String name) {
        String acName = "/assets/adventurecraft" + name;
        InputStream stream = MixinTranslationStorage.class.getResourceAsStream(acName);
        if (stream == null) {
            stream = MixinTranslationStorage.class.getResourceAsStream(name);
        }
        return stream;
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"))
    private InputStream useLoadResource(Class<?> instance, String s) {
        return loadResource(s);
    }

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
            this.translations.load(loadResource("/lang/en_US.lang"));
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
