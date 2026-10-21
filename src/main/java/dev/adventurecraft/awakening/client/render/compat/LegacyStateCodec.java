package dev.adventurecraft.awakening.client.render.compat;

import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import java.nio.ByteBuffer;

/** Decodes inherited OpenGL state once at the compatibility boundary. */
public final class LegacyStateCodec {
    private LegacyStateCodec() { }

    public static DrawParameters fromLegacy(float[] projection, float[] modelView, float[] tint, float[] fogColor,
            int fogMode, float fogStart, float fogEnd, float fogDensity,
            boolean alphaTest, int alphaFunc, float alphaRef,
            boolean depthTest, boolean depthWrite, int depthFunc,
            boolean blendEnabled, int blendSrc, int blendDst,
            boolean cullEnabled, int cullFace, int frontFace, int colorMask,
            int[] viewport, int[] scissor, float minDepth, float maxDepth,
            float lineWidth, float polygonOffsetFactor, float polygonOffsetUnits,
            boolean lighting, float[] textureMatrix,
            float[] light0Position, float[] light1Position,
            float[] light0Diffuse, float[] light1Diffuse, float[] lightAmbient,
            boolean colorMaterial, int lightMask, float[] blendConstants,
            int colorMaterialMode, int fogDistanceMode, int frameHeight) {
        FogState fog = new FogState(switch (fogMode) {
            case 9729 -> FogMode.LINEAR; case 2048 -> FogMode.EXPONENTIAL; case 2049 -> FogMode.EXPONENTIAL_SQUARED;
            default -> FogMode.NONE;
        }, fogDistanceMode == -1 ? FogDistance.RADIAL_VERTEX : fogDistanceMode == 34139 ?
            FogDistance.RADIAL_FRAGMENT : FogDistance.EYE_PLANE, color(fogColor, Color4.TRANSPARENT), fogStart, fogEnd, fogDensity);
        LightingState lights = new LightingState(
            new LightState((lightMask & 1) != 0, vector(light0Position, new Vector4(.2f,1,-.7f,0)), color(light0Diffuse,new Color4(.6f,.6f,.6f,1))),
            new LightState((lightMask & 2) != 0, vector(light1Position, new Vector4(-.2f,1,.7f,0)), color(light1Diffuse,new Color4(.6f,.6f,.6f,1))),
            color(lightAmbient, new Color4(.4f,.4f,.4f,1)));
        FrameView view = new FrameView(projection(projection), Matrix4.identity(), fog, lights);
        ObjectData object = new ObjectData(matrix(modelView), matrix(textureMatrix), color(tint, Color4.WHITE));
        ColorMaterial tracking = !colorMaterial ? ColorMaterial.NONE : switch (colorMaterialMode) {
            case 4608 -> ColorMaterial.AMBIENT; case 4609 -> ColorMaterial.DIFFUSE;
            case 5634 -> ColorMaterial.AMBIENT_AND_DIFFUSE; case 5632 -> ColorMaterial.EMISSION;
            default -> ColorMaterial.NONE;
        };
        MaterialState material = new MaterialState(alphaTest ? compare(alphaFunc) : CompareOp.ALWAYS, alphaRef, lighting, tracking);
        RasterState raster = new RasterState(depthTest, depthWrite, compare(depthFunc),
            new BlendState(blendEnabled, blend(blendSrc), blend(blendDst), color(blendConstants, Color4.TRANSPARENT)),
            !cullEnabled ? CullMode.NONE : switch (cullFace) { case 1028 -> CullMode.FRONT; case 1032 -> CullMode.FRONT_AND_BACK; default -> CullMode.BACK; },
            frontFace == 2304 ? Winding.CLOCKWISE : Winding.COUNTER_CLOCKWISE, mask(colorMask),
            rect(viewport, frameHeight), rect(scissor, frameHeight), minDepth, maxDepth, lineWidth,
            new DepthBias(polygonOffsetFactor, polygonOffsetUnits));
        return new DrawParameters(view, object, material, raster);
    }

