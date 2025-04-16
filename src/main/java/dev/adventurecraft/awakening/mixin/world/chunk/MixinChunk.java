package dev.adventurecraft.awakening.mixin.world.chunk;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintStream;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TilePos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

@Mixin(LevelChunk.class)
public abstract class MixinChunk implements ExChunk {

    @Shadow
    public byte[] heightMap;

    @Shadow
    public byte[] blocks;

    @Shadow
    public DataLayer data;

    @Shadow
    public boolean unsaved;

    @Shadow
    public Level level;

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    public double[] temperatures;
    public long lastUpdated;

    @Shadow
    protected abstract void lightGaps(int i, int j);

    @Shadow
    protected abstract void recalcHeight(int i, int j, int k);

    @Shadow
    public abstract int getData(int i, int j, int k);

    @Shadow
    public Map<TilePos, TileEntity> tileEntities;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;II)V", at = @At("TAIL"))
    private void initHeightMapAtInit(Level var1, int var2, int var3, CallbackInfo ci) {
        this.initHeightMap();
    }

    public void initHeightMap() {
        this.heightMap = new byte[256];
    }

    @Redirect(method = "recalcHeightmapOnly", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/tile/Tile;lightBlock:[I",
        args = {"array=get", "fuzz=9"}))
    private int redirect0_translate256(
        int[] array,
        int index,
        @Local(name = "var4") int var4,
        @Local(name = "var5") int var5) {
        return ExChunk.translate256(this.blocks[var5 + var4 - 1]);
    }

    @Redirect(method = "recalcHeightmap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/tile/Tile;lightBlock:[I",
        args = {"array=get", "fuzz=9"},
        ordinal = 0))
    private int redirect1_translate256(
        int[] array,
        int index,
        @Local(name = "var4") int var4,
        @Local(name = "var5") int var5) {
        return ExChunk.translate256(this.blocks[var5 + var4 - 1]);
    }

    @Redirect(method = "recalcHeightmap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/tile/Tile;lightBlock:[I",
        args = {"array=get", "fuzz=9"},
        ordinal = 1))
    private int redirect2_translate256(
        int[] array,
        int index,
        @Local(name = "var5") int var5,
        @Local(name = "var7") int var7) {
        return ExChunk.translate256(this.blocks[var5 + var7]);
    }

    @Redirect(method = "recalcHeight", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/tile/Tile;lightBlock:[I",
        args = {"array=get", "fuzz=9"},
        ordinal = 0))
    private int redirect3_translate256(
        int[] array,
        int index,
        @Local(name = "var5") int var5,
        @Local(name = "var6") int var6) {
        return ExChunk.translate256(this.blocks[var6 + var5 - 1]);
    }

    @Overwrite
    public int getTile(int var1, int var2, int var3) {
        return ExChunk.translate256(this.blocks[var1 << 11 | var3 << 7 | var2]);
    }

    @Redirect(method = "recalcHeightmap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;unsaved:Z"))
    private void removeWrite0_field967(LevelChunk instance, boolean value) {
    }

    @Redirect(method = "lightGap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;unsaved:Z"))
    private void removeWrite1_field967(LevelChunk instance, boolean value) {
    }

    @Redirect(method = "recalcHeight", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;unsaved:Z"))
    private void removeWrite2_field967(LevelChunk instance, boolean value) {
    }

    @Redirect(method = "setBrightness", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/chunk/LevelChunk;unsaved:Z"))
    private void removeWrite3_field967(LevelChunk instance, boolean value) {
    }

    @Overwrite
    public boolean setTileAndData(int x, int y, int z, int id, int meta) {
        AC_UndoStack undoStack = ((ExWorld) this.level).getUndoStack();
        if (undoStack.isRecording()) {
            int prevId = this.getTile(x, y, z);
            int prevMeta = this.getData(x, y, z);
            TileEntity entity = this.getChunkBlockTileEntityDontCreate(x, y, z);
            CompoundTag prevNbt = null;
            if (entity != null) {
                prevNbt = new CompoundTag();
                entity.save(prevNbt);
            }

            undoStack.recordChange(x, y, z, this.x, this.z, prevId, prevMeta, prevNbt, id, meta, null);
        }

        int id256 = ExChunk.translate256(id);
        int height = this.heightMap[z << 4 | x] & 255;
        int bId256 = ExChunk.translate256(this.blocks[x << 11 | z << 7 | y]) & 255;
        if (bId256 == id && this.data.get(x, y, z) == meta) {
            return false;
        }

        int bX = this.x * 16 + x;
        int bZ = this.z * 16 + z;
        this.blocks[x << 11 | z << 7 | y] = (byte) ExChunk.translate128(id256);
        if (bId256 != 0 && !this.level.isClientSide) {
            Tile.tiles[bId256].onRemove(this.level, bX, y, bZ);
        }

        this.data.set(x, y, z, meta);
        if (!this.level.dimension.hasCeiling) {
            if (Tile.lightBlock[id256 & 255] != 0) {
                if (y >= height) {
                    this.recalcHeight(x, y + 1, z);
                }
            } else if (y == height - 1) {
                this.recalcHeight(x, y, z);
            }

            this.level.updateLight(LightLayer.SKY, bX, y, bZ, bX, y, bZ);
        }

        this.level.updateLight(LightLayer.BLOCK, bX, y, bZ, bX, y, bZ);
        this.lightGaps(x, z);
        this.data.set(x, y, z, meta);
        if (id != 0) {
            Tile.tiles[id].onPlace(this.level, bX, y, bZ);
        }

        if (ACMod.chunkIsNotPopulating) {
            this.unsaved = true;
        }

        return true;
    }

    @Overwrite
    public boolean setTile(int x, int y, int z, int id) {
        AC_UndoStack undoStack = ((ExWorld) this.level).getUndoStack();
        if (undoStack.isRecording()) {
            int prevId = this.getTile(x, y, z);
            int prevMeta = this.getData(x, y, z);
            TileEntity entity = this.getChunkBlockTileEntityDontCreate(x, y, z);
            CompoundTag prevNbt = null;
            if (entity != null) {
                prevNbt = new CompoundTag();
                entity.save(prevNbt);
            }

            undoStack.recordChange(x, y, z, this.x, this.z, prevId, prevMeta, prevNbt, id, 0, null);
        }

        int id256 = ExChunk.translate256(id);
        int height = this.heightMap[z << 4 | x] & 255;
        int bId256 = ExChunk.translate256(this.blocks[x << 11 | z << 7 | y]) & 255;
        if (bId256 == id) {
            return false;
        }

        int bX = this.x * 16 + x;
        int bZ = this.z * 16 + z;
        this.blocks[x << 11 | z << 7 | y] = (byte) ExChunk.translate128(id256);
        if (bId256 != 0) {
            Tile.tiles[bId256].onRemove(this.level, bX, y, bZ);
        }

        this.data.set(x, y, z, 0);
        if (Tile.lightBlock[id256 & 255] != 0) {
            if (y >= height) {
                this.recalcHeight(x, y + 1, z);
            }
        } else if (y == height - 1) {
            this.recalcHeight(x, y, z);
        }

        this.level.updateLight(LightLayer.SKY, bX, y, bZ, bX, y, bZ);
        this.level.updateLight(LightLayer.BLOCK, bX, y, bZ, bX, y, bZ);
        this.lightGaps(x, z);
        if (id != 0 && !this.level.isClientSide) {
            Tile.tiles[id].onPlace(this.level, bX, y, bZ);
        }

        if (ACMod.chunkIsNotPopulating) {
            this.unsaved = true;
        }

        return true;
    }

    @Overwrite
    public void setData(int x, int y, int z, int newMeta) {
        AC_UndoStack undoStack = ((ExWorld) this.level).getUndoStack();
        if (undoStack.isRecording()) {
            int id = this.getTile(x, y, z);
            int prevMeta = this.getData(x, y, z);
            TileEntity entity = this.getChunkBlockTileEntityDontCreate(x, y, z);
            CompoundTag prevNbt = null;
            if (entity != null) {
                prevNbt = new CompoundTag();
                entity.save(prevNbt);
            }
            undoStack.recordChange(x, y, z, this.x, this.z, id, prevMeta, prevNbt, id, newMeta, null);
        }

        if (ACMod.chunkIsNotPopulating) {
            this.unsaved = true;
        }

        this.data.set(x, y, z, newMeta);
    }

    @WrapWithCondition(
        method = "addEntity",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;lastSaveHadEntities:Z"))
    private boolean guardWrite_field969(LevelChunk instance, boolean value, @Local(argsOnly = true) Entity var1) {
        return !(var1 instanceof Player);
    }

    @Redirect(
        method = "setTileEntity",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V",
            remap = false))
    private void printBetterBlockEntityError(
        PrintStream instance,
        String s,
        @Local(ordinal = 0, argsOnly = true) int x,
        @Local(ordinal = 1, argsOnly = true) int y,
        @Local(ordinal = 2, argsOnly = true) int z,
        @Local(argsOnly = true) TileEntity entity) {
        ACMod.LOGGER.error("No block entity container: BlockID: {}, TileEntity: {}, Coord: X:{} Y:{} Z:{}", this.getTile(x, y, z), ((ExBlockEntity) entity).getClassName(), entity.x, entity.y, entity.z);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void initTempMap(CallbackInfo ci) {
        this.initTempMap();
    }

    private void initTempMap() {
        this.temperatures = this.level.getBiomeSource().getTemperatureBlock(this.temperatures, this.x * 16, this.z * 16, 16, 16);
    }

    @Redirect(method = "unload", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/tile/entity/TileEntity;setRemoved()V"))
    private void killOnSave(TileEntity instance) {
        ((ExBlockEntity) instance).setKilledFromSaving(true);
        instance.setRemoved();
    }

    @Override
    public boolean setBlockIDWithMetadataTemp(int x, int y, int z, int id, int meta) {
        int var6 = ExChunk.translate256(id);
        this.blocks[x << 11 | z << 7 | y] = (byte) ExChunk.translate128(var6);
        this.data.set(x, y, z, meta);
        return true;
    }

    @Override
    public TileEntity getChunkBlockTileEntityDontCreate(int x, int y, int z) {
        TilePos var4 = new TilePos(x, y, z);
        TileEntity var5 = this.tileEntities.get(var4);
        return var5;
    }

    @Override
    public double getTemperatureValue(int x, int z) {
        if (this.temperatures == null) {
            this.initTempMap();
        }

        return this.temperatures[z << 4 | x];
    }

    @Override
    public void setTemperatureValue(int x, int z, double value) {
        this.temperatures[z << 4 | x] = value;
    }

    @Override
    public long getLastUpdated() {
        return this.lastUpdated;
    }

    @Override
    public void setLastUpdated(long value) {
        this.lastUpdated = value;
    }
}
