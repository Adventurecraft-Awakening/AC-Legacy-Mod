package dev.adventurecraft.awakening.extension.client.render;

import net.minecraft.client.entity.particle.ParticleEntity;

public interface ExWorldEventRenderer {

    ParticleEntity spawnParticleR(String var1, double var2, double var4, double var6, double var8, double var10, double var12);

    void setAllRenderersVisible();

    int renderAllSortedRenderers(int var1, double var2);

    void updateAllTheRenderers();

    void resetAll();

    void resetForDeath();
}
