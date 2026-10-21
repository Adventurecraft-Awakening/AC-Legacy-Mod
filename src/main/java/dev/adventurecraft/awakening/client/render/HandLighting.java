package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;

/** Minecraft's two directional item lights expressed as frame data. */
public final class HandLighting {
    private HandLighting() { }
    public static DrawParameters apply(DrawParameters parameters, Matrix4 transform) {
        Color4 diffuse = new Color4(.6F, .6F, .6F, 1);
        var lights = new LightingState(new LightState(true, direction(transform, .2F, 1, -.7F), diffuse),
            new LightState(true, direction(transform, -.2F, 1, .7F), diffuse), new Color4(.4F, .4F, .4F, 1));
        return parameters.withView(parameters.view().withLights(lights)).withMaterial(
            parameters.material().withLighting(true).withColorMaterial(ColorMaterial.AMBIENT_AND_DIFFUSE));
    }
    private static Vector4 direction(Matrix4 matrix, float x, float y, float z) {
        float length = (float) Math.sqrt(x*x + y*y + z*z);
        x /= length; y /= length; z /= length;
        return new Vector4(matrix.get(0)*x + matrix.get(4)*y + matrix.get(8)*z,
            matrix.get(1)*x + matrix.get(5)*y + matrix.get(9)*z,
            matrix.get(2)*x + matrix.get(6)*y + matrix.get(10)*z, 0);
    }
    public static DrawParameters mirrored(DrawParameters parameters) {
        var raster = parameters.raster();
        var mirrored = new RasterState(raster.depthTest(), raster.depthWrite(), raster.depthCompare(), raster.blend(),
            raster.cull(), raster.winding() == Winding.CLOCKWISE ? Winding.COUNTER_CLOCKWISE : Winding.CLOCKWISE,
            raster.colorMask(), raster.viewport(), raster.scissor(), raster.minDepth(), raster.maxDepth(), raster.lineWidth(), raster.depthBias());
        return parameters.withObject(parameters.object().withModel(parameters.object().model().scaled(-1, 1, 1))).withRaster(mirrored);
    }
}
