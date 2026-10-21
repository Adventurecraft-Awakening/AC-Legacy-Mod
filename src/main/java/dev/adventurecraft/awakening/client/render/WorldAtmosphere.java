package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.Color4;
import dev.adventurecraft.awakening.client.render.state.EnvironmentState;
import dev.adventurecraft.awakening.client.render.state.Vector4;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Material;

import java.util.Map;
import java.util.WeakHashMap;

/** World-thread environment capture. Weather history advances with simulation time, never rendering FPS. */
public final class WorldAtmosphere {
    private static final double PERIOD_SECONDS = 4096;
    // Numeric history contains no reference to its weakly keyed world.
    private static final Map<Level, WeatherHistory> HISTORY = new WeakHashMap<>();

    private WorldAtmosphere() { }

    public static void clear(Level world) { HISTORY.remove(world); }

    public static EnvironmentState capture(Minecraft client, double cameraX, double cameraY,
                                           double cameraZ, float partialTick) {
        if (client.level == null || client.cameraEntity == null || !RenderSystem.isInitialized() ||
            !RenderSystem.renderer().rayTracingSupported()) return EnvironmentState.NONE;
        Level world = client.level;
        ExGameOptions options = (ExGameOptions) client.options;
        boolean sky = !world.dimension.natural;
        float partial = Math.clamp(partialTick, 0, 1);
        WeatherHistory history = HISTORY.get(world);
        if (history == null) {
            history = new WeatherHistory(world.getTime(), partial,
                sky ? world.getRainLevel(partial) : 0, sky ? world.getThunderLevel(partial) : 0);
            HISTORY.put(world, history);
        }
        if (client.pause) partial = history.partial;
        history.update(world.getTime(), partial, client.pause,
            sky ? world.getRainLevel(partial) : 0, sky ? world.getThunderLevel(partial) : 0);

        double angle = world.getTimeOfDay(partial) * Math.PI * 2;
        float sunY = (float) Math.cos(angle), sunZ = (float) Math.sin(angle);
        float daylight = sky ? Math.max(0, sunY) : 0;
        float strength = daylight * (1 - history.rain * .75f) * (1 - history.thunder * .5f);
        // Biome/celestial sky color includes world weather, but no eye-local cave or fluid fog.
        var worldSky = world.getSkyColor(client.cameraEntity, partial);
        Color4 skyColor = new Color4((float) worldSky.x, (float) worldSky.y, (float) worldSky.z,
            sky ? .08f + .72f * strength : 0);
        int medium = client.cameraEntity.isUnderLiquid(Material.WATER) ? 1 :
            client.cameraEntity.isUnderLiquid(Material.LAVA) ? 2 : 0;
        float exposure = skyExposure(world, cameraX, cameraY, cameraZ, sky);
        var properties = (ExWorldProperties) world.levelData;
        boolean authoredFog = properties.isOverrideFogColor() || properties.isOverrideFogDensity();
        float haze = sky && !authoredFog && medium == 0 ? .0008f + .003f * history.rain + .001f * history.thunder : 0;
        float coverage = Math.clamp(.45f + .4f * history.rain + .05f * history.thunder, .45f, .9f);
        float clouds = sky && !options.isCloudsOff() ? 1 : 0;
        float cloudHeight = world.dimension.getCloudHeight() + options.ofCloudsHeight() * 25;
        return new EnvironmentState(new Vector4(0, sunY, sunZ, strength), skyColor,
            new Vector4(history.rain, history.thunder, history.wetness, (float) history.animation),
            new Vector4(modulo(cameraX, PERIOD_SECONDS), (float) cameraY, modulo(cameraZ, PERIOD_SECONDS), medium),
            new Vector4(cloudHeight, coverage, clouds, 0), new Vector4(1, haze, exposure, 0));
    }

    /** Sample eye-local exposure without moving the world-anchored material patterns. */
    public static EnvironmentState forView(EnvironmentState environment, WorldRenderContext context, Level world) {
        if (environment.fog().x() <= 0) return environment;
        double[] inverse = RenderMath.inverse(context.view().view());
        float exposure = skyExposure(world, context.cameraX() + inverse[12],
            context.cameraY() + inverse[13], context.cameraZ() + inverse[14], !world.dimension.natural);
        Vector4 fog = environment.fog();
        if (Float.compare(exposure, fog.z()) == 0) return environment;
        return new EnvironmentState(environment.sun(), environment.sky(), environment.weather(), environment.camera(),
            environment.clouds(), new Vector4(fog.x(), fog.y(), exposure, fog.w()));
    }

    private static float skyExposure(Level world, double x, double y, double z, boolean sky) {
        if (!sky || y < 0) return 0;
        if (y >= 128) return 1;
        int blockX = (int) Math.floor(x), blockY = (int) Math.floor(y), blockZ = (int) Math.floor(z);
        if (!((ExWorld) world).ac$hasChunk(blockX >> 4, blockZ >> 4)) return 0;
        return Math.clamp(world.getBrightness(LightLayer.SKY, blockX, blockY, blockZ), 0, 15) / 15f;
    }

    private static float modulo(double value, double period) {
        return (float) (value - Math.floor(value / period) * period);
    }

    private static float approach(float value, float target, double seconds, double duration) {
        return value + (target - value) * (float) (1 - Math.exp(-seconds / duration));
    }

    private static final class WeatherHistory {
        double lastTime;
        double animation;
        float partial;
        float rain, thunder, wetness;

        WeatherHistory(long ticks, float partial, float rain, float thunder) {
            this.lastTime = ticks + (double) partial;
            this.partial = partial;
            this.animation = (Math.floorMod(ticks, (long) (PERIOD_SECONDS * 20)) + partial) / 20.0;
            this.rain = Math.clamp(rain, 0, 1);
            this.thunder = Math.clamp(thunder, 0, 1);
        }

        void update(long ticks, float partial, boolean paused, float rain, float thunder) {
            double time = ticks + (double) partial;
            // Map scripts can move the world clock by hours; those jumps must not
            // instantly soak/dry surfaces or skip through the cloud animation.
            double seconds = paused ? 0 : Math.clamp((time - this.lastTime) / 20.0, 0, 1);
            this.lastTime = time;
            this.partial = partial;
            if (seconds == 0) return;
            this.animation = (this.animation + seconds) % PERIOD_SECONDS;
            this.rain = approach(this.rain, Math.clamp(rain, 0, 1), seconds, 2);
            this.thunder = approach(this.thunder, Math.clamp(thunder, 0, 1), seconds, 2);
            // Ground soaks over several seconds and dries over several minutes after rain ends.
            this.wetness = approach(this.wetness, this.rain, seconds, this.rain > this.wetness ? 20 : 120);
        }
    }
}
