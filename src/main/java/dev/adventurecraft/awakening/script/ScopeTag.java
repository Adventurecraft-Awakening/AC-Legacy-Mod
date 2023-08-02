package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.util.io.CompoundTag;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class ScopeTag {

    public static CompoundTag getTagFromScope(Scriptable scriptable) {
        var tag = new CompoundTag();
        Object[] ids = scriptable.getIds();

        for (Object id : ids) {
            if (id instanceof CharSequence key) {
                String name = key.toString();
                Object value = scriptable.get(name, scriptable);
                saveProperty(tag, name, value);
            }
        }

        return tag;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void saveProperty(CompoundTag tag, String name, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof CharSequence string) {
            tag.put("String_" + name, string.toString());
        } else if (value instanceof Boolean bool) {
            tag.put("Boolean_" + name, bool);
        } else if (value instanceof Number number) {
            double doubleValue = number.doubleValue();
            float floatValue = number.floatValue();
            long longValue = number.longValue();
            int intValue = number.intValue();
            short shortValue = number.shortValue();

            if (doubleValue != (double) floatValue) {
                tag.put("Double_" + name, doubleValue);
            } else if (floatValue != (float) longValue) {
                tag.put("Float_" + name, floatValue);
            } else if (longValue != (long) intValue) {
                tag.put("Long_" + name, longValue);
            } else if (intValue != shortValue) {
                tag.put("Integer_" + name, intValue);
            } else {
                tag.put("Short_" + name, shortValue);
            }
        } else if (Undefined.isUndefined(value)) {
            // Ignore
        } else if (value instanceof Scriptable) {
            // Ignore
        } else {
            logUnsupportedProp("write", name, value);
        }
    }

    public static void loadScopeFromTag(Scriptable scriptable, CompoundTag tag) {
        for (String key : ((ExCompoundTag) tag).getKeys()) {
            String[] elements = key.split("_", 2);
            if (elements.length != 2) {
                logUnsupportedProp("decode", key, ((ExCompoundTag) tag).getValue(key));
                continue;
            }

            String type = elements[0];
            String name = elements[1];
            switch (type) {
                case "String" -> {
                    String value = tag.getString(key);
                    scriptable.put(name, scriptable, value);
                }
                case "Boolean" -> {
                    boolean value = tag.getBoolean(key);
                    scriptable.put(name, scriptable, value);
                }
                case "Double" -> {
                    double value = tag.getDouble(key);
                    scriptable.put(name, scriptable, value);
                }
                case "Float" -> {
                    float value = tag.getFloat(key);
                    scriptable.put(name, scriptable, value);
                }
                case "Long" -> {
                    long value = tag.getLong(key);
                    scriptable.put(name, scriptable, value);
                }
                case "Integer" -> {
                    int value = tag.getInt(key);
                    scriptable.put(name, scriptable, value);
                }
                case "Short" -> {
                    short value = tag.getShort(key);
                    scriptable.put(name, scriptable, value);
                }
                default -> {
                    logUnsupportedProp("read", key, ((ExCompoundTag) tag).getValue(key));
                }
            }
        }
    }

    private static void logUnsupportedProp(String op, String name, Object value) {
        Class<?> type = value != null ? value.getClass() : null;
        ACMod.LOGGER.warn("Unsupported ({}}) type of property: {} = {} ({})", op, name, value, type);
    }
}
