package dev.adventurecraft.awakening.entity;

import net.minecraft.world.entity.Entity;

public interface AC_IMultiAttackEntity {

    default boolean attackEntityFromMulti(Entity entity, int damage) {
        return false;
    }
}
