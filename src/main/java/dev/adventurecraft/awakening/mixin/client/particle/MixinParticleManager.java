package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.MathF;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
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

    @Shadow protected Level level;
    @Shadow private List<Particle>[] particles;
    @Shadow private Textures textureManager;

    @Unique private ObjectArrayList<Particle>[] bufferLists;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
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

    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
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

    @Inject(
        method = "setLevel",
        at = @At("HEAD")
    )
    private void clearParticleBuffers(Level world, CallbackInfo ci) {
        for (ObjectArrayList<Particle> bufferList : this.bufferLists) {
            bufferList.clear();
        }
    }

    @Overwrite
    public void render(Entity camera, float partialTick) {
        Tesselator tesselator = Tesselator.instance;
        var et = (ExTesselator) tesselator;

        float xRad = MathF.toRadians(camera.xRot);
        float yRad = MathF.toRadians(camera.yRot);
        float xSin = Mth.sin(xRad);
        float x0 = Mth.cos(yRad);
        float y0 = Mth.cos(xRad);
        float z0 = Mth.sin(yRad);
        float x1 = -z0 * xSin;
        float z1 = x0 * xSin;

        Particle.xOff = (camera.xOld - et.getX()) + (camera.x - camera.xOld) * (double) partialTick;
        Particle.yOff = (camera.yOld - et.getY()) + (camera.y - camera.yOld) * (double) partialTick;
        Particle.zOff = (camera.zOld - et.getZ()) + (camera.z - camera.zOld) * (double) partialTick;

        for (int tex = 0; tex < this.particles.length; ++tex) {
            var list = this.particles[tex];
            if (list.isEmpty()) {
                continue;
            }

            int texName = switch (tex) {
                case 0 -> this.textureManager.loadTexture("/particles.png");
                case 1 -> this.textureManager.loadTexture("/terrain.png");
                case 2 -> this.textureManager.loadTexture("/gui/items.png");
                case 3 -> this.textureManager.loadTexture("/terrain2.png");
                case 4 -> this.textureManager.loadTexture("/terrain3.png");
                default -> 0;
            };
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texName);

            tesselator.begin();
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < list.size(); ++j) {
                Particle particle = list.get(j);
                particle.render(tesselator, partialTick, x0, y0, z0, x1, z1);
            }
            tesselator.end();
        }
    }

    @Overwrite
    public void renderLit(Entity player, float partialTick) {
        int tex = 5; // TODO: nothing seems to be using particle texture 5...
        var list = this.particles[tex];
        if (list.isEmpty()) {
            return;
        }

        Tesselator tesselator = Tesselator.instance;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < list.size(); ++i) {
            Particle particle = list.get(i);
            // TODO: provide non-zero coords?
            particle.render(tesselator, partialTick, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    @ModifyConstant(
        method = {"tick", "setLevel"},
        constant = @Constant(intValue = 4)
    )
    private int returnListCount(int constant) {
        return this.particles.length;
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
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < list.size(); i++) {
                Particle entity = list.get(i);
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
