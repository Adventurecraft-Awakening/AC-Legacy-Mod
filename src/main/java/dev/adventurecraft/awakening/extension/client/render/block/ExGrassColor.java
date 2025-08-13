package dev.adventurecraft.awakening.extension.client.render.block;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.image.ImageLoadOptions;
import dev.adventurecraft.awakening.image.ImageLoader;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;

import java.nio.IntBuffer;

public interface ExGrassColor {

    static int getBaseColor(int meta) {
        return GrassColor.pixels[0];
    }

    static int get(float temperature, float downfall) {
        int x = (int) ((1f - temperature) * 255f);
        int y = (int) ((1f - (downfall * temperature)) * 255f);
        return GrassColor.pixels[(y << 8) | x];
    }

    static void loadGrass(String fileName, Level world) {
        var image = ((ExWorld) world).loadMapTexture(fileName);
        if (image == null) {
            try {
                var url = GrassColor.class.getResource(fileName);
                if (url != null) {
                    image = ImageLoader.load(url, ImageLoadOptions.withFormat(ImageFormat.RGBA_U8));
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
