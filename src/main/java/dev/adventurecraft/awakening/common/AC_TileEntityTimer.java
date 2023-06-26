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

    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        this.resetOnTrigger = tag.getBoolean("resetOnTrigger");
        this.timeActive = tag.getInt("timeActive");
        this.timeDeactive = tag.getInt("timeDeactive");
        this.timeDelay = tag.getInt("timeDelay");
        this.ticks = tag.getInt("ticks");
        this.ticksDelay = tag.getInt("ticksDelay");
        this.active = tag.getBoolean("active");
        this.canActivate = tag.getBoolean("canActivate");
    }

    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        tag.put("resetOnTrigger", this.resetOnTrigger);
        tag.put("timeActive", this.timeActive);
        tag.put("timeDeactive", this.timeDeactive);
        tag.put("timeDelay", this.timeDelay);
        tag.put("ticks", this.ticks);
        tag.put("ticksDelay", this.ticksDelay);
        tag.put("active", this.active);
        tag.put("canActivate", this.canActivate);
    }
}
