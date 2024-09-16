package dev.adventurecraft.awakening.mixin.client.entity.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TerrainParticle.class)
public abstract class MixinDiggingParticleEntity extends Particle {

    @Shadow
    private Tile block;

    public MixinDiggingParticleEntity(Level world, double x, double y, double z, double vX, double vY, double vZ) {
        super(world, x, y, z, vX, vY, vZ);
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 0))
    private int useBlockMeta(int constant, @Local(ordinal = 0, argsOnly = true) int meta) {
        return meta;
    }

    @Overwrite
    public TerrainParticle multiplyColor(int x, int y, int z) {
        int n = this.block.getFoliageColor(this.level, x, y, z);
        this.rCol *= (float) (n >> 16 & 0xFF) / 255.0f;
        this.gCol *= (float) (n >> 8 & 0xFF) / 255.0f;
        this.bCol *= (float) (n & 0xFF) / 255.0f;
        return (TerrainParticle) (Object) this;
    }

    @Overwrite
    public int getParticleTexture() {
        int texture = ((ExBlock) this.block).getTextureNum();
        return texture == 2 ? 3 : (texture == 3 ? 4 : 1);
    }
}
