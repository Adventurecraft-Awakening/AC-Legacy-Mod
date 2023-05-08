package dev.adventurecraft.awakening.script;

import java.util.Iterator;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.util.io.CompoundTag;
import org.mozilla.javascript.Scriptable;

public class ScopeTag {

    public static CompoundTag getTagFromScope(Scriptable var0) {
        CompoundTag var1 = new CompoundTag();
        Object[] var2 = var0.getIds();

        for (Object var5 : var2) {
            if (var5 instanceof String var6) {
                Object var7 = var0.get(var6, var0);
                saveProperty(var1, var6, var7);
            }
        }

        return var1;
    }

    private static void saveProperty(CompoundTag var0, String var1, Object var2) {
        if (var2 instanceof String var3) {
            var0.put("String_" + var1, var3);
        } else if (var2 instanceof Boolean) {
            boolean var11 = (Boolean) var2;
            var0.put("Boolean_" + var1, var11);
        } else if (var2 instanceof Number var12) {
            double var4 = var12.doubleValue();
            float var6 = var12.floatValue();
            long var7 = var12.longValue();
            int var9 = var12.intValue();
            short var10 = var12.shortValue();
            if (var4 != (double) var6) {
                var0.put("Double_" + var1, var4);
            } else if (var6 != (float) var7) {
                var0.put("Float_" + var1, var6);
            } else if (var7 != (long) var9) {
                var0.put("Long_" + var1, var7);
            } else if (var9 != var10) {
                var0.put("Integer_" + var1, var9);
            } else {
                var0.put("Short_" + var1, var10);
            }
        }
    }

    public static void loadScopeFromTag(Scriptable var0, CompoundTag var1) {
        for (String var3 : ((ExCompoundTag) var1).getKeys()) {
            String[] var4 = var3.split("_", 2);
            if (var4.length != 2) {
                ACMod.LOGGER.warn(String.format("Unknown key in tag: %s %d\n", var3, var4.length));
                continue;
            }
            
            String var5 = var4[0];
            String var6 = var4[1];
            switch (var5) {
                case "String":
                    String var7 = var1.getString(var3);
                    var0.put(var6, var0, var7);
                    break;
                case "Boolean":
                    boolean var9 = var1.getBoolean(var3);
                    var0.put(var6, var0, (var9));
                    break;
                case "Double":
                    double var10 = var1.getDouble(var3);
                    var0.put(var6, var0, (var10));
                    break;
                case "Float":
                    float var11 = var1.getFloat(var3);
                    var0.put(var6, var0, (var11));
                    break;
                case "Long":
                    long var12 = var1.getLong(var3);
                    var0.put(var6, var0, (var12));
                    break;
                case "Integer":
                    int var13 = var1.getInt(var3);
                    var0.put(var6, var0, (var13));
                    break;
                case "Short":
                    short var14 = var1.getShort(var3);
                    var0.put(var6, var0, (var14));
                    break;
                default:
                    ACMod.LOGGER.warn(String.format("Unknown type: %s", var5));
                    break;
            }
        }
    }
}
