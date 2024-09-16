package dev.adventurecraft.awakening.extension.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;

public interface ExEntityRegistry {

    static String getEntityStringClimbing(Entity entity) {
        String name = null;

        Class<?> entityClass = entity.getClass();
        while (name == null && entityClass != null) {
            name = (String) EntityIO.classIdMap.get(entityClass);
            entityClass = entityClass.getSuperclass();
        }

        return name;
    }
}
