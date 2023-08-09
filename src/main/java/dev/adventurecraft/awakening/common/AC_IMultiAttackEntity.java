package dev.adventurecraft.awakening.common;

import net.minecraft.entity.Entity;

public interface AC_IMultiAttackEntity {

    default boolean attackEntityFromMulti(Entity entity, int damage) {
        return false;
    }
}
