package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import org.jetbrains.annotations.NotNull;

public class AC_TileEntityTriggerInverter extends AC_TileEntityMinMax {
    
    public void set(@NotNull Coord min, @NotNull Coord max) {
        int data = this.level.getData(this.x, this.y, this.z);
        if (this.isSet() && data <= 0) {
            AC_Blocks.triggerInverter.onTriggerActivated(this.level, this.x, this.y, this.z);
        }

        this.setMin(min);
        this.setMax(max);
        if (data <= 0) {
            AC_Blocks.triggerInverter.onTriggerDeactivated(this.level, this.x, this.y, this.z);
        }
    }
}
