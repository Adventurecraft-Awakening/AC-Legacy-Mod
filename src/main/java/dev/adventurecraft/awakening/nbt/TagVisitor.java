package dev.adventurecraft.awakening.nbt;

import net.minecraft.nbt.*;

public interface TagVisitor {

    default void visit(StringTag stringTag) {
    }

    default void visit(ByteTag byteTag) {
    }

    default void visit(ShortTag shortTag) {
    }

    default void visit(IntTag intTag) {
    }

    default void visit(LongTag longTag) {
    }

    default void visit(FloatTag floatTag) {
    }

    default void visit(DoubleTag doubleTag) {
    }

    default void visit(ByteArrayTag byteArrayTag) {
    }

    default void visit(IntArrayTag intArrayTag) {
    }

    default void visit(LongArrayTag longArrayTag) {
    }

    default void visit(ListTag listTag) {
    }

    default void visit(CompoundTag compoundTag) {
    }

    default void visit(EndTag endTag) {
    }
}
