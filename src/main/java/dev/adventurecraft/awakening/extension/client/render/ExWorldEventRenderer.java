package dev.adventurecraft.awakening.extension.client.render;

import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ExWorldEventRenderer {

    void drawCursorSelection(LivingEntity entity, ItemStack item, float deltaTime);

    void drawEntityPath(Entity entity, LivingEntity viewEntity, float deltaTime);

    void drawEntityFOV(LivingEntity entity, LivingEntity viewEntity, float deltaTime);

    ParticleEntity spawnParticleR(String name, double x, double y, double z, double vX, double vY, double vZ);

    void setAllRenderersVisible();

    int renderAllSortedRenderers(int var1, double var2);

    void updateAllTheRenderers();

    void resetAll();

    void resetForDeath();
}
