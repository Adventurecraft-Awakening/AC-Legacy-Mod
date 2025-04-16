package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.tile.AC_Blocks;

public class AC_TileEntityTriggerInverter extends AC_TileEntityMinMax {
    
    public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int data = this.level.getData(this.x, this.y, this.z);
        if (this.isSet() && data <= 0) {
            AC_Blocks.triggerInverter.onTriggerActivated(this.level, this.x, this.y, this.z);
        }

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        if (data <= 0) {
            AC_Blocks.triggerInverter.onTriggerDeactivated(this.level, this.x, this.y, this.z);
        }
    }
}
