package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class ScriptWeather {

    World world;

    ScriptWeather(World var1) {
        this.world = var1;
    }

    public void setPrecipitating(boolean var1) {
        this.world.properties.setRaining(var1);
    }

    public boolean getPrecipitating() {
        return this.world.properties.isRaining();
    }

    public void setTemperatureOffset(double var1) {
        ((ExWorldProperties) this.world.properties).setTempOffset(var1);
    }

    public double getTemperatureOffset() {
        return ((ExWorldProperties) this.world.properties).getTempOffset();
    }

    public void setThundering(boolean var1) {
        this.world.properties.setThundering(var1);
    }

    public boolean getThundering() {
        return this.world.properties.isThundering();
    }
}
