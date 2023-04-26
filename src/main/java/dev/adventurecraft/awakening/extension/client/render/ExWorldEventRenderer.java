package dev.adventurecraft.awakening.extension.client.render;

import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ExWorldEventRenderer {

    void drawCursorSelection(LivingEntity var1, ItemStack var2, float var3);

    void drawEntityPath(Entity var1, LivingEntity var2, float var3);

    void drawEntityFOV(LivingEntity var1, LivingEntity var2, float var3);

    ParticleEntity spawnParticleR(String var1, double var2, double var4, double var6, double var8, double var10, double var12);

    void setAllRenderersVisible();

    int renderAllSortedRenderers(int var1, double var2);

    void updateAllTheRenderers();

    void resetAll();

    void resetForDeath();
}
