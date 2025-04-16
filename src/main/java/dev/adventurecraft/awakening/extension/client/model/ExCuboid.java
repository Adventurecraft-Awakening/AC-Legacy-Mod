package dev.adventurecraft.awakening.extension.client.model;

import org.lwjgl.util.vector.Matrix4f;

public interface ExCuboid {

    void addBoxInverted(float var1, float var2, float var3, int var4, int var5, int var6, float var7);

    void setTWidth(int value);

    void setTHeight(int value);

    void render();

    void translateTo(Matrix4f transform);
}
