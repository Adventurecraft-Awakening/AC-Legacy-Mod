package dev.adventurecraft.awakening.extension.entity;

import dev.adventurecraft.awakening.common.AC_IMultiAttackEntity;

public interface ExEntity extends AC_IMultiAttackEntity {

    boolean handleFlying();

    void setIsFlying(boolean value);

    boolean getCollidesWithClipBlocks();

    void setCollidesWithClipBlocks(boolean value);

    int getStunned();

    void setStunned(int value);

    int getCollisionX();

    int getCollisionZ();
}
