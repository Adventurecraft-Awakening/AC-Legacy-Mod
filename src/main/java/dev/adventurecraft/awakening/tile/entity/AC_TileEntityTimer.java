package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.nbt.CompoundTag;

public class AC_TileEntityTimer extends AC_TileEntityMinMax {

    public int ticks;
    private int timeActive;
    private int timeInactive;
    private int timeDelay;
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
                var area = new AC_TriggerArea(this.min(), this.max());
                ((ExWorld) this.level).getTriggerManager().addArea(this.x, this.y, this.z, area);
            }
            else {
                ExBlock.resetArea(this.level, this.min(), this.max());
            }
        }

        if (this.ticks == 0) {
            if (this.active) {
                this.active = false;
                this.canActivate = false;
                this.ticks = this.timeInactive;
                if (!this.resetOnTrigger) {
                    ((ExWorld) this.level).getTriggerManager().removeArea(this.x, this.y, this.z);
                }
            }
            else {
                this.canActivate = true;
            }
        }
        else {
            --this.ticks;
        }
    }

    // TODO: fix NBT typos in future version?

    public void load(CompoundTag tag) {
        super.load(tag);
        this.resetOnTrigger = tag.getBoolean("resetOnTrigger");
        this.timeActive = tag.getInt("timeActive");
        this.timeInactive = tag.getInt("timeDeactive");
        this.timeDelay = tag.getInt("timeDelay");
        this.ticks = tag.getInt("ticks");
        this.ticksDelay = tag.getInt("ticksDelay");
        this.active = tag.getBoolean("active");
        this.canActivate = tag.getBoolean("canActivate");
    }

    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putBoolean("resetOnTrigger", this.resetOnTrigger);
        tag.putInt("timeActive", this.timeActive);
        tag.putInt("timeDeactive", this.timeInactive);
        tag.putInt("timeDelay", this.timeDelay);
        tag.putInt("ticks", this.ticks);
        tag.putInt("ticksDelay", this.ticksDelay);
        tag.putBoolean("active", this.active);
        tag.putBoolean("canActivate", this.canActivate);
    }

    public int getTimeActive() {
        return this.timeActive;
    }

    public void setTimeActive(int value) {
        if (this.timeActive != value) {
            this.timeActive = value;
            this.setChanged();
        }
    }

    public int getTimeInactive() {
        return this.timeInactive;
    }

    public void setTimeInactive(int value) {
        if (this.timeInactive != value) {
            this.timeInactive = value;
            this.setChanged();
        }
    }

    public int getTimeDelay() {
        return this.timeDelay;
    }

    public void setTimeDelay(int value) {
        if (this.timeDelay != value) {
            this.timeDelay = value;
            this.setChanged();
        }
    }
}
