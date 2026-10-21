package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.state.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/** Separate std140 records follow view, object and material update frequencies. */
final class VulkanDrawConstants {
    static final int VIEW_BYTES = 352;
    static final int OBJECT_BYTES = 192;
    static final int MATERIAL_BYTES = 32;

    // Offsets are float indices in the std140 blocks declared in draw_constants.glsl.
    private static final class ViewOffset {
        static final int PROJECTION = 0;
        static final int VIEW = 16;
        static final int FOG_COLOR = 32;
        static final int FOG_MODE = 36;
        static final int FOG_START = 37;
        static final int FOG_END = 38;
        static final int FOG_DENSITY = 39;
        static final int FIRST_LIGHT_POSITION = 40;
        static final int SECOND_LIGHT_POSITION = 44;
        static final int FIRST_LIGHT_DIFFUSE = 48;
        static final int SECOND_LIGHT_DIFFUSE = 52;
        static final int LIGHT_AMBIENT = 56;
        static final int ENABLED_LIGHTS = 60;
        static final int FOG_DISTANCE = 61;
        static final int ENV_SUN = 64;
        static final int ENV_SKY = 68;
        static final int ENV_WEATHER = 72;
        static final int ENV_CAMERA = 76;
        static final int ENV_CLOUDS = 80;
        static final int ENV_FOG = 84;
    }

    private static final class ObjectOffset {
        static final int MODEL = 0;
        static final int TEXTURE_MATRIX = 16;
        static final int TINT = 32;
        static final int NORMAL_0 = 36;
        static final int NORMAL_1 = 40;
        static final int NORMAL_2 = 44;
    }

    private static final class MaterialOffset {
        static final int ALPHA_TEST = 0;
        static final int ALPHA_REFERENCE = 1;
        static final int ALPHA_COMPARISON = 2;
        static final int LIGHTING = 3;
        static final int COLOR_MATERIAL = 4;
        static final int MASKED_CLEAR = 5;
        static final int EFFECT = 6;
    }

    private VulkanDrawConstants() { }

    static final class Block {
        private final float[] values;
        private final int hash;

        /** Takes ownership of a fresh array created by this packer. */
        private Block(float[] values) {
            this.values = values;
            this.hash = Arrays.hashCode(values);
        }

