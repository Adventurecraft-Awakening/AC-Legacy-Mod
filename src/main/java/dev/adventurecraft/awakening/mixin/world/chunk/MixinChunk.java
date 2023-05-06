package dev.adventurecraft.awakening.mixin.world.chunk;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.block.Block;
import net.minecraft.class_257;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
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

@Mixin(Chunk.class)
public abstract class MixinChunk implements ExChunk {

    @Shadow
    public byte[] heightmap;

    @Shadow
    public byte[] blocks;

    @Shadow
    public class_257 field_957;

    @Shadow
    public boolean field_967;

    @Shadow
    public World world;

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    public double[] temperatures;
    public long lastUpdated;

    @Shadow
    protected abstract void method_887(int i, int j);

    @Shadow
    protected abstract void method_889(int i, int j, int k);

    @Shadow
    public abstract int method_875(int i, int j, int k);

    @Shadow
    public Map<BlockPos, BlockEntity> field_964;

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("TAIL"))
    private void initHeightMapAtInit(World var1, int var2, int var3, CallbackInfo ci) {
        this.initHeightMap();
    }

    public void initHeightMap() {
        this.heightmap = new byte[256];
    }

    @Redirect(method = "method_892", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/block/Block;LIGHT_OPACITY:[I",
        args = {"array=get", "fuzz=9"}))
    private int redirect0_translate256(
        int[] array,
        int index,
        @Local(name = "var4") int var4,
        @Local(name = "var5") int var5) {
        return ExChunk.translate256(this.blocks[var5 + var4 - 1]);
    }

    @Redirect(method = "generateHeightmap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/block/Block;LIGHT_OPACITY:[I",
        args = {"array=get", "fuzz=9"},
        ordinal = 0))
    private int redirect1_translate256(
        int[] array,
        int index,
        @Local(name = "var4") int var4,
        @Local(name = "var5") int var5) {
        return ExChunk.translate256(this.blocks[var5 + var4 - 1]);
    }

    @Redirect(method = "generateHeightmap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/block/Block;LIGHT_OPACITY:[I",
        args = {"array=get", "fuzz=9"},
        ordinal = 1))
    private int redirect2_translate256(
        int[] array,
        int index,
        @Local(name = "var5") int var5,
        @Local(name = "var7") int var7) {
        return ExChunk.translate256(this.blocks[var5 + var7]);
    }

    @Redirect(method = "method_889", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/block/Block;LIGHT_OPACITY:[I",
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
    public int getBlockId(int var1, int var2, int var3) {
        return ExChunk.translate256(this.blocks[var1 << 11 | var3 << 7 | var2]);
    }

    @Redirect(method = "generateHeightmap", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/chunk/Chunk;field_967:Z"))
    private void removeWrite0_field967(Chunk instance, boolean value) {
    }

    @Redirect(method = "method_888", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/chunk/Chunk;field_967:Z"))
    private void removeWrite1_field967(Chunk instance, boolean value) {
    }

    @Redirect(method = "method_889", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/chunk/Chunk;field_967:Z"))
    private void removeWrite2_field967(Chunk instance, boolean value) {
    }

    @Redirect(method = "method_865", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/chunk/Chunk;field_967:Z"))
    private void removeWrite3_field967(Chunk instance, boolean value) {
    }

    @Overwrite
    public boolean setBlockWithMetadata(int var1, int var2, int var3, int var4, int var5) {
        int var6;
        int var7;
        if (((ExWorld) this.world).getUndoStack().isRecording()) {
            var6 = this.getBlockId(var1, var2, var3);
            var7 = this.method_875(var1, var2, var3);
            BlockEntity var8 = this.getChunkBlockTileEntityDontCreate(var1, var2, var3);
            CompoundTag var9 = null;
            if (var8 != null) {
                var9 = new CompoundTag();
                var8.writeNBT(var9);
            }

            ((ExWorld) this.world).getUndoStack().recordChange(var1 + (this.x << 4), var2, var3 + (this.z << 4), var6, var7, var9, var4, var5, (CompoundTag) null);
        }

        var6 = ExChunk.translate256(var4);
        var7 = this.heightmap[var3 << 4 | var1] & 255;
        int var11 = ExChunk.translate256(this.blocks[var1 << 11 | var3 << 7 | var2]) & 255;
        if (var11 == var4 && this.field_957.method_1703(var1, var2, var3) == var5) {
            return false;
        } else {
            int var12 = this.x * 16 + var1;
            int var10 = this.z * 16 + var3;
            this.blocks[var1 << 11 | var3 << 7 | var2] = (byte) ExChunk.translate128(var6);
            if (var11 != 0 && !this.world.isClient) {
                Block.BY_ID[var11].onBlockRemoved(this.world, var12, var2, var10);
            }

            this.field_957.method_1704(var1, var2, var3, var5);
            if (!this.world.dimension.halvesMapping) {
                if (Block.LIGHT_OPACITY[var6 & 255] != 0) {
                    if (var2 >= var7) {
                        this.method_889(var1, var2 + 1, var3);
                    }
                } else if (var2 == var7 - 1) {
                    this.method_889(var1, var2, var3);
                }

                this.world.method_166(LightType.field_2757, var12, var2, var10, var12, var2, var10);
            }

            this.world.method_166(LightType.field_2758, var12, var2, var10, var12, var2, var10);
            this.method_887(var1, var3);
            this.field_957.method_1704(var1, var2, var3, var5);
            if (var4 != 0) {
                Block.BY_ID[var4].onBlockPlaced(this.world, var12, var2, var10);
            }

            if (ACMod.chunkIsNotPopulating) {
                this.field_967 = true;
            }

            return true;
        }
    }

    @Overwrite
    public boolean method_860(int var1, int var2, int var3, int var4) {
        int var5;
        int var6;
        if (((ExWorld) this.world).getUndoStack().isRecording()) {
            var5 = this.getBlockId(var1, var2, var3);
            var6 = this.method_875(var1, var2, var3);
            BlockEntity var7 = this.getChunkBlockTileEntityDontCreate(var1, var2, var3);
            CompoundTag var8 = null;
            if (var7 != null) {
                var8 = new CompoundTag();
                var7.writeNBT(var8);
            }

            ((ExWorld) this.world).getUndoStack().recordChange(var1 + (this.x << 4), var2, var3 + (this.z << 4), var5, var6, var8, var4, 0, (CompoundTag) null);
        }

        var5 = ExChunk.translate256(var4);
        var6 = this.heightmap[var3 << 4 | var1] & 255;
        int var10 = ExChunk.translate256(this.blocks[var1 << 11 | var3 << 7 | var2]) & 255;
        if (var10 == var4) {
            return false;
        } else {
            int var11 = this.x * 16 + var1;
            int var9 = this.z * 16 + var3;
            this.blocks[var1 << 11 | var3 << 7 | var2] = (byte) ExChunk.translate128(var5);
            if (var10 != 0) {
                Block.BY_ID[var10].onBlockRemoved(this.world, var11, var2, var9);
            }

            this.field_957.method_1704(var1, var2, var3, 0);
            if (Block.LIGHT_OPACITY[var5 & 255] != 0) {
                if (var2 >= var6) {
                    this.method_889(var1, var2 + 1, var3);
                }
            } else if (var2 == var6 - 1) {
                this.method_889(var1, var2, var3);
            }

            this.world.method_166(LightType.field_2757, var11, var2, var9, var11, var2, var9);
            this.world.method_166(LightType.field_2758, var11, var2, var9, var11, var2, var9);
            this.method_887(var1, var3);
            if (var4 != 0 && !this.world.isClient) {
                Block.BY_ID[var4].onBlockPlaced(this.world, var11, var2, var9);
            }

            if (ACMod.chunkIsNotPopulating) {
                this.field_967 = true;
            }

            return true;
        }
    }

    @Overwrite
    public void method_876(int var1, int var2, int var3, int var4) {
        if (((ExWorld) this.world).getUndoStack().isRecording()) {
            int var5 = this.getBlockId(var1, var2, var3);
            int var6 = this.method_875(var1, var2, var3);
            BlockEntity var7 = this.getChunkBlockTileEntityDontCreate(var1, var2, var3);
            CompoundTag var8 = null;
            if (var7 != null) {
                var8 = new CompoundTag();
                var7.writeNBT(var8);
            }

            ((ExWorld) this.world).getUndoStack().recordChange(var1 + (this.x << 4), var2, var3 + (this.z << 4), var5, var6, var8, var5, var4, null);
        }

        if (ACMod.chunkIsNotPopulating) {
            this.field_967 = true;
        }

        this.field_957.method_1704(var1, var2, var3, var4);
    }

    @WrapWithCondition(method = "addEntity", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/chunk/Chunk;field_969:Z"))
    private boolean guardWrite_field969(Chunk instance, boolean value, @Local Entity var1) {
        return !(var1 instanceof PlayerEntity);
    }

    @Redirect(method = "placeBlockEntity", at = @At(
        value = "INVOKE",
        target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V",
        remap = false))
    private void printBetterBlockEntityError(
        PrintStream instance,
        String s,
        @Local(ordinal = 0, argsOnly = true) int x,
        @Local(ordinal = 1, argsOnly = true) int y,
        @Local(ordinal = 2, argsOnly = true) int z,
        @Local(argsOnly = true) BlockEntity entity) {
        ACMod.LOGGER.error(String.format("No block entity container: BlockID: %d, TileEntity: %s, Coord: %d  %d  %d", this.getBlockId(x, y, z), ((ExBlockEntity) entity).getClassName(), entity.x, entity.y, entity.z));
    }

    @Inject(method = "method_881", at = @At("TAIL"))
    private void initTempMap(CallbackInfo ci) {
        this.initTempMap();
    }

    private void initTempMap() {
        this.temperatures = this.world.method_1781().getTemperatures(this.temperatures, this.x * 16, this.z * 16, 16, 16);
    }

    @Redirect(method = "method_883", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/BlockEntity;invalidate()V"))
    private void killOnSave(BlockEntity instance) {
        ((ExBlockEntity) instance).setKilledFromSaving(true);
        instance.invalidate();
    }

    @Override
    public boolean setBlockIDWithMetadataTemp(int var1, int var2, int var3, int var4, int var5) {
        int var6 = ExChunk.translate256(var4);
        this.blocks[var1 << 11 | var3 << 7 | var2] = (byte) ExChunk.translate128(var6);
        this.field_957.method_1704(var1, var2, var3, var5);
        return true;
    }

    @Override
    public BlockEntity getChunkBlockTileEntityDontCreate(int var1, int var2, int var3) {
        BlockPos var4 = new BlockPos(var1, var2, var3);
        BlockEntity var5 = this.field_964.get(var4);
        return var5;
    }

    @Override
    public double getTemperatureValue(int var1, int var2) {
        if (this.temperatures == null) {
            this.initTempMap();
        }

        return this.temperatures[var2 << 4 | var1];
    }

    @Override
    public void setTemperatureValue(int var1, int var2, double var3) {
        this.temperatures[var2 << 4 | var1] = var3;
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
