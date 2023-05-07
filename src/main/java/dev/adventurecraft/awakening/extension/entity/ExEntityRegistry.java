package dev.adventurecraft.awakening.extension.entity;

import net.minecraft.entity.EntityRegistry;

import javax.swing.text.html.parser.Entity;

public interface ExEntityRegistry {

    static String getEntityStringClimbing(Entity entity) {
        String name = null;

        Class<?> entityClass = entity.getClass();
        while (name == null && entityClass != null) {
            name = (String) EntityRegistry.CLASS_TO_STRING_ID.get(entityClass);
            entityClass = entityClass.getSuperclass();
        }

        return name;
    }
}
