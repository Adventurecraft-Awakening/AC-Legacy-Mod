package dev.adventurecraft.awakening.extension.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.entity.player.Player;

public interface ExEntityRegistry {

    static String getEntityClassType(Entity entity) {
        if (entity instanceof Player) {
            return "Player";
        }
        return ExEntityRegistry.getEntityStringClimbing(entity);
    }

    static String getEntityStringClimbing(Entity entity) {
        String name = null;
        Class<?> next = entity.getClass();
        while (name == null && next != null) {
            name = (String) EntityIO.classIdMap.get(next);
            next = next.getSuperclass();
        }
        return name;
    }
}
