package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ServerCommandSource {

    private final Minecraft client;
    private final Level world;
    private final Entity entity;

    public ServerCommandSource(Minecraft client, Level world, Entity entity) {
        this.client = client;
        this.world = world;
        this.entity = entity;
    }

    public Minecraft getClient() {
        return this.client;
    }

    public Level getWorld() {
        return this.world;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
