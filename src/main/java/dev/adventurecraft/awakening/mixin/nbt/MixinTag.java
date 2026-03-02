package dev.adventurecraft.awakening.mixin.nbt;

import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.nbt.IntArrayTag;
import dev.adventurecraft.awakening.nbt.LongArrayTag;
import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@Mixin(Tag.class)
public abstract class MixinTag implements ExTag {

    @Invoker
    public abstract void invokeWrite(DataOutput output)
        throws IOException;

    @Invoker
    public abstract void invokeRead(DataInput input)
        throws IOException;

    @Override
    public abstract void accept(TagVisitor visitor);

    @Override
    public abstract Tag copy();

    @Overwrite
    public static Tag fromId(byte id) {
        return switch (id) {
            case 0 -> new EndTag();
            case 1 -> new ByteTag();
            case 2 -> new ShortTag();
            case 3 -> new IntTag();
            case 4 -> new LongTag();
            case 5 -> new FloatTag();
            case 6 -> new DoubleTag();
            case 7 -> new ByteArrayTag();
            case 8 -> new StringTag();
            case 9 -> new ListTag();
            case 10 -> new CompoundTag();
            case 11 -> new IntArrayTag();
            case 12 -> new LongArrayTag();
            default -> null;
        };
    }

    @Overwrite
    public static String getTypeFromId(byte id) {
        return switch (id) {
            case 0 -> "TAG_End";
            case 1 -> "TAG_Byte";
            case 2 -> "TAG_Short";
            case 3 -> "TAG_Int";
            case 4 -> "TAG_Long";
            case 5 -> "TAG_Float";
            case 6 -> "TAG_Double";
            case 7 -> "TAG_Byte_Array";
            case 8 -> "TAG_String";
            case 9 -> "TAG_List";
            case 10 -> "TAG_Compound";
            case 11 -> "TAG_Int_Array";
            case 12 -> "TAG_Long_Array";
            default -> "UNKNOWN";
        };
    }
}
