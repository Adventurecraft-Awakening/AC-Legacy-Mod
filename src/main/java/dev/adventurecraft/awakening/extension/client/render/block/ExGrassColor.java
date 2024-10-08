package dev.adventurecraft.awakening.extension.client.render.block;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public interface ExGrassColor {

    static int getBaseColor(int meta) {
        return GrassColor.pixels[0];
    }

    static void loadGrass(String fileName, Level world) {
        BufferedImage var1 = ((ExWorld) world).loadMapTexture(fileName);
        if (var1 == null) {
            try {
                var1 = ImageIO.read(FoliageColor.class.getResource(fileName));
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        if (var1 != null) {
            var1.getRGB(0, 0, 256, 256, GrassColor.pixels, 0, 256);
        }
    }
}
