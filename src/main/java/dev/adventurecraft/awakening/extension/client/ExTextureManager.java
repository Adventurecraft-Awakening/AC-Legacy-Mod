package dev.adventurecraft.awakening.extension.client;

import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.common.Vec2;
import net.minecraft.client.render.TextureBinder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;

public interface ExTextureManager {

    <T extends TextureBinder> Stream<T> getTextureBinders(Class<T> type);

    BufferedImage getTextureImage(String name) throws IOException;

    void loadTexture(int id, String name);

    Vec2 getTextureResolution(String name);

    void clearTextureAnimations();

    void registerTextureAnimation(String animationName, AC_TextureAnimated animation);

    void unregisterTextureAnimation(String animationName);

    void replaceTexture(String keyName, String replacementName);

    void revertTextures();

    static BufferedImage scaleBufferedImage(BufferedImage var0, int var1, int var2) {
        BufferedImage var3 = new BufferedImage(var1, var2, 2);
        Graphics2D var4 = var3.createGraphics();
        var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        var4.drawImage(var0, 0, 0, var1, var2, null);
        return var3;
    }
}
