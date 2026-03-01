package dev.adventurecraft.awakening.extension.client.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public interface ExWorldEventRenderer {

    void drawCursorSelection(Mob entity, ItemInstance stack, float deltaTime);

    void drawEntityPath(Entity entity, Mob viewEntity, float deltaTime);

    void drawEntityFOV(Mob entity, Mob viewEntity, float deltaTime);

    Particle spawnParticleR(String name, double x, double y, double z, double vX, double vY, double vZ);

    void setAllRenderersVisible();

    int renderAllSortedRenderers(int var1, double var2);

    void updateAllTheRenderers();

    void resetAll();

    void resetForDeath();
}
