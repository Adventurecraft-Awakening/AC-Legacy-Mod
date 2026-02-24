package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.tile.AC_BlockColor;
import dev.adventurecraft.awakening.item.AC_ItemSubtypes;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.world.item.TopSnowTileItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.StoneTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tile.class)
public abstract class MixinBlock implements ExBlock, Cloneable {

    private @Unique int textureNum;

    @Shadow @Final @Mutable public static Tile STONE;
    @Shadow @Final @Mutable public static Tile COBBLESTONE;

    @Shadow public int tex;
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

    @Environment(value=EnvType.CLIENT)
    @Shadow
    public int getRenderShape() {
        throw new AssertionError();
    }

    // TODO: ((1 << face) & mask) instead of double comparisons
    @Shadow
    public abstract boolean shouldRenderFace(LevelSource level, int x, int y, int z, int face);

    @Shadow
    public abstract boolean mayPlace(Level level, int x, int y, int z);

    @Shadow
    public abstract void onPlace(Level level, int x, int y, int z);

    @Shadow
    public abstract AABB getAABB(Level level, int x, int y, int z) ;

    @Shadow
    public abstract void playerDestroy(Level level, Player player, int x, int y, int z, int meta);

    @Shadow
    public abstract void setShape(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

    @Shadow
    public abstract void updateShape(LevelSource source, int x, int y, int z);

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
        Tile.tiles[1] = null;
        Tile.tiles[4] = null;

        STONE = (new StoneTile(1, 215))
            .setDestroyTime(1.5F)
            .setExplodeable(10.0F)
            .setSoundType(Tile.SOUND_STONE)
            .setDescriptionId("stone");

        ((ExBlock) Tile.GRASS).setSubTypes(5);

        COBBLESTONE = (new AC_BlockColor(4, 214, Material.STONE))
            .setDestroyTime(2.0F)
            .setExplodeable(10.0F)
            .setSoundType(Tile.SOUND_STONE)
            .setDescriptionId("stonebrick");

        Tile.FLOWING_WATER.setDestroyTime(0.5F);
        Tile.WATER.setDestroyTime(0.5F);
        Tile.FLOWING_LAVA.setDestroyTime(0.5F);
        Tile.LAVA.setDestroyTime(0.5F);

        ((ExBlock) Tile.SAND).setSubTypes(4);

        Item.items[Tile.GRASS.id] = (new AC_ItemSubtypes(Tile.GRASS.id - 256)).setDescriptionId("grass");
        Item.items[Tile.SAND.id] = (new AC_ItemSubtypes(Tile.SAND.id - 256)).setDescriptionId("sand");
        Item.items[Tile.TALL_GRASS.id] = (new AC_ItemSubtypes(Tile.TALL_GRASS.id - 256)).setDescriptionId("tallgrass");
        Item.items[Tile.SNOW_LAYER.id] = new TopSnowTileItem(Tile.SNOW_LAYER.id - 256);
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public float getBrightness(LevelSource arg, int i, int j, int k) {
        return arg.getBrightness(i, j, k, this.getBlockLightValue(arg, i, j, k));
    }

    @Overwrite
    public void onRemove(Level var1, int x, int y, int z) {
        // TODO: should this really be called for every tile in the game?
        //       that has massive perf implications...
        ((ExWorld) var1).getTriggerManager().removeArea(x, y, z);
    }

    @Override
    public void ac$onRemove(Level level, int x, int y, int z, boolean dropItems) {
        this.onRemove(level, x, y, z);
    }

    @Override
    public Tile ac$clone() {
        try {
            return (Tile) this.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Overwrite
    public void dropResources(Level var1, int var2, int var3, int var4, int var5, float var6) {
    }

    @Overwrite
    public void popResource(Level var1, int var2, int var3, int var4, ItemInstance var5) {
    }

    @Override
    public int getBlockLightValue(LevelSource view, int x, int y, int z) {
        return Tile.lightEmission[this.id];
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
        return AC_TexturedBlock.fromTexture(this.getTexture(view, x, y, z, side));
    }
}
