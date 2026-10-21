package dev.adventurecraft.awakening.extension.client.model;

import org.lwjgl.util.vector.Matrix4f;

public interface ExCuboid {

    void addBoxInverted(float var1, float var2, float var3, int var4, int var5, int var6, float var7);

    void setTWidth(int value);

    void setTHeight(int value);

    boolean canRender();

    void render();

    void render(Matrix4f modelView, float[] tint);

    void render(dev.adventurecraft.awakening.client.render.ModelRenderContext context,
                dev.adventurecraft.awakening.client.render.state.Matrix4 model,
                dev.adventurecraft.awakening.client.render.state.Color4 tint);

    void render(dev.adventurecraft.awakening.client.render.ModelRenderContext context,
                dev.adventurecraft.awakening.client.render.state.Matrix4 model,
                dev.adventurecraft.awakening.client.render.state.Color4 tint,float scale,boolean rollable);

    dev.adventurecraft.awakening.client.render.state.Matrix4 pose(
        dev.adventurecraft.awakening.client.render.state.Matrix4 parent,float scale,boolean rollable);

    void translateTo(Matrix4f transform);
}
