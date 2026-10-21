package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Material;

/** World atmosphere expressed as values, independent of drawing and compatibility state. */
public final class WorldEnvironment {
    private WorldEnvironment() { }

    public static Color4 clearColor(Minecraft mc, float partialTick, float brightness, boolean thickFog) {
        var world = mc.level;
        var sky = world.getSkyColor(mc.cameraEntity, partialTick);
        var fog = world.getFogColor(partialTick);
        float skyBlend = 1F - (float) Math.pow(.5, .25);
        float r = (float) fog.x + ((float) sky.x - (float) fog.x) * skyBlend;
        float g = (float) fog.y + ((float) sky.y - (float) fog.y) * skyBlend;
        float b = (float) fog.z + ((float) sky.z - (float) fog.z) * skyBlend;
        float rain = world.getRainLevel(partialTick);
        r *= 1 - rain * .5F;
        g *= 1 - rain * .5F;
        b *= 1 - rain * .4F;
        float thunder = 1 - world.getThunderLevel(partialTick) * .5F;
        r *= thunder;
        g *= thunder;
        b *= thunder;
        if (thickFog) {
            var cloud = world.getCloudColor(partialTick);
            r = (float) cloud.x;
            g = (float) cloud.y;
            b = (float) cloud.z;
        } else if (mc.cameraEntity.isUnderLiquid(Material.WATER)) {
            r = .02F; g = .02F; b = .2F;
        } else if (mc.cameraEntity.isUnderLiquid(Material.LAVA)) {
            r = .6F; g = .1F; b = 0;
        }
        r *= brightness;
        g *= brightness;
        b *= brightness;
        if (mc.options.anaglyph3d) {
            return new Color4((r * 30 + g * 59 + b * 11) / 100,
                (r * 30 + g * 70) / 100, (r * 30 + b * 70) / 100, 0);
        }
        return new Color4(r, g, b, 0);
    }

    public static FogState fog(Minecraft mc, float partialTick, int pass, float farPlane,
                               boolean thickFog, Color4 clearColor) {
        var options = (ExGameOptions) mc.options;
        var distance = options.ofFogFancy() ? FogDistance.RADIAL_FRAGMENT : FogDistance.RADIAL_VERTEX;
        var color = new Color4(clearColor.r(), clearColor.g(), clearColor.b(), 1);
        if (thickFog) return new FogState(FogMode.EXPONENTIAL, distance, color, 0, 1, .1F);
        if (mc.cameraEntity.isUnderLiquid(Material.WATER)) {
            return new FogState(FogMode.EXPONENTIAL, distance, color, 0, 1, options.ofClearWater() ? .02F : .1F);
        }
        if (mc.cameraEntity.isUnderLiquid(Material.LAVA)) {
            return new FogState(FogMode.EXPONENTIAL, distance, color, 0, 1, 2);
        }
        float start = options.ofFogStart(), end = 1;
        if (pass < 0) { start = 0; end = .8F; }
        if (mc.level.dimension.natural) { start = 0; end = 1; }
        var world = (ExWorld) mc.level;
        return new FogState(FogMode.LINEAR, distance, color, world.getFogStart(farPlane * start, partialTick),
            world.getFogEnd(farPlane * end, partialTick), 0);
    }
}
