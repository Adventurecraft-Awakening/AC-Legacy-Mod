package dev.adventurecraft.awakening.client.render.chunk;

import java.util.Arrays;

/** Lazy lighting derived from one immutable terrain capture, owned by its builder. */
public final class TerrainLightCache {
    private static final byte UNKNOWN = -1;
    private static final long SKY_TOUCHED = 1L << 32;

    private final Samples samples;
    private final int volume;
    private final int stepX;
    private final int stepZ;
    private final boolean enabled;
    private final byte[] source;
    private final byte[] effective;
    private long queries;
    private long effectiveHits;
    private long sourceSamples;
    private long sourceHits;

    public TerrainLightCache(int volume, int stepX, int stepZ, Samples samples) {
        this(volume, stepX, stepZ, samples,
            Boolean.parseBoolean(System.getProperty("ac.terrain.lightCache", "true")));
    }

    public TerrainLightCache(int volume, int stepX, int stepZ, Samples samples, boolean enabled) {
        this.volume = volume;
        this.stepX = stepX;
        this.stepZ = stepZ;
        this.samples = java.util.Objects.requireNonNull(samples);
        this.enabled = enabled;
        this.source = new byte[enabled ? volume : 0];
        this.effective = new byte[enabled ? volume : 0];
        this.reset();
    }

    public void reset() {
        Arrays.fill(this.source, UNKNOWN);
        Arrays.fill(this.effective, UNKNOWN);
        this.queries = 0;
        this.effectiveHits = 0;
        this.sourceSamples = 0;
        this.sourceHits = 0;
    }

    public int brightness(int index, boolean checkNeighbors) {
        this.queries++;
        if (!checkNeighbors) return light(this.source(index));

        boolean cacheable = this.enabled && this.contains(index);
        if (cacheable && this.effective[index] != UNKNOWN) {
            this.effectiveHits++;
            long sample = unpack(this.effective[index]);
            this.samples.replay(sample);
            return light(sample);
        }

        long sample;
        if (this.samples.neighborLit(index)) {
            sample = this.source(index + 1);
            sample = this.clampedSource(index + this.stepX, sample);
            sample = this.clampedSource(index - this.stepX, sample);
            sample = this.clampedSource(index + this.stepZ, sample);
            sample = this.clampedSource(index - this.stepZ, sample);
        } else {
            sample = this.source(index);
        }
        if (cacheable) this.effective[index] = encode(sample);
        return light(sample);
    }

    private long clampedSource(int index, long previous) {
        // Preserve the original short circuit: skipped neighbors must not touch skylight.
        if (light(previous) >= 15) return pack(15, touchedSky(previous));
        long next = this.source(index);
        return pack(Math.max(light(previous), light(next)), touchedSky(previous) || touchedSky(next));
    }

    private long source(int index) {
        boolean cacheable = this.enabled && this.contains(index);
        if (cacheable && this.source[index] != UNKNOWN) {
            this.sourceHits++;
            long sample = unpack(this.source[index]);
            this.samples.replay(sample);
            return sample;
        }
        this.sourceSamples++;
        // Invalid indices deliberately reach the source, preserving its bounds checks.
        long sample = this.samples.sample(index);
        if (cacheable) this.source[index] = encode(sample);
        return sample;
    }

    private boolean contains(int index) {
        return index >= 0 && index < this.volume;
    }

    private static byte encode(long sample) {
        int light = light(sample);
        // Unusual captured sky-darkening values retain their uncached integer behavior.
        return light < 0 || light > 15 ? UNKNOWN : (byte) (light | (touchedSky(sample) ? 16 : 0));
    }

    private static long unpack(byte value) {
        return pack(value & 15, (value & 16) != 0);
    }

    public static long pack(int light, boolean touchedSky) {
        return Integer.toUnsignedLong(light) | (touchedSky ? SKY_TOUCHED : 0);
    }

    public static int light(long sample) {
        return (int) sample;
    }

    public static boolean touchedSky(long sample) {
        return (sample & SKY_TOUCHED) != 0;
    }

    public Statistics statistics() {
        return new Statistics(this.queries, this.effectiveHits, this.sourceSamples, this.sourceHits,
            this.source.length + this.effective.length, this.enabled);
    }

    public record Statistics(long queries, long effectiveHits, long sourceSamples, long sourceHits,
                             int cachedBytes, boolean enabled) { }

    public interface Samples {
        /** Returns packed light and publishes any sampled skylight before later reads can fail. */
        long sample(int index);
        boolean neighborLit(int index);
        /** Replays the sampled-skylight side effect when a cached value replaces source reads. */
        void replay(long sample);
    }
}
