package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.tile.AC_IBlockColor;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.GrassTile;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GrassTile.class)
public abstract class MixinGrassBlock extends MixinBlock implements ExGrassBlock, AC_IBlockColor, AC_TexturedBlock {

    @Override
    public int getTexture(LevelSource view, int x, int y, int z, int side) {
        return (int) getTextureForSideEx(view, x, y, z, side);
    }

    @Override
    public long getTextureForSideEx(LevelSource view, int x, int y, int z, int side) {
        if (side == 1) {
            return getTopTexture(view, x, y, z);
        } else if (side == 0) {
            return 2;
        } else {
            return getSideTexture(view, x, y, z, side);
        }
    }

    @Override
    public int getColor(int meta) {
        return ExGrassColor.getBaseColor(meta);
    }

    private int getTopTexture(LevelSource view, int x, int y, int z) {
        int meta = view.getData(x, y, z);
        return getTexture(0, meta);
    }

    private long getSideTexture(LevelSource view, int x, int y, int z, int side) {
        ConnectedGrassOption option = ((ExGameOptions) Minecraft.instance.options).ofConnectedGrass();

        Material material = view.getMaterial(x, y + 1, z);
        if (material == Material.TOP_SNOW || material == Material.SNOW) {
            if (option == ConnectedGrassOption.OFF) {
                return 68;
            }
            if (option == ConnectedGrassOption.FANCY) {
                int nX = x;
                int nZ = z;
                switch (side) {
                    case 2 -> --nZ;
                    case 3 -> ++nZ;
                    case 4 -> --nX;
                    case 5 -> ++nX;
                }

                int id = view.getTile(nX, y, nZ);
                if (id != Tile.SNOW_LAYER.id && id != Tile.SNOW.id) {
                    return 68;
                }
            }
            return 66;
        }

        if (option == ConnectedGrassOption.OFF) {
            return 3;
        }
        if (option == ConnectedGrassOption.FANCY) {
            int nX = x;
            int nY = y - 1;
            int nZ = z;
            switch (side) {
                case 2 -> --nZ;
                case 3 -> ++nZ;
                case 4 -> --nX;
                case 5 -> ++nX;
            }

            int id = view.getTile(nX, nY, nZ);
            if (id != GRASS.id) {
                return 3;
            }
        }
        return (long) getTopTexture(view, x, y, z) | (1L << 32);
    }

    @Redirect(method = "tick", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;setTile(IIII)Z"))
    private boolean setChunkPopulatingOnSetBlock(Level instance, int j, int k, int l, int i) {
        ACMod.chunkIsNotPopulating = false;
        boolean result = instance.setTile(j, k, l, i);
        ACMod.chunkIsNotPopulating = true;
        return result;
    }

    @Override
    public int getTexture(int var1, int meta) {
        return meta == 0 ? 0 : 232 + meta - 1;
    }

    @Override
    public int getRenderShape() {
        return ((ExGameOptions) Minecraft.instance.options).isGrass3d() ? 30 : super.getRenderShape();
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
