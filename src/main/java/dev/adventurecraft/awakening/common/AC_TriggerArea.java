package dev.adventurecraft.awakening.common;

import net.minecraft.util.io.CompoundTag;

public class AC_TriggerArea {
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

    public AC_TriggerArea(int var1, int var2, int var3, int var4, int var5, int var6) {
        this.minX = var1;
        this.minY = var2;
        this.minZ = var3;
        this.maxX = var4;
        this.maxY = var5;
        this.maxZ = var6;
    }

    public boolean isPointInside(int var1, int var2, int var3) {
        if (var1 >= this.minX && var1 <= this.maxX) {
            if (var2 >= this.minY && var2 <= this.maxY) {
                return this.minZ <= var3 && var3 <= this.maxZ;
            }
            return false;
        }
        return false;
    }

    public CompoundTag getTagCompound() {
        CompoundTag var1 = new CompoundTag();
        var1.put("minX", this.minX);
        var1.put("minY", this.minY);
        var1.put("minZ", this.minZ);
        var1.put("maxX", this.maxX);
        var1.put("maxY", this.maxY);
        var1.put("maxZ", this.maxZ);
        return var1;
    }

    public static AC_TriggerArea getFromTagCompound(CompoundTag var0) {
        int var1 = var0.getInt("minX");
        int var2 = var0.getInt("minY");
        int var3 = var0.getInt("minZ");
        int var4 = var0.getInt("maxX");
        int var5 = var0.getInt("maxY");
        int var6 = var0.getInt("maxZ");
        return new AC_TriggerArea(var1, var2, var3, var4, var5, var6);
    }
}
