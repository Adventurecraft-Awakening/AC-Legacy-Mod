package dev.adventurecraft.awakening.mixin.world.chunk;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_BlockEditAction;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.util.BufferUtil;
import dev.adventurecraft.awakening.util.NibbleBuffer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;

import javax.annotation.Nullable;

@Mixin(LevelChunk.class)
public abstract class MixinChunk implements ExChunk {

    @Shadow public boolean loaded;
    @Shadow public byte[] heightMap;
    @Shadow public byte[] blocks;
    @Shadow public DataLayer data;
    @Shadow public DataLayer skyLight;
    @Shadow public DataLayer blockLight;
    @Shadow public Level level;

    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow public Map<Integer, TileEntity> tileEntities = new Int2ObjectOpenHashMap<>();

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
        return this.ac$setTileAndData(x, y, z, id, meta, true);
    }

    @Override
    public boolean ac$setTileAndData(int x, int y, int z, int id, int meta, boolean dropItems) {
        int prevId = this.getTile(x, y, z);
        int prevMeta = this.getData(x, y, z);
        if (prevId == id && prevMeta == meta) {
            // TODO: expose flag that recreates TileEntity here?
            return false;
        }

        AC_UndoStack undoStack = ((ExWorld) this.level).getUndoStack();
        if (undoStack.isRecording()) {
            this.recordChange(x, y, z, prevId, prevMeta, id, meta, undoStack);
        }

        int newId = id & 0xff;
        this.blocks[x << 11 | z << 7 | y] = ExChunk.narrowByte(newId);
        this.data.set(x, y, z, meta);

        int bX = (this.x << 4) + x;
        int bZ = (this.z << 4) + z;
        if (prevId != 0 && !this.level.isClientSide) {
            ((ExBlock) Tile.tiles[prevId]).ac$onRemove(this.level, bX, y, bZ, dropItems);
        }

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
            this.recordChange(x, y, z, id, prevMeta, id, newMeta, undoStack);
        }

        this.data.set(x, y, z, newMeta);
        if (ACMod.chunkIsNotPopulating) {
            this.markUnsaved();
        }
    }

    @Unique
    private void recordChange(
        int x,
        int y,
        int z,
        int prevTile,
        int prevMeta,
        int newTile,
        int newMeta,
        AC_UndoStack stack
    ) {
        TileEntity entity = this.ac$tryGetTileEntity(x, y, z, null);
        CompoundTag prevNbt = null;
        if (entity != null) {
            prevNbt = new CompoundTag();
            entity.save(prevNbt);
        }

        int bX = x + (this.x << 4);
        int bZ = z + (this.z << 4);

        var action = new AC_BlockEditAction(bX, y, bZ, prevTile, prevMeta, prevNbt, newTile, newMeta, null);
        stack.recordAction(action);
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

    @Overwrite
    public void setTileEntity(int x, int y, int z, TileEntity entity) {
        int eX = (this.x << 4) + x;
        int eZ = (this.z << 4) + z;

        entity.level = this.level;
        entity.x = eX;
        entity.y = y;
        entity.z = eZ;

        int id = this.getTile(x, y, z);
        if (!Tile.isEntityTile[id]) {
            logUnexpectedTileEntityError(eX, y, eZ, id, entity);
            return;
        }
        entity.clearRemoved();
        this.ac$tileEntities().put(this.ac$tileEntityKey(eX, y, eZ), entity);
    }

    @Unique
    private static void logUnexpectedTileEntityError(int x, int y, int z, int id, @Nullable TileEntity entity) {
        Tile tile = Tile.tiles[id];
        String tName = tile != null ? tile.getName() : "<null>";
        String eName = entity != null ? ((ExBlockEntity) entity).getClassName() : "<null>";
        ACMod.LOGGER.error("Unexpected {} (#{}) for entity {} at XYZ {} {} {}", tName, id, eName, x, y, z);
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

    @Unique
    public @Override Int2ObjectMap<TileEntity> ac$tileEntities() {
        return (Int2ObjectMap<TileEntity>) this.tileEntities;
    }

    @Unique
    public int ac$tileEntityKey(int x, int y, int z) {
        int bX = (x - (this.x << 4)) & 0xF;
        int bZ = (z - (this.z << 4)) & 0xF;
        int bY = y & 0xFF;
        return (bY << 8) | (bZ << 4) | bX;
    }

    public @Override <E extends TileEntity> E ac$tryGetTileEntity(int x, int y, int z, @Nullable Class<E> type) {
        var entity = this.ac$tileEntities().get(this.ac$tileEntityKey(x, y, z));
        if (type == null) {
            //noinspection unchecked
            return (E) entity;
        }
        return type.cast(entity);
    }

    public @Override <E extends TileEntity> E ac$getTileEntity(int x, int y, int z, @Nullable Class<E> type) {
        int eX = x + (this.x << 4);
        int eZ = z + (this.z << 4);
        int key = this.ac$tileEntityKey(eX, y, eZ);
        Int2ObjectMap<TileEntity> map = this.ac$tileEntities();

        TileEntity entity = map.get(key);
        if (entity == null || (type != null && !type.isInstance(entity))) {
            int id = this.getTile(x, y, z);
            if (!Tile.isEntityTile[id]) {
                logUnexpectedTileEntityError(eX, y, eZ, id, entity);
                return null;
            }

            Tile.tiles[id].onPlace(this.level, eX, y, eZ);
            entity = map.get(key);
            // Skip type check; assume tile always creates correct type.
        }

        if (entity != null && entity.isRemoved()) {
            map.remove(key);
            return null;
        }
        //noinspection unchecked
        return (E) entity;
    }

    @Overwrite
    public TileEntity getTileEntity(int x, int y, int z) {
        return this.ac$getTileEntity(x, y, z, null);
    }

    @Overwrite
    public void removeTileEntity(int x, int y, int z) {
        if (!this.loaded) {
            return;
        }
        TileEntity entity = this.ac$tileEntities().remove(this.ac$tileEntityKey(x, y, z));
        if (entity != null) {
            entity.setRemoved();
        }
    }

    public @Override void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1) {
        // Fill the entire requested range with values; out of bounds is zero.
        if (y0 < 0) {
            BufferUtil.repeatZero(buffer, -y0);
            y0 = 0;
        }
        buffer.put(this.blocks, (x << 11 | z << 7) + y0, Math.min(y1, 128) - y0);
        if (y1 > 128) {
            BufferUtil.repeatZero(buffer, y1 - 128);
        }
    }

    public @Override void getDataColumn(DataType type, NibbleBuffer buffer, int x, int y0, int z, int y1) {
        // Fill the entire requested range with values; out of bounds is zero.
        if (y0 < 0) {
            buffer.repeat(0, -y0);
            y0 = 0;
        }
        DataLayer layer = switch (type) {
            case BLOCK_META -> this.data;
            case BLOCK_LIGHT -> this.blockLight;
            case SKY_LIGHT -> this.skyLight;
        };
        buffer.put(layer.data, (x << 11 | z << 7) + y0, Math.min(y1, 128) - y0);
        if (y1 > 128) {
            buffer.repeat(0, y1 - 128);
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
