package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_BlockColor;
import dev.adventurecraft.awakening.common.AC_ItemSubtypes;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.GrassTile;
import net.minecraft.world.level.tile.SoundType;
import net.minecraft.world.level.tile.StoneTile;
import net.minecraft.world.level.tile.TallGrassTile;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tile.class)
public abstract class MixinBlock implements ExBlock {

    private int textureNum;

    @Shadow
    @Final
    public static SoundType PISTON_SOUNDS;

    @Shadow
    @Final
    public static Tile[] BY_ID;

    @Shadow
    @Final
    public static int[] EMITTANCE;

    @Shadow
    @Final
    @Mutable
    public static Tile STONE;

    @Shadow
    @Final
    public static GrassTile GRASS;

    @Shadow
    @Final
    @Mutable
    public static Tile COBBLESTONE;

    @Shadow
    @Final
    public static Tile FLOWING_WATER;

    @Shadow
    @Final
    public static Tile STILL_WATER;

    @Shadow
    @Final
    public static Tile FLOWING_LAVA;

    @Shadow
    @Final
    public static Tile STILL_LAVA;

    @Shadow
    @Final
    public static Tile SAND;

    @Shadow
    @Final
    public static TallGrassTile TALLGRASS;

    @Shadow
    @Final
    public int id;

    @Shadow
    public double minX;

    @Shadow
    public double minY;

    @Shadow
    public double minZ;

    @Shadow
    public double maxX;

    @Shadow
    public double maxY;

    @Shadow
    public double maxZ;

    @Shadow
    public abstract int getTextureForSide(int j, int l);

    @Shadow
    public abstract int getTextureForSide(LevelSource arg, int i, int j, int k, int l);
    
    @Shadow
    public int getBaseColor(int meta) {
        throw new AssertionError();
    }

    @Shadow
    public int getRenderType() {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/item/Item;byId:[Lnet/minecraft/item/Item;",
        shift = At.Shift.BEFORE,
        ordinal = 0))
    private static void changeBlocksAndItems(CallbackInfo ci) {
        BY_ID[1] = null;
        BY_ID[4] = null;

        STONE = (new StoneTile(1, 215)).setDestroyTime(1.5F).setExplodeable(10.0F).setSoundType(PISTON_SOUNDS).setDescriptionId("stone");
        ((ExBlock) GRASS).setSubTypes(5);
        COBBLESTONE = (new AC_BlockColor(4, 214, Material.STONE)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(PISTON_SOUNDS).setDescriptionId("stonebrick");
        FLOWING_WATER.setDestroyTime(0.5F);
        STILL_WATER.setDestroyTime(0.5F);
        FLOWING_LAVA.setDestroyTime(0.5F);
        STILL_LAVA.setDestroyTime(0.5F);
        ((ExBlock) SAND).setSubTypes(4);

        Item.items[GRASS.id] = (new AC_ItemSubtypes(GRASS.id - 256)).setDescriptionId("grass");
        Item.items[SAND.id] = (new AC_ItemSubtypes(SAND.id - 256)).setDescriptionId("sand");
        Item.items[TALLGRASS.id] = (new AC_ItemSubtypes(TALLGRASS.id - 256)).setDescriptionId("tallgrass");
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public float getBrightness(LevelSource arg, int i, int j, int k) {
        return arg.getBrightness(i, j, k, this.getBlockLightValue(arg, i, j, k));
    }

    @Overwrite
    public void onBlockRemoved(Level var1, int var2, int var3, int var4) {
        ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
    }

    @Overwrite
    public void beforeDestroyedByExplosion(Level var1, int var2, int var3, int var4, int var5, float var6) {
    }

    @Overwrite
    public void drop(Level var1, int var2, int var3, int var4, ItemInstance var5) {
    }

    @Override
    public int getBlockLightValue(LevelSource view, int x, int y, int z) {
        return EMITTANCE[this.id];
    }

    @Override
    public void setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    @Override
    public int getTextureNum() {
        return this.textureNum;
    }

    @Override
    public Tile setTextureNum(int var1) {
        this.textureNum = var1;
        return (Tile) (Object) this;
    }

    @Override
    public Tile setSubTypes(int var1) {
        subTypes[this.id] = var1;
        return (Tile) (Object) this;
    }

    @Override
    public long getTextureForSideEx(LevelSource view, int x, int y, int z, int side) {
        return this.getTextureForSide(view, x, y, z, side);
    }
}
