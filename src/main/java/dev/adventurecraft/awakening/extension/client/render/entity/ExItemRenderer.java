package dev.adventurecraft.awakening.extension.client.render.entity;

public interface ExItemRenderer {

    void setScale(float value);
    void renderGuiItem(dev.adventurecraft.awakening.client.render.UiDrawContext ui,net.minecraft.world.ItemInstance stack,int x,int y);
    void renderGuiItemDecorations(dev.adventurecraft.awakening.client.render.UiDrawContext ui,net.minecraft.world.ItemInstance stack,int x,int y);
}
