package dev.adventurecraft.awakening.mixin.world.level.chunk.storage;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile {

    private static final int ID_LZ4 = 4;
    private static final int ID_CUSTOM = 127;

    private static final String NAME_ZSTD = ACMod.NAMESPACE + ":zstd";

    @Unique
    private static boolean writeLz4 = true;

    @Shadow
    private RandomAccessFile file;

    @Shadow
    protected abstract void debugln(String type, int x, int z, String reason);

    @Inject(
        method = "readChunk",
        cancellable = true,
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Ljava/io/RandomAccessFile;readByte()B"))
    private void createDecompressor(
        int x, int z,
        CallbackInfoReturnable<DataInputStream> cir,
        @Local(ordinal = 5) int length,
        @Local(ordinal = 6) int id)
        throws IOException {

        switch (id) {
            // Check for LZ4 introduced in 24w04a.
            case ID_LZ4 -> {
                var byStream = readBytesIntoArray(length);
                cir.setReturnValue(new DataInputStream(new LZ4BlockInputStream(byStream)));
            }

            // Check for custom compression ID introduced in 24w05a.
            case ID_CUSTOM -> {
                var byStream = readBytesIntoArray(length);
                String idName = DataInputStream.readUTF(new DataInputStream(byStream));
                // if (!idName.equals(ID_ZSTD))
                {
                    this.debugln("READ", x, z, "unknown id name " + idName);
                    cir.setReturnValue(null);
                    return;
                }
            }
        }
    }

    @Inject(
        method = "open",
        cancellable = true,
        at = @At(value = "RETURN", ordinal = 1, shift = At.Shift.BEFORE))
    private void openCustomStream(int x, int z, CallbackInfoReturnable<DataOutputStream> cir) {
        if (writeLz4) {
            RegionFile self = (RegionFile) (Object) this;
            cir.setReturnValue(new DataOutputStream(new LZ4BlockOutputStream(self.new ChunkBuffer(x, z))));
        }
    }

    @ModifyArg(
        method = "write(I[BI)V",
        at = @At(value = "INVOKE", target = "Ljava/io/RandomAccessFile;writeByte(I)V"))
    private int writeCompressionId(int value) {
        if (writeLz4) {
            return ID_LZ4;
        }
        return value;
    }

    @Unique
    private ByteArrayInputStream readBytesIntoArray(int length) throws IOException {
        var byArray = new byte[length - 1];
        this.file.readFully(byArray);
        return new ByteArrayInputStream(byArray);
    }
}
