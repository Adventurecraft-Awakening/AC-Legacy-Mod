package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.compat.LegacyParticleBridge;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.particle.ExParticle;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import dev.adventurecraft.awakening.util.MathF;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
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
    @Unique private final QuadBuilder ac$vertices = QuadBuilder.frameOwned(1024);

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
        RenderSystem.phase(RenderPhase.PARTICLES);
        var context = ParticleRenderContext.of(camera, partialTick);
        var textures = (ExTextureManager) this.textureManager;
        try (var pass = NativeRenderer.pass(context.parameters())) {
            for (int layer = 0; layer < this.particles.length - 1; layer++) {
                var particles = this.particles[layer];
                if (particles.isEmpty()) continue;
                String path = switch (layer) {
                    case 0 -> "/particles.png";
                    case 1 -> "/terrain.png";
                    case 2 -> "/gui/items.png";
                    case 3 -> "/terrain2.png";
                    case 4 -> "/terrain3.png";
                    default -> throw new IllegalStateException("Unknown particle material " + layer);
                };
                var texture=textures.texture(path);
                this.ac$vertices.clear();
                for (int i = 0; i < particles.size(); i++) {
                    var particle=particles.get(i);
                    if(LegacyParticleBridge.nativeNormal(particle)) ((ExParticle)particle).ac$render(context,this.ac$vertices);
                    else {
                        pass.drawQuads(this.ac$vertices,texture);this.ac$vertices.clear();
                        LegacyParticleBridge.render(particle,context,texture,false);
                    }
                }
                pass.drawQuads(this.ac$vertices, texture);
            }
        }
    }

    @Overwrite
    public void renderLit(Entity camera, float partialTick) {
        var particles = this.particles[this.particles.length - 1];
        if (particles.isEmpty()) return;
        RenderSystem.phase(RenderPhase.PARTICLES);
        var context = ParticleRenderContext.of(camera, partialTick);
        for(int i=0;i<particles.size();i++) {
            var particle=particles.get(i);
            if(LegacyParticleBridge.nativeLit(particle)) ((ExParticle)particle).ac$renderLit(context);
            else LegacyParticleBridge.render(particle,context,null,true);
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
