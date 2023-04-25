package dev.adventurecraft.awakening.extension.client.render.block;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.FoliageColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public interface ExFoliageColor {

    static void loadFoliage(String var0) {
        BufferedImage var1 = ((ExWorld) Minecraft.instance.world).loadMapTexture(var0);
        if (var1 == null) {
            try {
                var1 = ImageIO.read(FoliageColor.class.getResource(var0));
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        if (var1 != null) {
            var1.getRGB(0, 0, 256, 256, FoliageColor.map, 0, 256);
        }
    }
}
