package dev.adventurecraft.awakening.extension.client.render.block;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.image.ImageLoader;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;

import java.nio.IntBuffer;

public interface ExGrassColor {

    static int getBaseColor(int meta) {
        return GrassColor.pixels[0];
    }

    static void loadGrass(String fileName, Level world) {
        var image = ((ExWorld) world).loadMapTexture(fileName);
        if (image == null) {
            try {
                var url = GrassColor.class.getResource(fileName);
                if (url != null) {
                    image = ImageLoader.load(url, 4);
                }
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        if (image != null) {
            image.copyTo(IntBuffer.wrap(GrassColor.pixels), ImageFormat.BGRA_U8);
        }
    }
}
