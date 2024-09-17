package dev.adventurecraft.awakening.extension.client.particle;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public interface ExParticleManager {

    void getEffectsWithinAABB(AABB var1, List<Entity> destination);
}
