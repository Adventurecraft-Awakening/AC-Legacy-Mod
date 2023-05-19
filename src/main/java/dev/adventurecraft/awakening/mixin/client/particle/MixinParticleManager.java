package dev.adventurecraft.awakening.mixin.client.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager implements ExParticleManager {

    @Shadow
    private List<ParticleEntity>[] field_270;

    @Shadow
    private TextureManager textureManager;

    private ObjectArrayList<ParticleEntity>[] bufferLists;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, TextureManager var2, CallbackInfo ci) {
        this.field_270 = new List[6];
        this.bufferLists = new ObjectArrayList[field_270.length];

        for (int i = 0; i < this.field_270.length; ++i) {
            this.field_270[i] = new ObjectArrayList<>();
            this.bufferLists[i] = new ObjectArrayList<>();
        }
    }

    @Overwrite
    public void addParticle(ParticleEntity particle) {
        int n = particle.method_2003();
        ObjectArrayList<ParticleEntity> list = this.bufferLists[n];
        list.add(particle);
    }

    @Inject(method = "method_320", at = @At("HEAD"))
    private void flushParticleBuffers(CallbackInfo ci) {
        this.flushParticleBuffers();
    }

    private void flushParticleBuffers() {
        for (int i = 0; i < this.field_270.length; ++i) {
            var dst = (ObjectArrayList<ParticleEntity>) this.field_270[i];
            var src = this.bufferLists[i];

            int toRemove = (dst.size() + src.size()) - 4000;
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

    @Inject(method = "method_323", at = @At("HEAD"))
    private void clearParticleBuffers(World world, CallbackInfo ci) {
        for (ObjectArrayList<ParticleEntity> bufferList : this.bufferLists) {
            bufferList.clear();
        }
    }

    @ModifyConstant(method = "method_324", constant = @Constant(intValue = 0, ordinal = 1))
    private int bindTerrainTextures(int constant, @Local int i) {
        if (i == 3) {
            return this.textureManager.getTextureId("/terrain2.png");
        }
        if (i == 4) {
            return this.textureManager.getTextureId("/terrain3.png");
        }
        return constant;
    }

    @ModifyConstant(method = {"method_320", "method_323"}, constant = @Constant(intValue = 4))
    private int returnListCount(int constant) {
        return this.field_270.length;
    }

    @ModifyConstant(method = "method_327", constant = @Constant(intValue = 3))
    private int updateLastParticleType(int constant) {
        return 5;
    }

    @Overwrite
    public String method_326() {
        int particleCount = 0;
        for (List<ParticleEntity> particles : this.field_270) {
            particleCount += particles.size();
        }
        return String.valueOf(particleCount);
    }

    @Override
    public void getEffectsWithinAABB(AxixAlignedBoundingBox aabb, List<Entity> destination) {
        this.flushParticleBuffers();

        for (List<ParticleEntity> list : this.field_270) {
            for (ParticleEntity entity : list) {
                if (aabb.minX <= entity.x &&
                    aabb.maxX >= entity.x &&
                    aabb.minY <= entity.y &&
                    aabb.maxY >= entity.y &&
                    aabb.minZ <= entity.z &&
                    aabb.maxZ >= entity.z) {
                    destination.add(entity);
                }
            }
        }
    }
}
