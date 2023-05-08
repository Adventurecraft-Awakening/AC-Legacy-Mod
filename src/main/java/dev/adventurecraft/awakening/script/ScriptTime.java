package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class ScriptTime {

    World world;

    ScriptTime(World var1) {
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
        return ((ExWorldProperties) this.world.properties).getTimeRate();
    }

    public void setRate(float var1) {
        ((ExWorldProperties) this.world.properties).setTimeRate(var1);
    }

    public long getTickCount() {
        return this.world.getWorldTime();
    }

    public void sleep(float var1) {
        ((ExWorld) this.world).getScript().sleep(var1);
    }
}
