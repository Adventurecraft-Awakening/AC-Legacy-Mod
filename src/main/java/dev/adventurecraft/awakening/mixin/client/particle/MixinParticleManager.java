package dev.adventurecraft.awakening.mixin.client.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleManager implements ExParticleManager {

    @Shadow
    protected Level level;

    @Shadow
    private List<Particle>[] particles;

    @Shadow
    private Textures textureManager;

    private ObjectArrayList<Particle>[] bufferLists;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, Textures var2, CallbackInfo ci) {
        this.particles = new List[6];
        this.bufferLists = new ObjectArrayList[particles.length];

        for (int i = 0; i < this.particles.length; ++i) {
            this.particles[i] = new ObjectArrayList<>();
            this.bufferLists[i] = new ObjectArrayList<>();
        }
    }

    @Overwrite
    public void add(Particle particle) {
        int n = particle.getParticleTexture();
        ObjectArrayList<Particle> list = this.bufferLists[n];
        list.add(particle);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void flushParticleBuffers(CallbackInfo ci) {
        this.flushParticleBuffers();
    }

    @Unique
    private void flushParticleBuffers() {
        int particleLimit = ((ExGameOptions) Minecraft.instance.options).getParticleLimit();

        for (int i = 0; i < this.particles.length; ++i) {
            var dst = (ObjectArrayList<Particle>) this.particles[i];
            var src = this.bufferLists[i];

            int toRemove = (dst.size() + src.size()) - particleLimit;
            if (toRemove > 0) {
                int removeFromDst = Math.min(dst.size(), toRemove);
                dst.removeElements(0, removeFromDst);
                toRemove -= removeFromDst;

                if (toRemove > 0) {
                    src.removeElements(0, toRemove);
                }
            }

            dst.addAll(src);
            src.clear();
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void clearParticleBuffers(Level world, CallbackInfo ci) {
        for (ObjectArrayList<Particle> bufferList : this.bufferLists) {
            bufferList.clear();
        }
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 0, ordinal = 1))
    private int bindTerrainTextures(int constant, @Local int i) {
        if (i == 3) {
            return this.textureManager.loadTexture("/terrain2.png");
        }
        if (i == 4) {
            return this.textureManager.loadTexture("/terrain3.png");
        }
        return constant;
    }

    @ModifyConstant(method = {"tick", "setLevel"}, constant = @Constant(intValue = 4))
    private int returnListCount(int constant) {
        return this.particles.length;
    }

    @ModifyConstant(method = "renderLit", constant = @Constant(intValue = 3))
    private int updateLastParticleType(int constant) {
        return 5;
    }

    @Overwrite
    public String countParticles() {
        int particleCount = 0;
        for (List<Particle> particles : this.particles) {
            particleCount += particles.size();
        }
        return String.valueOf(particleCount);
    }

    @Override
    public void getEffectsWithinAABB(AABB aabb, List<Entity> destination) {
        this.flushParticleBuffers();

        for (List<Particle> list : this.particles) {
            for (Particle entity : list) {
                if (aabb.x0 <= entity.x &&
                    aabb.x1 >= entity.x &&
                    aabb.y0 <= entity.y &&
                    aabb.y1 >= entity.y &&
                    aabb.z0 <= entity.z &&
                    aabb.z1 >= entity.z) {
                    destination.add(entity);
                }
            }
        }
    }
}
