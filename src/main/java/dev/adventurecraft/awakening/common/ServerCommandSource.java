package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ServerCommandSource {

    public static final String COMMAND_PROMPT = "/";

    private final Minecraft client;
    private final Level level;
    private final Entity entity;

    public ServerCommandSource(Minecraft client, Level level, Entity entity) {
        this.client = client;
        this.level = level;
        this.entity = entity;
    }

    public Minecraft getClient() {
        return this.client;
    }

    public Level getLevel() {
        return this.level;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
