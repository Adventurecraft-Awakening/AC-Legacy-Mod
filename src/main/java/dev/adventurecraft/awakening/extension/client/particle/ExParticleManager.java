package dev.adventurecraft.awakening.extension.client.particle;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxixAlignedBoundingBox;

import java.util.List;

public interface ExParticleManager {

    void getEffectsWithinAABB(AxixAlignedBoundingBox var1, List<Entity> destination);
}
