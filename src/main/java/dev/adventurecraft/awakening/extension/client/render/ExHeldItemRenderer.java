package dev.adventurecraft.awakening.extension.client.render;

public interface ExHeldItemRenderer {

    void renderItemInFirstPerson(float partialTick, float prog1, float prog2);

    void renderItemInFirstPerson(float partialTick, float prog1, float prog2,
        dev.adventurecraft.awakening.client.render.state.DrawParameters parameters);

    void renderItem(net.minecraft.world.entity.Mob mob,net.minecraft.world.ItemInstance item,
        dev.adventurecraft.awakening.client.render.state.DrawParameters parameters);

    boolean hasItem();
}
