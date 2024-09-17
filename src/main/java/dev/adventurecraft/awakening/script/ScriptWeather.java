package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class ScriptWeather {

    Level world;

    ScriptWeather(Level var1) {
        this.world = var1;
    }

    public void setPrecipitating(boolean var1) {
        this.world.levelData.setRaining(var1);
    }

    public boolean getPrecipitating() {
        return this.world.levelData.isRaining();
    }

    public void setTemperatureOffset(double var1) {
        ((ExWorldProperties) this.world.levelData).setTempOffset(var1);
    }

    public double getTemperatureOffset() {
        return ((ExWorldProperties) this.world.levelData).getTempOffset();
    }

    public void setThundering(boolean var1) {
        this.world.levelData.setThundering(var1);
    }

    public boolean getThundering() {
        return this.world.levelData.isThundering();
    }
}
