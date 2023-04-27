package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.BetterGrassOption;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.AC_IBlockColor;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GrassBlock.class)
public abstract class MixinGrassBlock extends MixinBlock implements ExGrassBlock, AC_IBlockColor, AC_TexturedBlock {

    @Override
    public int getTextureForSide(BlockView view, int x, int y, int z, int side) {
        return (int)getTextureForSideEx(view, x, y, z, side);
    }

    @Override
    public long getTextureForSideEx(BlockView view, int x, int y, int z, int side) {
        if (side == 1) {
            return getTopTexture(view, x, y, z);
        } else if (side == 0) {
            return 2;
        } else {
            return getSideTexture(view, x, y, z, side);
        }
    }

    private int getTopTexture(BlockView view, int x, int y, int z) {
        int meta = view.getBlockMeta(x, y, z);
        return getTextureForSide(0, meta);
    }

    private long getSideTexture(BlockView view, int x, int y, int z, int side) {
        BetterGrassOption option = Config.getBetterGrassOption();

        Material var6 = view.getMaterial(x, y + 1, z);
        if (var6 == Material.SNOW || var6 == Material.SNOW_BLOCK) {
            if (option == BetterGrassOption.OFF) {
                return 68;
            }
            if (option == BetterGrassOption.FANCY) {
                int nX = x;
                int nZ = z;
                switch (side) {
                    case 2 -> --nZ;
                    case 3 -> ++nZ;
                    case 4 -> --nX;
                    case 5 -> ++nX;
                }

                int id = view.getBlockId(nX, y, nZ);
                if (id != Block.SNOW.id && id != Block.SNOW_BLOCK.id) {
                    return 68;
                }
            }
            return 66;
        }

        if (option == BetterGrassOption.OFF) {
            return 3;
        }
        if (option == BetterGrassOption.FANCY) {
            int nX = x;
            int nY = y - 1;
            int nZ = z;
            switch (side) {
                case 2 -> --nZ;
                case 3 -> ++nZ;
                case 4 -> --nX;
                case 5 -> ++nX;
            }

            int id = view.getBlockId(nX, nY, nZ);
            if (id != GRASS.id) {
                return 3;
            }
        }
        return (long)getTopTexture(view, x, y, z) | (1L << 32);
    }

    @Redirect(method = "onScheduledTick", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/World;setBlock(IIII)Z"))
    private boolean setChunkPopulatingOnSetBlock(World instance, int j, int k, int l, int i) {
        ACMod.chunkIsNotPopulating = false;
        boolean result = instance.setBlock(j, k, l, i);
        ACMod.chunkIsNotPopulating = true;
        return result;
    }

    @Override
    public int getTextureForSide(int var1, int meta) {
        return meta == 0 ? 0 : 232 + meta - 1;
    }

    public int getRenderType() {
        return 30;
        // return ((ExGameOptions)Minecraft.instance.options.grass3d) ? 30 : super.getRenderType(); TODO
    }

    @Override
    public void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockMeta(var2, var3, var4);
        var1.setBlockMeta(var2, var3, var4, (var5 + 1) % ExBlock.subTypes[this.id]);
    }

    @Override
    public float grassMultiplier(int var1) {
        return switch (var1) {
            case 2 -> 0.62F;
            case 3 -> 0.85F;
            case 4 -> -1.0F;
            default -> 1.0F;
        };
    }
}
