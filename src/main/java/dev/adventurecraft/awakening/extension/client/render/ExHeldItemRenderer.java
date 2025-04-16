package dev.adventurecraft.awakening.extension.client.render;

public interface ExHeldItemRenderer {

    void renderItemInFirstPerson(float partialTick, float prog1, float prog2);

    boolean hasItem();
}