    /** Convert GL clip depth/Y once, leaving native shaders entirely in Vulkan coordinates. */
    public static Matrix4 projection(float[] source) {
        float[] values = matrix(source).copyArray();
        for (int column = 0; column < 4; column++) {
            int offset = column * 4;
            values[offset + 1] = -values[offset + 1];
            values[offset + 2] = .5f * (values[offset + 2] + values[offset + 3]);
        }
        return new Matrix4(values);
    }

    public static Rect rect(int[] value, int frameHeight) {
        return value == null ? null : new Rect(value[0], frameHeight - value[1] - value[3], value[2], value[3]);
    }

    public static ClearRequest clear(int mask, float[] color, float depth, boolean depthWrite, int colorMask, int[] scissor, int frameHeight) {
        return new ClearRequest((mask & 16384) != 0, depthWrite && (mask & 256) != 0, Color4.of(color), depth, mask(colorMask), rect(scissor,frameHeight));
    }
    public static VulkanRenderer.Mesh upload(VulkanRenderer renderer, ByteBuffer vertices, int mode) { return LegacyPrimitiveConverter.upload(renderer,vertices,mode); }
    public static VulkanRenderer.Mesh stream(VulkanRenderer renderer, ByteBuffer vertices, int mode) { return LegacyPrimitiveConverter.stream(renderer,vertices,mode); }

    public static ColorMask mask(int value) { return new ColorMask((value&1)!=0,(value&2)!=0,(value&4)!=0,(value&8)!=0); }
    public static CompareOp compare(int value) { return value >= 512 && value <= 519 ? CompareOp.values()[value-512] : CompareOp.LESS_OR_EQUAL; }
    public static SamplerState sampler(int min, int mag, int wrapU, int wrapV) {
        return new SamplerState(min == 9729 || min == 9985 || min == 9987 ? SamplerState.Filter.LINEAR : SamplerState.Filter.NEAREST,
            mag == 9729 ? SamplerState.Filter.LINEAR : SamplerState.Filter.NEAREST,
            min < 9984 || min > 9987 ? SamplerState.MipmapMode.NONE : min == 9986 || min == 9987 ? SamplerState.MipmapMode.LINEAR : SamplerState.MipmapMode.NEAREST,
            address(wrapU), address(wrapV));
    }
    private static SamplerState.AddressMode address(int value) { return switch(value) {
        case 33648 -> SamplerState.AddressMode.MIRRORED_REPEAT; case 10496,33071 -> SamplerState.AddressMode.CLAMP_TO_EDGE;
        case 33069 -> SamplerState.AddressMode.CLAMP_TO_BORDER; default -> SamplerState.AddressMode.REPEAT;
    }; }
    private static BlendFactor blend(int value) { return switch(value) {
        case 0 -> BlendFactor.ZERO; case 1 -> BlendFactor.ONE; case 768 -> BlendFactor.SRC_COLOR;
        case 769 -> BlendFactor.ONE_MINUS_SRC_COLOR; case 770 -> BlendFactor.SRC_ALPHA; case 771 -> BlendFactor.ONE_MINUS_SRC_ALPHA;
        case 772 -> BlendFactor.DST_ALPHA; case 773 -> BlendFactor.ONE_MINUS_DST_ALPHA; case 774 -> BlendFactor.DST_COLOR;
        case 775 -> BlendFactor.ONE_MINUS_DST_COLOR; case 776 -> BlendFactor.SRC_ALPHA_SATURATE;
        case 32769 -> BlendFactor.CONSTANT_COLOR; case 32770 -> BlendFactor.ONE_MINUS_CONSTANT_COLOR;
        case 32771 -> BlendFactor.CONSTANT_ALPHA; case 32772 -> BlendFactor.ONE_MINUS_CONSTANT_ALPHA; default -> BlendFactor.ONE;
    }; }
    private static Matrix4 matrix(float[] value) { return value == null ? Matrix4.identity() : new Matrix4(value); }
    private static Color4 color(float[] value, Color4 fallback) { return value == null ? fallback : Color4.of(value); }
    private static Vector4 vector(float[] value, Vector4 fallback) { return value == null ? fallback : Vector4.of(value); }
}
