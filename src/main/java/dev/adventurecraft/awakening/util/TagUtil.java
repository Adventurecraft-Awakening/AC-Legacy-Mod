package dev.adventurecraft.awakening.util;

import net.minecraft.nbt.*;

import java.util.Optional;

public final class TagUtil {

    public static Optional<Boolean> toBool(Tag tag) {
        return toByte(tag).map(b -> b != 0);
    }

    public static Tag fromBool(boolean value) {
        return new ByteTag(value ? (byte) 1 : 0);
    }

    public static Optional<Byte> toByte(Tag tag) {
        if (tag instanceof ByteTag bTag) {
            return Optional.of(bTag.data);
        }
        return Optional.empty();
    }

    public static Optional<Short> widenToShort(Tag tag) {
        if (tag instanceof ShortTag sTag) {
            return Optional.of(sTag.data);
        }
        return toByte(tag).map(b -> (short) (byte) b);
    }

    public static Optional<Integer> widenToInt(Tag tag) {
        if (tag instanceof IntTag iTag) {
            return Optional.of(iTag.data);
        }
        return widenToShort(tag).map(s -> (int) (short) s);
    }

    public static Optional<Long> widenToLong(Tag tag) {
        if (tag instanceof LongTag lTag) {
            return Optional.of(lTag.data);
        }
        return widenToInt(tag).map(i -> (long) (int) i);
    }

    public static byte getTypeId(Object item) {
        return switch (item) {
            case Byte b -> Tags.TAG_BYTE;
            case Short s -> Tags.TAG_SHORT;
            case Integer i -> Tags.TAG_INT;
            case Long l -> Tags.TAG_LONG;
            case Float f -> Tags.TAG_FLOAT;
            case Double d -> Tags.TAG_DOUBLE;
            case Tag tag -> tag.getId();
            default -> throwInvalidType(item);
        };
    }

    public static Tag wrapPrimitive(Object primitive) {
        return switch (primitive) {
            case Byte b -> new ByteTag(b);
            case Short s -> new ShortTag(s);
            case Integer i -> new IntTag(i);
            case Long l -> new LongTag(l);
            case Float f -> new FloatTag(f);
            case Double d -> new DoubleTag(d);
            case String st -> new StringTag(st);
            default -> {
                throwInvalidType(primitive);
                yield null;
            }
        };
    }

    public static Object tagToObject(Tag tag) {
        return switch (tag) {
            //case EndTag endTag -> null;
            case ByteTag byteTag -> byteTag.data;
            case ShortTag shortTag -> shortTag.data;
            case IntTag intTag -> intTag.data;
            case LongTag longTag -> longTag.data;
            case FloatTag floatTag -> floatTag.data;
            case DoubleTag doubleTag -> doubleTag.data;
            //case ByteArrayTag byteArrayTag -> null;
            case StringTag stringTag -> stringTag.contents;
            //case ListTag listTag -> null;
            //case CompoundTag compoundTag -> null;
            default -> {
                throwInvalidType(tag);
                yield null;
            }
        };
    }

    public static byte throwInvalidType(Object item) {
        throw new AssertionError("Unexpected type in list: " + item.getClass());
    }
}
