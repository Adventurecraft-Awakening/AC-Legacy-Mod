package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ServerCommandSource {

    private final Minecraft client;
    private final World world;
    private final Entity entity;

    public ServerCommandSource(Minecraft client, World world, Entity entity) {
        this.client = client;
        this.world = world;
        this.entity = entity;
    }

    public Minecraft getClient() {
        return this.client;
    }

    public World getWorld() {
        return this.world;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
