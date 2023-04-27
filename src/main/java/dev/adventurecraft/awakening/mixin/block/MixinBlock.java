package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_BlockColor;
import dev.adventurecraft.awakening.common.AC_ItemSubtypes;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class MixinBlock implements ExBlock, AC_TexturedBlock {

    private int textureNum;

    @Shadow
    @Final
    public static BlockSounds PISTON_SOUNDS;

    @Shadow
    @Final
    public static Block[] BY_ID;

    @Shadow
    @Final
    public static int[] EMITTANCE;

    @Shadow
    @Final
    @Mutable
    public static Block STONE;

    @Shadow
    @Final
    public static GrassBlock GRASS;

    @Shadow
    @Final
    @Mutable
    public static Block COBBLESTONE;

    @Shadow
    @Final
    public static Block FLOWING_WATER;

    @Shadow
    @Final
    public static Block STILL_WATER;

    @Shadow
    @Final
    public static Block FLOWING_LAVA;

    @Shadow
    @Final
    public static Block STILL_LAVA;

    @Shadow
    @Final
    public static Block SAND;

    @Shadow
    @Final
    public static TallGrassBlock TALLGRASS;

    @Shadow
    @Final
    public int id;

    @Shadow
    public abstract int getTextureForSide(BlockView arg, int i, int j, int k, int l);

    @Inject(method = "<clinit>", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/item/Item;byId:[Lnet/minecraft/item/Item;",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private static void changeBlocksAndItems(CallbackInfo ci) {
        BY_ID[1] = null;
        BY_ID[4] = null;

        STONE = (new StoneBlock(1, 215)).setHardness(1.5F).setBlastResistance(10.0F).setSounds(PISTON_SOUNDS).setTranslationKey("stone");
        ((ExBlock) GRASS).setSubTypes(5);
        COBBLESTONE = (new AC_BlockColor(4, 214, Material.STONE)).setHardness(2.0F).setBlastResistance(10.0F).setSounds(PISTON_SOUNDS).setTranslationKey("stonebrick");
        FLOWING_WATER.setHardness(0.5F);
        STILL_WATER.setHardness(0.5F);
        FLOWING_LAVA.setHardness(0.5F);
        STILL_LAVA.setHardness(0.5F);
        ((ExBlock) SAND).setSubTypes(4);

        Item.byId[GRASS.id] = (new AC_ItemSubtypes(GRASS.id - 256)).setTranslationKey("grass");
        Item.byId[SAND.id] = (new AC_ItemSubtypes(SAND.id - 256)).setTranslationKey("sand");
        Item.byId[TALLGRASS.id] = (new AC_ItemSubtypes(TALLGRASS.id - 256)).setTranslationKey("tallgrass");
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "getBrightness", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/block/Block;EMITTANCE:[I",
            opcode = Opcodes.GETSTATIC,
            args = "array=get"))
    private int getBlockLightForBrightness(int[] emittance, int index, BlockView var1, int var2, int var3, int var4) {
        return this.getBlockLightValue(var1, var2, var3, var4);
    }

    @Overwrite
    public void onBlockRemoved(World var1, int var2, int var3, int var4) {
        ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
    }

    @Overwrite
    public void beforeDestroyedByExplosion(World var1, int var2, int var3, int var4, int var5, float var6) {
    }

    @Overwrite
    public void drop(World var1, int var2, int var3, int var4, ItemStack var5) {
    }

    @Override
    public int getBlockLightValue(BlockView var1, int var2, int var3, int var4) {
        return EMITTANCE[this.id];
    }

    @Override
    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return true;
    }

    @Override
    public boolean canBeTriggered() {
        return false;
    }

    @Override
    public void addTriggerActivation(World var1, int var2, int var3, int var4) {
        if (this.canBeTriggered()) {
            int var5 = Math.min(var1.getBlockMeta(var2, var3, var4) + 1, 15);
            var1.method_223(var2, var3, var4, var5);
            if (var5 == 1) {
                this.onTriggerActivated(var1, var2, var3, var4);
            }
        }
    }

    @Override
    public void removeTriggerActivation(World var1, int var2, int var3, int var4) {
        if (this.canBeTriggered()) {
            int var5 = var1.getBlockMeta(var2, var3, var4) - 1;
            var1.method_223(var2, var3, var4, Math.max(var5, 0));
            if (var5 == 0) {
                this.onTriggerDeactivated(var1, var2, var3, var4);
            }
        }
    }

    @Override
    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
    }

    @Override
    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
    }

    @Override
    public void reset(World var1, int var2, int var3, int var4, boolean var5) {
    }

    @Override
    public int alwaysUseClick(World var1, int var2, int var3, int var4) {
        return -1;
    }

    @Override
    public int getTextureNum() {
        return this.textureNum;
    }

    @Override
    public Block setTextureNum(int var1) {
        this.textureNum = var1;
        return (Block) (Object) this;
    }

    @Override
    public Block setSubTypes(int var1) {
        subTypes[this.id] = var1;
        return (Block) (Object) this;
    }

    @Override
    public long getTextureForSideEx(BlockView view, int x, int y, int z, int side) {
        return this.getTextureForSide(view, x, y, z, side);
    }
}
