package dev.adventurecraft.awakening.extension.client;

import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.common.Vec2;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.resource.TexturePack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public interface ExTextureManager {

    <T extends TextureBinder> T getTextureBinder(Class<T> type);

    BufferedImage getTextureImage(String var1) throws IOException;

    void loadTexture(int var1, String var2);

    Vec2 getTextureResolution(String var1);

    void clearTextureAnimations();

    void registerTextureAnimation(String var1, AC_TextureAnimated var2);

    void unregisterTextureAnimation(String var1);

    void updateTextureAnimations();

    void replaceTexture(String var1, String var2);

    void revertTextures();

    static BufferedImage scaleBufferedImage(BufferedImage var0, int var1, int var2) {
        BufferedImage var3 = new BufferedImage(var1, var2, 2);
        Graphics2D var4 = var3.createGraphics();
        var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        var4.drawImage(var0, 0, 0, var1, var2, null);
        return var3;
    }

    static InputStream getAssetStream(TexturePack pack, String name) {
        String acName = "/assets/adventurecraft" + name;
        InputStream stream = pack.getResourceAsStream(acName);
        if (stream == null) {
            stream = pack.getResourceAsStream(name);
        }
        return stream;
    }
}
