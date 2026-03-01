package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TerrainParticle.class)
public abstract class MixinDiggingParticleEntity extends Particle {

    @Shadow private Tile tile;
    @Shadow private int face;

    public MixinDiggingParticleEntity(Level world, double x, double y, double z, double vX, double vY, double vZ) {
        super(world, x, y, z, vX, vY, vZ);
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/Tile;getTexture(II)I"
        )
    )
    private int useBlockFace(Tile tile, int side, int meta) {
        return 0;
    }

    @Overwrite
    public TerrainParticle init(int x, int y, int z) {
        long textureKey = ((ExBlock) this.tile).getTextureForSideEx(this.level, x, y, z, this.face);
        this.texture = AC_TexturedBlock.toTexture(textureKey);

        // TODO: make less magical in future
        if (this.tile.id != Tile.GRASS.id || AC_TexturedBlock.hasBiomeBit(textureKey)) {
            int n = this.tile.getFoliageColor(this.level, x, y, z);
            this.rCol *= (float) ((n >> 16) & 0xFF) / 255.0f;
            this.gCol *= (float) ((n >> 8) & 0xFF) / 255.0f;
            this.bCol *= (float) (n & 0xFF) / 255.0f;
        }
        return (TerrainParticle) (Object) this;
    }

    @Overwrite
    public int getParticleTexture() {
        return switch (((ExBlock) this.tile).getTextureNum()) {
            case 2 -> 3;
            case 3 -> 4;
            default -> 1;
        };
    }
}
