package dev.adventurecraft.awakening.mixin.client.entity.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.client.entity.particle.DiggingParticleEntity;
import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DiggingParticleEntity.class)
public abstract class MixinDiggingParticleEntity extends ParticleEntity {

    @Shadow
    private Block block;

    public MixinDiggingParticleEntity(World world, double x, double y, double z, double vX, double vY, double vZ) {
        super(world, x, y, z, vX, vY, vZ);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 0))
    private int useBlockMeta(int constant, @Local(ordinal = 0, argsOnly = true) int meta) {
        return meta;
    }

    @Overwrite
    public DiggingParticleEntity multiplyColor(int x, int y, int z) {
        int n = this.block.getColorMultiplier(this.world, x, y, z);
        this.red *= (float) (n >> 16 & 0xFF) / 255.0f;
        this.green *= (float) (n >> 8 & 0xFF) / 255.0f;
        this.blue *= (float) (n & 0xFF) / 255.0f;
        return (DiggingParticleEntity) (Object) this;
    }

    @Overwrite
    public int method_2003() {
        int texture = ((ExBlock) this.block).getTextureNum();
        return texture == 2 ? 3 : (texture == 3 ? 4 : 1);
    }
}
