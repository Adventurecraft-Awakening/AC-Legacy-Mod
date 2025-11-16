package dev.adventurecraft.awakening.mixin.world.chunk;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.util.BufferUtil;
import net.minecraft.world.level.tile.TileEntityTile;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintStream;
import java.nio.ByteBuffer;
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

    @Shadow public byte[] heightMap;
    @Shadow public byte[] blocks;
    @Shadow public DataLayer data;
    @Shadow public Level level;

    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow public Map<TilePos, TileEntity> tileEntities;

    @Unique public double[] temperatures;
    @Unique public long lastUpdated;
    @Unique private int lightHash;

    @Shadow
    protected abstract void lightGaps(int x, int z);

    @Shadow
    protected abstract void recalcHeight(int x, int y, int z);

    @Shadow
    public abstract int getTile(int x, int y, int z);

    @Shadow
    public abstract int getData(int x, int y, int z);

    @Shadow
    public abstract void markUnsaved();

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/Level;II)V",
        at = @At("TAIL")
    )
    private void doInit(Level level, int x, int y, CallbackInfo ci) {
        this.lightHash = level.random.nextInt();
    }

    @Redirect(
        method = {"recalcHeightmap", "lightGap", "recalcHeight", "setBrightness"},
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;unsaved:Z"
        )
    )
    private void markLightButUnsaved(LevelChunk instance, boolean value) {
        this.updateLightHash();
        // TODO: Maybe mark unsaved, at least in some situations?
        //       Would be needed if we want to avoid light updates on load.
        //       Could be good for [player] saves that are not in Editing mode...
    }

    @Overwrite
    public boolean setTileAndData(int x, int y, int z, int id, int meta) {
        int prevId = this.getTile(x, y, z);
        int prevMeta = this.getData(x, y, z);
        if (prevId == id && prevMeta == meta) {
            // TODO: expose flag that recreates TileEntity here?
            return false;
        }

        // TODO: record block regions
        AC_UndoStack undoStack = ((ExWorld) this.level).getUndoStack();
        if (undoStack.isRecording()) {
            var entity = this.ac$tryGetTileEntity(x, y, z, TileEntity.class);
            CompoundTag prevNbt = null;
            if (entity != null) {
                prevNbt = new CompoundTag();
                entity.save(prevNbt);
            }

            undoStack.recordChange(x, y, z, this.x, this.z, prevId, prevMeta, prevNbt, id, meta, null);
        }

        int newId = id & 0xff;
        int bX = this.x * 16 + x;
        int bZ = this.z * 16 + z;
        this.blocks[x << 11 | z << 7 | y] = ExChunk.narrowByte(newId);
        if (prevId != 0 && !this.level.isClientSide) {
            Tile.tiles[prevId].onRemove(this.level, bX, y, bZ);
        }
        this.data.set(x, y, z, meta);

        int height = this.heightMap[z << 4 | x] & 0xff;
        if (Tile.lightBlock[newId] != 0) {
            if (y >= height) {
                this.recalcHeight(x, y + 1, z);
            }
        }
        else if (y == height - 1) {
            this.recalcHeight(x, y, z);
        }
        this.level.updateLight(LightLayer.SKY, bX, y, bZ, bX, y, bZ);
        this.level.updateLight(LightLayer.BLOCK, bX, y, bZ, bX, y, bZ);
        this.lightGaps(x, z);

        if (id != 0 && !this.level.isClientSide) {
            Tile.tiles[id].onPlace(this.level, bX, y, bZ);
        }

        if (ACMod.chunkIsNotPopulating) {
            this.markUnsaved();
        }
        return true;
    }

    @Overwrite
    public boolean setTile(int x, int y, int z, int id) {
        return this.setTileAndData(x, y, z, id, 0);
    }

    @Overwrite
    public void setData(int x, int y, int z, int newMeta) {
        // TODO: record block regions
        AC_UndoStack undoStack = ((ExWorld) this.level).getUndoStack();
        if (undoStack.isRecording()) {
            int id = this.getTile(x, y, z);
            int prevMeta = this.getData(x, y, z);
            var entity = this.ac$tryGetTileEntity(x, y, z, TileEntity.class);
            CompoundTag prevNbt = null;
            if (entity != null) {
                prevNbt = new CompoundTag();
                entity.save(prevNbt);
            }
            undoStack.recordChange(x, y, z, this.x, this.z, id, prevMeta, prevNbt, id, newMeta, null);
        }

        this.data.set(x, y, z, newMeta);
        if (ACMod.chunkIsNotPopulating) {
            this.markUnsaved();
        }
    }

    @WrapWithCondition(
        method = "addEntity",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;lastSaveHadEntities:Z"
        )
    )
    private boolean guardWrite_field969(LevelChunk instance, boolean value, @Local(argsOnly = true) Entity var1) {
        return !(var1 instanceof Player);
    }

    @Redirect(
        method = "setTileEntity",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V",
            remap = false
        )
    )
    private void printBetterBlockEntityError(
        PrintStream instance, String s, @Local(
            ordinal = 0,
            argsOnly = true
        ) int x, @Local(
            ordinal = 1,
            argsOnly = true
        ) int y, @Local(
            ordinal = 2,
            argsOnly = true
        ) int z, @Local(argsOnly = true) TileEntity entity
    ) {
        ACMod.LOGGER.error(
            "No block entity container: BlockID: {}, TileEntity: {}, Coord: X:{} Y:{} Z:{}",
            this.getTile(x, y, z),
            ((ExBlockEntity) entity).getClassName(),
            entity.x,
            entity.y,
            entity.z
        );
    }

    @Inject(
        method = "load",
        at = @At("TAIL")
    )
    private void initTempMap(CallbackInfo ci) {
        this.initTempMap();
    }

    private void initTempMap() {
        this.temperatures = this.level
            .getBiomeSource()
            .getTemperatureBlock(this.temperatures, this.x * 16, this.z * 16, 16, 16);
    }

    @Redirect(
        method = "unload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/entity/TileEntity;setRemoved()V"
        )
    )
    private void killOnSave(TileEntity instance) {
        ((ExBlockEntity) instance).setKilledFromSaving(true);
        instance.setRemoved();
    }

    @Override
    public boolean setBlockIDWithMetadataTemp(int x, int y, int z, int id, int meta) {
        this.blocks[x << 11 | z << 7 | y] = ExChunk.narrowByte(id);
        this.data.set(x, y, z, meta);
        return true;
    }

    public @Override <E extends TileEntity> E ac$tryGetTileEntity(int x, int y, int z, Class<E> type) {
        var pos = new TilePos(x, y, z);
        return type.cast(this.tileEntities.get(pos));
    }

    public @Override <E extends TileEntity> E ac$getTileEntity(int x, int y, int z, Class<E> type) {
        int n = this.getTile(x, y, z);
        if (!Tile.isEntityTile[n]) {
            return null;
        }

        var pos = new TilePos(x, y, z);
        TileEntity entity = this.tileEntities.get(pos);
        if (!type.isInstance(entity)) {
            var tile = (TileEntityTile) Tile.tiles[n];
            tile.onPlace(this.level, this.x * 16 + x, y, this.z * 16 + z);
            entity = this.tileEntities.get(pos);
            // Skip type check; assume tile always creates correct type.
        }

        if (entity != null && entity.isRemoved()) {
            this.tileEntities.remove(pos);
            return null;
        }
        return (E) entity;
    }

    @Overwrite
    public TileEntity getTileEntity(int x, int y, int z) {
        return this.ac$getTileEntity(x, y, z, TileEntity.class);
    }

    public @Override void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1) {
        // Fill the entire requested range with values; out of bounds is zero.
        if (y0 < 0) {
            BufferUtil.repeat(buffer, (byte) 0, -y0);
            y0 = 0;
        }
        buffer.put(this.blocks, (x << 11 | z << 7) + y0, Math.min(y1, 128) - y0);
        if (y1 > 128) {
            BufferUtil.repeat(buffer, (byte) 0, y1 - 128);
        }
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

    @Override
    public int getLightUpdateHash(int x, int y, int z) {
        int hash = this.lightHash;
        hash = (7302013 * hash) + x;
        hash = (7302013 * hash) + y;
        hash = (7302013 * hash) + z;

        // Not needed (until lightmaps); chunk updates affect this.lightHash
        //hash = (7302013 * hash) + this.level.skyDarken * 1430287;

        return hash;
    }

    @Override
    public void updateLightHash() {
        this.lightHash += 1;
    }
}
