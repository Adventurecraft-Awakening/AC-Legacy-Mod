package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class ScriptTime {

    Level world;

    ScriptTime(Level var1) {
        this.world = var1;
    }

    public float get() {
        return ((ExWorld) this.world).getTimeOfDay();
    }

    public void set(long var1) {
        ((ExWorld) this.world).setTimeOfDay(var1);
    }

    public float getTime() {
        return ((ExWorld) this.world).getTimeOfDay();
    }

    public void setTime(long var1) {
        ((ExWorld) this.world).setTimeOfDay(var1);
    }

    public float getRate() {
        return ((ExWorldProperties) this.world.levelData).getTimeRate();
    }

    public void setRate(float var1) {
        ((ExWorldProperties) this.world.levelData).setTimeRate(var1);
    }

    public long getTickCount() {
        return this.world.getTime();
    }

    public void sleep(float seconds) {
        ((ExWorld) this.world).getScript().sleep(seconds);
    }
}
