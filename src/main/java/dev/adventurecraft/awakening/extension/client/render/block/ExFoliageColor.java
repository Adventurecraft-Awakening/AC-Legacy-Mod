package dev.adventurecraft.awakening.extension.client.render.block;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.image.ImageLoader;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;

import java.nio.IntBuffer;

public interface ExFoliageColor {

    static void loadFoliage(String fileName, Level world) {
        var image = ((ExWorld) world).loadMapTexture(fileName);
        if (image == null) {
            try {
                var url = FoliageColor.class.getResource(fileName);
                if (url != null) {
                    image = ImageLoader.load(url, 4);
                }
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        if (image != null) {
            image.copyTo(IntBuffer.wrap(FoliageColor.pixels), ImageFormat.BGRA_U8);
        }
    }
}
