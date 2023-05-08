package dev.adventurecraft.awakening.extension.entity;

import net.minecraft.entity.Entity;

public interface ExEntity {

    boolean attackEntityFromMulti(Entity var1, int var2);

    boolean handleFlying();

    void setIsFlying(boolean value);

    boolean getCollidesWithClipBlocks();

    void setCollidesWithClipBlocks(boolean value);

    int getStunned();

    void setStunned(int value);

    int getCollisionX();

    int getCollisionZ();
}
