package dev.adventurecraft.awakening.extension.client;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ExTextureManager {

    static BufferedImage scaleBufferedImage(BufferedImage var0, int var1, int var2) {
        BufferedImage var3 = new BufferedImage(var1, var2, 2);
        Graphics2D var4 = var3.createGraphics();
        var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        var4.drawImage(var0, 0, 0, var1, var2, null);
        return var3;
    }
}
