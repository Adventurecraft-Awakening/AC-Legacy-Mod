package dev.adventurecraft.awakening.mixin.client.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.particle.ExParticleManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager implements ExParticleManager {

    @Shadow
    private List<Entity>[] field_270;

    @Shadow
    private TextureManager textureManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, TextureManager var2, CallbackInfo ci) {
        this.field_270 = new List[6];
        for (int var3 = 0; var3 < this.field_270.length; ++var3) {
            this.field_270[var3] = new ArrayList<>();
        }
    }

    @ModifyConstant(method = "method_324", constant = @Constant(intValue = 0, ordinal = 1))
    private int bindTerrainTextures(int constant, @Local int var8) {
        if (var8 == 3) {
            return this.textureManager.getTextureId("/terrain2.png");
        }
        if (var8 == 4) {
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

    @Override
    public void getEffectsWithinAABB(AxixAlignedBoundingBox var1, List<Entity> destination) {

        for (List<Entity> list : this.field_270) {
            for (Entity var6 : list) {
                if (var1.minX <= var6.x &&
                    var1.maxX >= var6.x &&
                    var1.minY <= var6.y &&
                    var1.maxY >= var6.y &&
                    var1.minZ <= var6.z &&
                    var1.maxZ >= var6.z) {
                    destination.add(var6);
                }
            }
        }
    }
}
