package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.tile.AC_BlockColor;
import dev.adventurecraft.awakening.item.AC_ItemSubtypes;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    private @Unique int textureNum;

    @Shadow @Final public static SoundType SOUND_STONE;
    @Shadow @Final public static Tile[] tiles;
    @Shadow @Final public static int[] lightEmission;
    @Shadow @Final @Mutable public static Tile STONE;
    @Shadow @Final public static GrassTile GRASS;
    @Shadow @Final @Mutable public static Tile COBBLESTONE;
    @Shadow @Final public static Tile FLOWING_WATER;
    @Shadow @Final public static Tile WATER;
    @Shadow @Final public static Tile FLOWING_LAVA;
    @Shadow @Final public static Tile LAVA;
    @Shadow @Final public static Tile SAND;
    @Shadow @Final public static TallGrassTile TALL_GRASS;

    @Shadow @Final public int id;
    @Shadow public double xx0;
    @Shadow public double yy0;
    @Shadow public double zz0;
    @Shadow public double xx1;
    @Shadow public double yy1;
    @Shadow public double zz1;

    @Shadow
    public abstract int getTexture(int side, int meta);

    @Shadow
    public abstract int getTexture(LevelSource view, int x, int y, int z, int side);

    @Shadow
    public int getColor(int meta) {
        throw new AssertionError();
    }

    @Shadow
    public int getRenderShape() {
        throw new AssertionError();
    }

    @Inject(
        method = "<clinit>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/item/Item;items:[Lnet/minecraft/world/item/Item;",
            shift = At.Shift.BEFORE,
            ordinal = 0
        )
    )
    private static void changeBlocksAndItems(CallbackInfo ci) {
        tiles[1] = null;
        tiles[4] = null;

        STONE = (new StoneTile(1, 215))
            .setDestroyTime(1.5F)
            .setExplodeable(10.0F)
            .setSoundType(SOUND_STONE)
            .setDescriptionId("stone");

        ((ExBlock) GRASS).setSubTypes(5);

        COBBLESTONE = (new AC_BlockColor(4, 214, Material.STONE))
            .setDestroyTime(2.0F)
            .setExplodeable(10.0F)
            .setSoundType(SOUND_STONE)
            .setDescriptionId("stonebrick");

        FLOWING_WATER.setDestroyTime(0.5F);
        WATER.setDestroyTime(0.5F);
        FLOWING_LAVA.setDestroyTime(0.5F);
        LAVA.setDestroyTime(0.5F);

        ((ExBlock) SAND).setSubTypes(4);

        Item.items[GRASS.id] = (new AC_ItemSubtypes(GRASS.id - 256)).setDescriptionId("grass");
        Item.items[SAND.id] = (new AC_ItemSubtypes(SAND.id - 256)).setDescriptionId("sand");
        Item.items[TALL_GRASS.id] = (new AC_ItemSubtypes(TALL_GRASS.id - 256)).setDescriptionId("tallgrass");
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public float getBrightness(LevelSource arg, int i, int j, int k) {
        return arg.getBrightness(i, j, k, this.getBlockLightValue(arg, i, j, k));
    }

    @Overwrite
    public void onRemove(Level var1, int var2, int var3, int var4) {
        ((ExWorld) var1).getTriggerManager().removeArea(var2, var3, var4);
    }

    @Overwrite
    public void dropResources(Level var1, int var2, int var3, int var4, int var5, float var6) {
    }

    @Overwrite
    public void popResource(Level var1, int var2, int var3, int var4, ItemInstance var5) {
    }

    @Override
    public int getBlockLightValue(LevelSource view, int x, int y, int z) {
        return lightEmission[this.id];
    }

    @Override
    public void setBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.xx0 = minX;
        this.yy0 = minY;
        this.zz0 = minZ;
        this.xx1 = maxX;
        this.yy1 = maxY;
        this.zz1 = maxZ;
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
        return this.getTexture(view, x, y, z, side);
    }
}
