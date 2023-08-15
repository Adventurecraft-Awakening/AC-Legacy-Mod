package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.render.TextureBinder;

public interface AC_TextureBinder {

    void onTick(Vec2 var1);

    void loadImage();

    void loadImage(String name);

    String getTexture();

    static <T extends TextureBinder> void loadImages(ExTextureManager texManager, Class<T> type) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage());
    }

    static <T extends TextureBinder> void loadImages(ExTextureManager texManager, Class<T> type, String name) {
        texManager.getTextureBinders(type).forEach(b -> ((AC_TextureBinder) b).loadImage(name));
    }
}
