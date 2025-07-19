package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.nbt.CompoundTag;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class ScopeTag {

    public static CompoundTag getTagFromScope(Scriptable scriptable) {
        var tag = new CompoundTag();
        for (Object key : scriptable.getIds()) {
            String name = key.toString();
            Object value = scriptable.get(name, scriptable);
            saveProperty(tag, name, value);
        }
        return tag;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void saveProperty(CompoundTag tag, String name, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof CharSequence string) {
            tag.putString("String_" + name, string.toString());
        }
        else if (value instanceof Boolean bool) {
            tag.putBoolean("Boolean_" + name, bool);
        }
        else if (value instanceof Number number) {
            double doubleValue = number.doubleValue();
            float floatValue = number.floatValue();
            if (doubleValue != (double) floatValue) {
                tag.putDouble("Double_" + name, doubleValue);
                return;
            }

            long longValue = number.longValue();
            if (floatValue != (float) longValue) {
                tag.putFloat("Float_" + name, floatValue);
                return;
            }

            int intValue = number.intValue();
            if (longValue != (long) intValue) {
                tag.putLong("Long_" + name, longValue);
                return;
            }

            short shortValue = number.shortValue();
            if (intValue != (int) shortValue) {
                tag.putInt("Integer_" + name, intValue);
                return;
            }
            tag.putShort("Short_" + name, shortValue);
        }
        else if (Undefined.isUndefined(value)) {
            // Ignore
        }
        else if (value instanceof Scriptable) {
            // Ignore
        }
        else {
            logUnsupportedProp("write", name, value);
        }
    }

    public static void loadScopeFromTag(Scriptable scriptable, CompoundTag tag) {
        var exTag = (ExCompoundTag) tag;
        for (String key : exTag.getKeys()) {
            String[] elements = key.split("_", 2);
            if (elements.length != 2) {
                logUnsupportedProp("decode", key, exTag.getTag(key));
                continue;
            }

            String type = elements[0];
            String name = elements[1];
            Object value = switch (type) {
                case "String" -> tag.getString(key);
                case "Boolean" -> tag.getBoolean(key);
                case "Double" -> tag.getDouble(key);
                case "Float" -> tag.getFloat(key);
                case "Long" -> tag.getLong(key);
                case "Integer" -> tag.getInt(key);
                case "Short" -> tag.getShort(key);
                default -> {
                    logUnsupportedProp("read", key, exTag.getTag(key));
                    yield null;
                }
            };
            if (value == null) {
                continue;
            }
            scriptable.put(name, scriptable, value);
        }
    }

    private static void logUnsupportedProp(String op, String name, Object value) {
        Class<?> type = value != null ? value.getClass() : null;
        ACMod.LOGGER.warn("Unsupported ({}}) type of property: {} = {} ({})", op, name, value, type);
    }
}