        /** Writes native-order floats at the current position and advances it by the block size. */
        void writeTo(ByteBuffer target) {
            target.order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.values);
            target.position(target.position() + this.values.length * Float.BYTES);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Block block && Arrays.equals(this.values, block.values);
        }
    }

    record Blocks(Block view, Block object, Block material) { }

    static Blocks pack(DrawParameters state) {
        return new Blocks(
            view(state.view()),
            object(state.view(), state.object(), state.material().lighting()),
            material(state.material()));
    }

    static Block view(FrameView frame) {
        float[] values = new float[VIEW_BYTES / Float.BYTES];
        frame.projection().copyTo(values, ViewOffset.PROJECTION);
        frame.view().copyTo(values, ViewOffset.VIEW);
        FogState fog = frame.fog();
        putColor(values, ViewOffset.FOG_COLOR, fog.color());
        values[ViewOffset.FOG_MODE] = VulkanStateCodes.fogMode(fog.mode());
        values[ViewOffset.FOG_START] = fog.start();
        values[ViewOffset.FOG_END] = fog.end();
        values[ViewOffset.FOG_DENSITY] = fog.density();
        LightingState lights = frame.lights();
        putVector(values, ViewOffset.FIRST_LIGHT_POSITION, lights.first().position());
        putVector(values, ViewOffset.SECOND_LIGHT_POSITION, lights.second().position());
        putColor(values, ViewOffset.FIRST_LIGHT_DIFFUSE, lights.first().diffuse());
        putColor(values, ViewOffset.SECOND_LIGHT_DIFFUSE, lights.second().diffuse());
        putColor(values, ViewOffset.LIGHT_AMBIENT, lights.ambient());
        values[ViewOffset.ENABLED_LIGHTS] = (lights.first().enabled() ? 1 : 0) | (lights.second().enabled() ? 2 : 0);
        values[ViewOffset.FOG_DISTANCE] = VulkanStateCodes.fogDistance(fog.distance());
        EnvironmentState environment = frame.environment();
        putVector(values, ViewOffset.ENV_SUN, environment.sun());
        putColor(values, ViewOffset.ENV_SKY, environment.sky());
        putVector(values, ViewOffset.ENV_WEATHER, environment.weather());
        putVector(values, ViewOffset.ENV_CAMERA, environment.camera());
        putVector(values, ViewOffset.ENV_CLOUDS, environment.clouds());
        putVector(values, ViewOffset.ENV_FOG, environment.fog());
        return new Block(values);
    }

    static Block object(FrameView frame, ObjectData object, boolean lighting) {
        float[] values = new float[OBJECT_BYTES / Float.BYTES];
        object.model().copyTo(values, ObjectOffset.MODEL);
        object.textureTransform().copyTo(values, ObjectOffset.TEXTURE_MATRIX);
        putColor(values, ObjectOffset.TINT, object.tint());
        putNormalMatrix(values, frame.view(), object.model(), lighting);
        return new Block(values);
    }

    static Block material(MaterialState material) {
        return material(material, false);
    }

    private static Block material(MaterialState material, boolean maskedClear) {
        float[] values = new float[MATERIAL_BYTES / Float.BYTES];
        values[MaterialOffset.ALPHA_TEST] = material.alphaCompare() == CompareOp.ALWAYS ? 0 : 1;
        values[MaterialOffset.ALPHA_REFERENCE] = material.alphaReference();
        values[MaterialOffset.ALPHA_COMPARISON] = VulkanStateCodes.comparison(material.alphaCompare());
        values[MaterialOffset.LIGHTING] = material.lighting() ? 1 : 0;
        values[MaterialOffset.COLOR_MATERIAL] = VulkanStateCodes.colorMaterial(material.colorMaterial());
        values[MaterialOffset.MASKED_CLEAR] = maskedClear ? 1 : 0;
        values[MaterialOffset.EFFECT] = material.effect().shaderCode();
        return new Block(values);
    }

    static Blocks clear(Color4 color) {
        var frame = new FrameView(Matrix4.identity(), Matrix4.identity(), FogState.NONE, LightingState.NONE);
        var object = ObjectData.IDENTITY.withTint(color);
        return new Blocks(view(frame), object(frame, object, false), material(MaterialState.UNLIT, true));
    }

    private static void putNormalMatrix(float[] target, Matrix4 view, Matrix4 model, boolean lighting) {
        target[ObjectOffset.NORMAL_0] = 1;
        target[ObjectOffset.NORMAL_1 + 1] = 1;
        target[ObjectOffset.NORMAL_2 + 2] = 1;
        if (!lighting) {
            return;
        }
        // Only the upper model-view 3x3 is needed; avoid temporary 4x4 matrices and cloned arrays.
        float a = product(view, model, 0, 0);
        float b = product(view, model, 0, 1);
        float c = product(view, model, 0, 2);
        float d = product(view, model, 1, 0);
        float e = product(view, model, 1, 1);
        float f = product(view, model, 1, 2);
        float g = product(view, model, 2, 0);
        float h = product(view, model, 2, 1);
        float i = product(view, model, 2, 2);
        float determinant = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        if (!Float.isFinite(determinant) || Math.abs(determinant) < 1.0e-12f) {
            return;
        }
        float inverse = 1 / determinant;
        target[ObjectOffset.NORMAL_0] = (e * i - f * h) * inverse;
        target[ObjectOffset.NORMAL_0 + 1] = (c * h - b * i) * inverse;
        target[ObjectOffset.NORMAL_0 + 2] = (b * f - c * e) * inverse;
        target[ObjectOffset.NORMAL_1] = (f * g - d * i) * inverse;
        target[ObjectOffset.NORMAL_1 + 1] = (a * i - c * g) * inverse;
        target[ObjectOffset.NORMAL_1 + 2] = (c * d - a * f) * inverse;
        target[ObjectOffset.NORMAL_2] = (d * h - e * g) * inverse;
        target[ObjectOffset.NORMAL_2 + 1] = (b * g - a * h) * inverse;
        target[ObjectOffset.NORMAL_2 + 2] = (a * e - b * d) * inverse;
    }

    private static float product(Matrix4 left, Matrix4 right, int row, int column) {
        float value = 0;
        for (int k = 0; k < 4; k++) {
            value += left.get(k * 4 + row) * right.get(column * 4 + k);
        }
        return value;
    }

    private static void putColor(float[] target, int offset, Color4 value) {
        target[offset] = value.r();
        target[offset + 1] = value.g();
        target[offset + 2] = value.b();
        target[offset + 3] = value.a();
    }

    private static void putVector(float[] target, int offset, Vector4 value) {
        target[offset] = value.x();
        target[offset + 1] = value.y();
        target[offset + 2] = value.z();
        target[offset + 3] = value.w();
    }
}
