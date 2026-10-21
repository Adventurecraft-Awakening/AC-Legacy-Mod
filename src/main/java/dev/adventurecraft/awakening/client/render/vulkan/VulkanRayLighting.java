package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.RayLighting;
import dev.adventurecraft.awakening.client.render.state.Color4;
import dev.adventurecraft.awakening.client.render.state.Matrix4;
import dev.adventurecraft.awakening.client.render.state.Vector4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;
import java.util.List;

/** The std430 SceneLighting ABI shared with ray_lighting.glsl. Storage belongs to a frame slot. */
final class VulkanRayLighting {
    private static final int HEADER_BYTES = 10 * 16;
    private static final int LIGHT_BYTES = 2 * 16;
    static final int BYTES = HEADER_BYTES + RayLighting.MAX_LIGHTS * LIGHT_BYTES;
    static final int EMITTER_HEADER_BYTES = 16;
    static final int EMITTER_BYTES = 48;
    private static final RayLighting.Snapshot DEFAULT =
        new RayLighting.Snapshot(.35f, .85f, .4f, 1, .95f, .85f, 1, 0, List.of());
    private static final Color4 CELESTIAL_SKY = new Color4(.55f, .7f, 1, 1);

    private VulkanRayLighting() { }

    static int manyLightBytes(RayLighting.Snapshot source) {
        RayLighting.Snapshot snapshot = source == null ? DEFAULT : source;
        return Math.addExact(EMITTER_HEADER_BYTES, Math.multiplyExact(snapshot.emitters().size(), EMITTER_BYTES));
    }

    /**
     * Separate many-light std430 ABI: uvec4 header, then 48-byte records containing
     * position/range, color/intensity, stable ID low/high, cube half-extent and inclusive CDF.
     * A 50% uniform proposal keeps every emitter sampleable when power differs greatly.
     */
    static void writeEmitters(ByteBuffer target, RayLighting.Snapshot source) {
        RayLighting.Snapshot snapshot = source == null ? DEFAULT : source;
        int bytes = manyLightBytes(snapshot);
        if (target.isReadOnly()) throw new ReadOnlyBufferException();
        if (target.remaining() < bytes) throw new BufferOverflowException();
        ByteBuffer output = target.duplicate().order(ByteOrder.nativeOrder());
        List<RayLighting.Emitter> emitters = snapshot.emitters();
        long fingerprint = snapshot.emitterFingerprint();
        output.putInt(emitters.size()).putInt((int) fingerprint).putInt((int) (fingerprint >>> 32)).putInt(0);
        double total = 0;
        for (RayLighting.Emitter emitter : emitters) total += power(emitter);
        double cumulative = 0;
        for (int i = 0; i < emitters.size(); i++) {
            RayLighting.Emitter emitter = emitters.get(i);
            RayLighting.Light light = emitter.light();
            vector(output, light.x(), light.y(), light.z(), light.radius());
            vector(output, light.red(), light.green(), light.blue(), light.intensity());
            output.putInt((int) emitter.stableId()).putInt((int) (emitter.stableId() >>> 32));
            output.putFloat(emitter.emitterRadius());
            cumulative += power(emitter);
            double uniform = (i + 1.0) / emitters.size();
            float cdf = i + 1 == emitters.size() ? 1
                : (float) (total > 0 ? 0.5 * cumulative / total + 0.5 * uniform : uniform);
            output.putFloat(cdf);
        }
        target.position(target.position() + bytes);
    }

    private static double power(RayLighting.Emitter emitter) {
        RayLighting.Light light = emitter.light();
        double luminance = light.red() * 0.2126 + light.green() * 0.7152 + light.blue() * 0.0722;
        double area = emitter.emitterRadius() > 0 ? 24.0 * emitter.emitterRadius() * emitter.emitterRadius() : 1;
        return light.intensity() * luminance * area;
    }

    static void write(VulkanBuffer target, RayLighting.Snapshot source, Color4 clearColor, Matrix4 model) {
        RayLighting.Snapshot snapshot = source == null ? DEFAULT : source;
        target.write(bytes -> {
            vector(bytes, snapshot.sunX(), snapshot.sunY(), snapshot.sunZ(), 1);
            vector(bytes, snapshot.sunR(), snapshot.sunG(), snapshot.sunB(), snapshot.brightness());
            // The framebuffer clear color contains camera-local cave darkness, fluid fog and
            // cloud effects. It must not recolor world irradiance whenever the eye moves.
            var environment = snapshot.environment();
            Color4 sky = environment.fog().x() > 0 ? environment.sky() : CELESTIAL_SKY;
            vector(bytes, sky.r(), sky.g(), sky.b(), snapshot.skyIntensity());
            int count = Math.min(RayLighting.MAX_LIGHTS, snapshot.lights().size());
            // Camera translation anchors stochastic samples to the map rather than the moving eye.
            vector(bytes, count, -model.get(12), -model.get(13), -model.get(14));
            vector(bytes, environment.sun());
            vector(bytes, environment.sky().r(), environment.sky().g(), environment.sky().b(), environment.sky().a());
            vector(bytes, environment.weather());
            vector(bytes, environment.camera());
            vector(bytes, environment.clouds());
            vector(bytes, environment.fog());
            for (int i = 0; i < count; i++) {
                var light = snapshot.lights().get(i);
                vector(bytes, light.x(), light.y(), light.z(), light.radius());
                vector(bytes, light.red(), light.green(), light.blue(), light.intensity());
            }
        });
    }

    private static void vector(ByteBuffer target, float x, float y, float z, float w) {
        target.putFloat(x).putFloat(y).putFloat(z).putFloat(w);
    }

    private static void vector(ByteBuffer target, Vector4 value) {
        vector(target, value.x(), value.y(), value.z(), value.w());
    }
}
