package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import dev.adventurecraft.awakening.tile.AC_IBlockColor;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Facing;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.GrassTile;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GrassTile.class)
public abstract class MixinGrassBlock extends MixinBlock implements ExGrassBlock, AC_IBlockColor, AC_TexturedBlock {

    public @Override int getTexture(LevelSource view, int x, int y, int z, int side) {
        return AC_TexturedBlock.toTexture(this.getTextureForSideEx(view, x, y, z, side));
    }

    public @Override long getTextureForSideEx(LevelSource view, int x, int y, int z, int side) {
        return switch (side) {
            case Facing.UP -> this.getTopTexture(view, x, y, z);
            case Facing.DOWN -> AC_TexturedBlock.fromTexture(Tile.DIRT.tex);
            default -> this.getSideTexture(view, x, y, z, side);
        };
    }

    public @Override int getColor(int meta) {
        return ExGrassColor.getBaseColor(meta);
    }

    private @Unique long getTopTexture(LevelSource view, int x, int y, int z) {
        int meta = view.getData(x, y, z);
        int tex = this.getTexture(Facing.UP, meta);
        return AC_TexturedBlock.fromTexture(tex) | AC_TexturedBlock.BIOME_BIT;
    }

    private @Unique long getSideTexture(LevelSource view, int x, int y, int z, int side) {
        ConnectedGrassOption option = ((ExGameOptions) Minecraft.instance.options).ofConnectedGrass();

        Material material = view.getMaterial(x, y + 1, z);
        if (material == Material.TOP_SNOW || material == Material.SNOW) {
            if (option == ConnectedGrassOption.OFF) {
                return 68;
            }
            else if (option == ConnectedGrassOption.FANCY) {
                int id = this.getSideTile(view, x, y, z, side);
                if (id != Tile.SNOW_LAYER.id && id != Tile.SNOW.id) {
                    return 68;
                }
            }
            return 66;
        }

        if (option == ConnectedGrassOption.OFF) {
            return 3;
        }
        else if (option == ConnectedGrassOption.FANCY) {
            int id = this.getSideTile(view, x, y - 1, z, side);
            if (id != GRASS.id) {
                return 3;
            }
        }
        return this.getTopTexture(view, x, y, z);
    }

    private @Unique int getSideTile(LevelSource view, int x, int y, int z, int side) {
        switch (side) {
            case Facing.NORTH -> --z;
            case Facing.SOUTH -> ++z;
            case Facing.WEST -> --x;
            case Facing.EAST -> ++x;
        }
        return view.getTile(x, y, z);
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setTile(IIII)Z"
        )
    )
    private boolean setChunkPopulatingOnSetBlock(Level instance, int j, int k, int l, int i) {
        // TODO: why is this forcing chunkIsNotPopulating?
        ACMod.chunkIsNotPopulating = false;
        boolean result = instance.setTile(j, k, l, i);
        ACMod.chunkIsNotPopulating = true;
        return result;
    }

    /**
     * @reason Store {@link Level#getBiomeSource} in local.
     */
    @Environment(EnvType.CLIENT)
    @Overwrite
    public int getFoliageColor(LevelSource level, int x, int y, int z) {
        var source = level.getBiomeSource();
        source.getBiomeBlock(x, z, 1, 1);
        double temp = source.temperatures[0];
        double downfall = source.downfalls[0];
        return GrassColor.get(temp, downfall);
    }

    public @Override int getTexture(int side, int meta) {
        return (meta == 0) ? 0 : ((232 + meta) - 1);
    }

    @Override
    public int getRenderShape() {
        return ((ExGameOptions) Minecraft.instance.options).isGrass3d()
            ? AC_BlockShapes.GRASS_3D
            : super.getRenderShape();
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }

    @Override
    public float getGrassMultiplier(int meta) {
        return switch (meta) {
            case 2 -> 0.62F;
            case 3 -> 0.85F;
            case 4 -> -1.0F;
            default -> 1.0F;
        };
    }
}
