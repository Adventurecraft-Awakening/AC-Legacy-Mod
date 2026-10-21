package dev.adventurecraft.awakening.extension.client.render;

import dev.adventurecraft.awakening.client.render.chunk.ChunkBuildStatistics;
import dev.adventurecraft.awakening.client.render.lighting.TorchLightSections;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public interface ExWorldEventRenderer {

    void ac$updateChunks(Mob camera, long deadline);

    ChunkBuildStatistics ac$chunkBuildStatistics();

    long ac$beginTorchLightUpdate(Level level);

    void ac$endTorchLightUpdate(Level level, long epoch, TorchLightSections sections);

    int ac$lastTorchDirtySections();

    int ac$lastTorchDirtyChunks();

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
