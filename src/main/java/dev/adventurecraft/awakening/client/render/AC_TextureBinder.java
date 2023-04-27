package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.common.Vec2;

public interface AC_TextureBinder {

    void onTick(Vec2 var1);

    void loadImage();

    void loadImage(String var0);

    String getTexture();
}
