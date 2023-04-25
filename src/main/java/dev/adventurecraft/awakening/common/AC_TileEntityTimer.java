package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityTimer extends AC_TileEntityMinMax {
    public int ticks;
    public int timeActive;
    public int timeDeactive;
    public int timeDelay;
    public int ticksDelay;
    public boolean active = false;
    public boolean canActivate = true;
    public boolean resetOnTrigger;

    public void startActive() {
        this.active = true;
        this.ticks = this.timeActive;
        this.ticksDelay = this.timeDelay + 1;
    }

    public void tick() {
        if (this.ticksDelay > 0) {
            --this.ticksDelay;
            if (this.ticksDelay != 0) {
                return;
            }

            if (!this.resetOnTrigger) {
                ((ExWorld) this.world).getTriggerManager().addArea(this.x, this.y, this.z, new AC_TriggerArea(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ));
            } else {
                ExBlock.resetArea(this.world, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            }
        }

        if (this.ticks == 0) {
            if (this.active) {
                this.active = false;
                this.canActivate = false;
                this.ticks = this.timeDeactive;
                if (!this.resetOnTrigger) {
                    ((ExWorld) this.world).getTriggerManager().removeArea(this.x, this.y, this.z);
                }
            } else {
                this.canActivate = true;
            }
        } else {
            --this.ticks;
        }

    }

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.resetOnTrigger = var1.getBoolean("resetOnTrigger");
        this.timeActive = var1.getInt("timeActive");
        this.timeDeactive = var1.getInt("timeDeactive");
        this.timeDelay = var1.getInt("timeDelay");
        this.ticks = var1.getInt("ticks");
        this.ticksDelay = var1.getInt("ticksDelay");
        this.active = var1.getBoolean("active");
        this.canActivate = var1.getBoolean("canActivate");
    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        var1.put("resetOnTrigger", this.resetOnTrigger);
        var1.put("timeActive", this.timeActive);
        var1.put("timeDeactive", this.timeDeactive);
        var1.put("timeDelay", this.timeDelay);
        var1.put("ticks", this.ticks);
        var1.put("ticksDelay", this.ticksDelay);
        var1.put("active", this.active);
        var1.put("canActivate", this.canActivate);
    }
}
