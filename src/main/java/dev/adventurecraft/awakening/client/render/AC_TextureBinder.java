package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.world.World;

public interface AC_TextureBinder {

    void onTick(Vec2 var1);

    void loadImage(World world);

    void loadImage(String name, World world);

    String getTexture();

    static <T extends TextureBinder> void loadImages(ExTextureManager texManager, Class<T> type, World world) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage(world));
    }

    static <T extends TextureBinder> void loadImages(ExTextureManager texManager, Class<T> type, String name, World world) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage(name, world));
    }
}
