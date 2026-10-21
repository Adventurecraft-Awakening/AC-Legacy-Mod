package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.chunk.LevelRegion;
import dev.adventurecraft.awakening.client.render.material.MapMaterials;
import dev.adventurecraft.awakening.client.render.state.EnvironmentState;
import dev.adventurecraft.awakening.common.AC_PlayerTorch;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/** World-thread snapshots of celestial lighting and nearby block emitters. */
public final class RayLighting {
    public static final int MAX_LIGHTS = 32;
    public static final long HELD_TORCH_ID = Long.MIN_VALUE;
    public static final long UNTRACKED_TRANSPORT_REVISION = Long.MIN_VALUE;
    private static final double FULL_LIGHT_DISTANCE = 64;
    private static final double LIGHT_DISTANCE = 80;
    private static final double FADE_IN_SECONDS = 0.25;
    private static final double FADE_OUT_SECONDS = 0.35;
    private static final float MIN_INTENSITY = 0.001f;
    // One persistent slot belongs to the held torch, including its fade-out tail.
    private static final int WORLD_LIGHTS = MAX_LIGHTS - 1;
    // Values contain positions and numeric history only, never their weakly keyed world or chunks.
    private static final Map<Level, WorldLights> SOURCES = new WeakHashMap<>();

    private RayLighting() { }

    /** Position is camera-relative; intensity is normalized block emission and radius is in blocks. */
    public record Light(float x, float y, float z, float red, float green, float blue, float radius, float intensity) { }

    /** Stable map source identity and cubical emitter half-extent; zero preserves a legacy point source. */
    public record Emitter(long stableId, Light light, float emitterRadius) {
        public Emitter {
            Objects.requireNonNull(light, "light");
            if (!Float.isFinite(light.x()) || !Float.isFinite(light.y()) || !Float.isFinite(light.z())
                || !Float.isFinite(light.red()) || !Float.isFinite(light.green()) || !Float.isFinite(light.blue())
                || !Float.isFinite(light.radius()) || !Float.isFinite(light.intensity())
                || light.red() < 0 || light.green() < 0 || light.blue() < 0 || light.radius() <= 0
                || light.intensity() < 0 || !Float.isFinite(emitterRadius) || emitterRadius < 0) {
                throw new IllegalArgumentException("Emitter values must be finite with nonnegative radiance/size and positive range");
            }
        }
    }

    public record Snapshot(float sunX, float sunY, float sunZ, float sunR, float sunG, float sunB,
                           float skyIntensity, float brightness, List<Light> lights, EnvironmentState environment,
                           List<Emitter> emitters, long transportRevision) {
        public Snapshot {
            lights = List.copyOf(lights);
            Objects.requireNonNull(environment, "environment");
            var sorted = new ArrayList<>(emitters);
            sorted.sort(Comparator.comparingLong(Emitter::stableId));
            for (int i = 1; i < sorted.size(); i++) {
                if (sorted.get(i - 1).stableId() == sorted.get(i).stableId()) {
                    throw new IllegalArgumentException("Duplicate stable emitter identity " + sorted.get(i).stableId());
                }
            }
            emitters = List.copyOf(sorted);
        }
        public Snapshot(float sunX, float sunY, float sunZ, float sunR, float sunG, float sunB,
                        float skyIntensity, float brightness, List<Light> lights, EnvironmentState environment,
                        List<Emitter> emitters) {
            this(sunX, sunY, sunZ, sunR, sunG, sunB, skyIntensity, brightness, lights, environment, emitters,
                UNTRACKED_TRANSPORT_REVISION);
        }
        public Snapshot(float sunX, float sunY, float sunZ, float sunR, float sunG, float sunB,
                        float skyIntensity, float brightness, List<Light> lights, EnvironmentState environment) {
            this(sunX, sunY, sunZ, sunR, sunG, sunB, skyIntensity, brightness, lights, environment, legacyEmitters(lights));
        }
        public Snapshot(float sunX, float sunY, float sunZ, float sunR, float sunG, float sunB,
                        float skyIntensity, float brightness, List<Light> lights) {
            this(sunX, sunY, sunZ, sunR, sunG, sunB, skyIntensity, brightness, lights, EnvironmentState.NONE);
        }

        /** Source identity and sampling domain; motion and changes within an area domain retain history. */
        public long emitterFingerprint() {
            long hash = 0xcbf29ce484222325L;
            for (Emitter emitter : emitters) {
                long id = emitter.stableId();
                for (int byteIndex = 0; byteIndex < Long.BYTES; byteIndex++) {
                    hash = (hash ^ ((id >>> (byteIndex * 8)) & 255)) * 0x100000001b3L;
                }
                // A point is a discrete sample; an area source uses a surface PDF.
                // Reusing one as the other would reinterpret historical proposals.
                hash = (hash ^ (emitter.emitterRadius() > 0 ? 1 : 0)) * 0x100000001b3L;
            }
            return (hash ^ emitters.size()) * 0x100000001b3L;
        }
    }

    private static List<Emitter> legacyEmitters(List<Light> lights) {
        var result = new ArrayList<Emitter>(lights.size());
        for (int i = 0; i < lights.size(); i++) {
            result.add(new Emitter(Long.MIN_VALUE + 1 + i, lights.get(i), 0));
        }
        return result;
    }

    /** Collision-free within the supported map bounds; separate from the negative held/synthetic identities. */
    public static long blockSourceId(int x, int y, int z) {
        if (x < -(1 << 25) || x >= (1 << 25) || z < -(1 << 25) || z >= (1 << 25) || y < 0 || y > 255) {
            throw new IllegalArgumentException("Emitter position exceeds supported map bounds");
        }
        return ((x & 0x3ffffffL) << 34) | ((z & 0x3ffffffL) << 8) | y;
    }

    /** Chunk workers use only captured emission; dynamic block hooks run while reading the live world. */
    public static int emission(LevelSource view, Tile tile, int x, int y, int z) {
        if (view instanceof LevelRegion region) return region.emission(x, y, z);
        return MapMaterials.emission(view, tile, x, y, z);
    }

    /** Mesh tags exclude texture/tile emission; chunk workers use the captured block-only value. */
    public static int vertexEmission(LevelSource view, Tile tile, int x, int y, int z) {
        if (view instanceof LevelRegion region) return region.emission(x, y, z);
        return MapMaterials.vertexEmission(view, tile, x, y, z);
    }

    public static boolean isEmitterCandidate(Tile tile) {
        return tile != null && MapMaterials.mayEmit(tile);
    }

    /** Index once on the world thread, independent of terrain meshing, visibility, or trigger state. */
    public static void chunkLoaded(LevelChunk chunk) {
        WorldLights sources = SOURCES.get(chunk.level);
        if (sources == null) return;
        var positions = new HashSet<BlockPos>();
        byte[] blocks = chunk.blocks;
        if (blocks != null) {
            for (int index = 0; index < blocks.length; index++) {
                Tile tile = Tile.tiles[blocks[index] & 255];
                if (isEmitterCandidate(tile)) {
                    positions.add(new BlockPos((chunk.x << 4) + (index >> 11), index & 127,
                        (chunk.z << 4) + ((index >> 7) & 15)));
                }
            }
        }
        // Empty chunks must also be remembered, so capture never rescans their block volume.
        sources.chunks.put(chunkKey(chunk.x, chunk.z), positions);
    }

    public static void chunkUnloaded(Level world, int x, int z) {
        WorldLights sources = SOURCES.get(world);
        if (sources != null) {
            Set<BlockPos> positions = sources.chunks.remove(chunkKey(x, z));
            if (positions != null) for (BlockPos position : positions) sources.transport.forget(sourceId(position));
        }
    }

    public static void blockChanged(Level world, int x, int y, int z, int id) {
        WorldLights sources = SOURCES.get(world);
        if (sources == null) return;
        Set<BlockPos> positions = sources.chunks.get(chunkKey(x >> 4, z >> 4));
        var position = new BlockPos(x, y, z);
        boolean emitter = isEmitterCandidate(Tile.tiles[id & 255]);
        if (emitter || positions != null && positions.contains(position)) sources.transport.changed(sourceId(position));
        if (positions != null) {
            if (emitter) positions.add(position);
            else positions.remove(position);
        }
    }

    public static void clear(Level world) { SOURCES.remove(world); WorldAtmosphere.clear(world); }

    /** Call once per world frame. Only previously unindexed, already loaded chunks need a block scan. */
    public static Snapshot capture(Level world, double cameraX, double cameraY, double cameraZ, float partialTick) {
        return capture(world, cameraX, cameraY, cameraZ, partialTick, EnvironmentState.NONE);
    }

    public static Snapshot capture(Level world, double cameraX, double cameraY, double cameraZ, float partialTick,
                                   EnvironmentState environment) {
        WorldLights sources = SOURCES.computeIfAbsent(world, ignored -> new WorldLights());
        discoverLoadedChunks(world, sources, cameraX, cameraZ);
        long now = System.nanoTime();
        double seconds = sources.lastCapture == 0 ? 0 : Math.max(0, (now - sources.lastCapture) * 1.0e-9);
        sources.lastCapture = now;
        var candidates = new ArrayList<Candidate>();
        for (Set<BlockPos> positions : sources.chunks.values()) {
            var positionIterator = positions.iterator();
            while (positionIterator.hasNext()) {
                BlockPos position = positionIterator.next();
                Tile tile = Tile.tiles[world.getTile(position.x(), position.y(), position.z())];
                if (!isEmitterCandidate(tile)) {
                    sources.transport.changed(sourceId(position));
                    positionIterator.remove();
                    continue;
                }
                int emission = emission(world, tile, position.x(), position.y(), position.z());
                int color = lightColor(world, tile, position.x(), position.y(), position.z());
                float configuredIntensity = MapMaterials.lightIntensity(world, tile, position.x(), position.y(), position.z());
                float authoredIntensity = configuredIntensity >= 0.0F && Float.isFinite(configuredIntensity)
                    ? configuredIntensity : emission / 15f;
                float configuredRadius = MapMaterials.lightRadius(world, tile, position.x(), position.y(), position.z());
                float radius = configuredRadius > 0.0F && Float.isFinite(configuredRadius) ? configuredRadius : emission + 1;
                float emitterRadius = tile.isCubeShaped() || tile.id == 10 || tile.id == 11 ? 0.4f : 0.12f;
                // Track authored source state even while disabled or outside the camera's sampling radius.
                sources.transport.observe(sourceId(position), new SourceConfiguration(emission, color,
                    authoredIntensity, radius, emitterRadius));
                double x = position.x() + 0.5 - cameraX;
                double y = position.y() + 0.5 - cameraY;
                double z = position.z() + 0.5 - cameraZ;
                double distanceSquared = x * x + y * y + z * z;
                if (distanceSquared >= LIGHT_DISTANCE * LIGHT_DISTANCE) continue;
                if (emission == 0) continue;
                double fade = Math.clamp((Math.sqrt(distanceSquared) - FULL_LIGHT_DISTANCE) /
                    (LIGHT_DISTANCE - FULL_LIGHT_DISTANCE), 0, 1);
                float fadeAmount = (float) (1 - fade * fade * (3 - 2 * fade));
                float intensity = authoredIntensity * fadeAmount;
                if (intensity <= MIN_INTENSITY) continue;
                double priority = intensity * radius * radius / Math.max(distanceSquared, radius * radius);
                FadingLight previous = sources.active.get(position);
                if (previous != null && previous.selected) priority *= 1.15;
                candidates.add(new Candidate(position, color, radius, intensity, priority, emitterRadius));
            }
        }
        candidates.sort(Comparator.comparingDouble(Candidate::priority).reversed()
            .thenComparingInt(candidate -> candidate.position.x())
            .thenComparingInt(candidate -> candidate.position.y())
            .thenComparingInt(candidate -> candidate.position.z()));
        var selected = new LinkedHashMap<BlockPos, Candidate>();
        for (int i = 0; i < Math.min(WORLD_LIGHTS, candidates.size()); i++) {
            Candidate candidate = candidates.get(i);
            selected.put(candidate.position, candidate);
        }
        var iterator = sources.active.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            FadingLight light = entry.getValue();
            Candidate candidate = selected.get(entry.getKey());
            light.update(candidate, seconds);
            if (candidate == null && light.intensity < MIN_INTENSITY) iterator.remove();
        }
        for (Candidate candidate : selected.values()) {
            if (sources.active.containsKey(candidate.position)) continue;
            if (sources.active.size() == WORLD_LIGHTS) break; // Retiring slots fade before being reused.
            FadingLight light = new FadingLight(candidate.position.x() + 0.5, candidate.position.y() + 0.5,
                candidate.position.z() + 0.5, candidate.color, candidate.radius);
            light.update(candidate, 0); // Start the fade when discovered, not before a loading hitch.
            sources.active.put(candidate.position, light);
        }
        var lights = new ArrayList<Light>(MAX_LIGHTS);
        updateHeldTorch(world, sources, partialTick, seconds);
        if (sources.held != null && sources.held.intensity >= MIN_INTENSITY) lights.add(sources.held.relativeTo(cameraX, cameraY, cameraZ));
        for (FadingLight light : sources.active.values()) {
            if (light.intensity >= MIN_INTENSITY) lights.add(light.relativeTo(cameraX, cameraY, cameraZ));
        }
        // The primary-light snapshot tracks every nearby source independently of the legacy 31-source budget.
        var allCandidates = new HashMap<BlockPos, Candidate>();
        for (Candidate candidate : candidates) allCandidates.put(candidate.position, candidate);
        var emitterIterator = sources.emitters.entrySet().iterator();
        while (emitterIterator.hasNext()) {
            var entry = emitterIterator.next();
            Candidate candidate = allCandidates.get(entry.getKey());
            entry.getValue().update(candidate, seconds);
            if (candidate == null && entry.getValue().intensity < MIN_INTENSITY) emitterIterator.remove();
        }
        for (Candidate candidate : candidates) {
            if (sources.emitters.containsKey(candidate.position)) continue;
            FadingLight light = new FadingLight(candidate.position.x() + 0.5, candidate.position.y() + 0.5,
                candidate.position.z() + 0.5, candidate.color, candidate.radius);
            light.emitterRadius = candidate.emitterRadius;
            light.update(candidate, 0);
            sources.emitters.put(candidate.position, light);
        }
        var emitters = new ArrayList<Emitter>(sources.emitters.size() + 1);
        if (sources.held != null && sources.held.intensity >= MIN_INTENSITY) {
            emitters.add(new Emitter(HELD_TORCH_ID, sources.held.relativeTo(cameraX, cameraY, cameraZ), 0.08f));
        }
        for (var entry : sources.emitters.entrySet()) {
            FadingLight light = entry.getValue();
            if (light.intensity < MIN_INTENSITY) continue;
            BlockPos position = entry.getKey();
            emitters.add(new Emitter(blockSourceId(position.x(), position.y(), position.z()),
                light.relativeTo(cameraX, cameraY, cameraZ), light.emitterRadius));
        }

        double angle = world.getTimeOfDay(partialTick) * Math.PI * 2;
        float sunX = 0, sunY = (float) Math.cos(angle), sunZ = (float) Math.sin(angle);
        float daylight = world.dimension.natural ? 0 : Math.max(0, sunY);
        float weather = (1 - world.getRainLevel(partialTick) * 0.75f) * (1 - world.getThunderLevel(partialTick) * 0.5f);
        float sunlight = daylight * weather;
        float skyIntensity = world.dimension.natural ? 0 : 0.08f + 0.72f * sunlight;
        if (environment.fog().x() > 0) {
            sunX = environment.sun().x(); sunY = environment.sun().y(); sunZ = environment.sun().z();
            sunlight = environment.sun().w(); skyIntensity = environment.sky().a();
        }
        Minecraft client = Minecraft.instance;
        float brightness = client == null || client.options == null ? 0 :
            Math.clamp(((ExGameOptions) client.options).ofBrightness(), 0, 1);
        return new Snapshot(sunX, sunY, sunZ, sunlight, sunlight * 0.97f, sunlight * 0.9f,
            skyIntensity, brightness, lights, environment, emitters, sources.transport.revision());
    }

    private static void discoverLoadedChunks(Level world, WorldLights sources, double cameraX, double cameraZ) {
        ExWorld loaded = (ExWorld) world;
        var iterator = sources.chunks.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long key = entry.getKey();
            if (loaded.ac$hasChunk((int) (key >> 32), (int) key)) continue;
            for (BlockPos position : entry.getValue()) sources.transport.forget(sourceId(position));
            iterator.remove();
        }
        int x0 = (int) Math.floor(cameraX - LIGHT_DISTANCE) >> 4;
        int x1 = (int) Math.floor(cameraX + LIGHT_DISTANCE) >> 4;
        int z0 = (int) Math.floor(cameraZ - LIGHT_DISTANCE) >> 4;
        int z1 = (int) Math.floor(cameraZ + LIGHT_DISTANCE) >> 4;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!sources.chunks.containsKey(chunkKey(x, z)) && loaded.ac$hasChunk(x, z)) {
                    chunkLoaded(world.getChunk(x, z));
                }
            }
        }
    }

    private static void updateHeldTorch(Level world, WorldLights sources, float partialTick, double seconds) {
        Minecraft client = Minecraft.instance;
        boolean hasPlayer = client != null && client.level == world && client.player != null;
        boolean active = hasPlayer && AC_PlayerTorch.isTorchActive();
        sources.transport.heldTorch(active);
        boolean created = sources.held == null && active;
        if (created) sources.held = new FadingLight(0, 0, 0, 0xFFD194, 16);
        if (sources.held == null) return;
        if (hasPlayer) {
            var player = client.player;
            sources.held.x = player.xOld + (player.x - player.xOld) * partialTick;
            sources.held.y = player.yOld + (player.y - player.yOld) * partialTick;
            sources.held.z = player.zOld + (player.z - player.zOld) * partialTick;
        }
        sources.held.fade(active ? 1 : 0, created ? 0 : seconds);
        if (!active && sources.held.intensity < MIN_INTENSITY) sources.held = null;
    }

    private static int lightColor(Level world, Tile tile, int x, int y, int z) {
        int configured = MapMaterials.lightColor(world, tile, x, y, z);
        if (configured >= 0) return configured;
        if (tile == AC_Blocks.redstonePower || tile.id == 76) return 0xFF3020;
        if (tile.id == 10 || tile.id == 11 || tile.id == 50 || tile.id == 51) return 0xFFD194;
        return 0xFFFFFF;
    }

    private static long chunkKey(int x, int z) { return (long) x << 32 | (z & 0xFFFFFFFFL); }

    private static long sourceId(BlockPos position) { return blockSourceId(position.x(), position.y(), position.z()); }

    /** Camera-independent authored values, before range selection and temporal fade-in/out. */
    public record SourceConfiguration(int emission, int color, float intensity, float radius, float emitterRadius) { }

    /** Pure source-state policy, also used to verify transport epochs without constructing a game world. */
    public static final class TransportTracker {
        private final Map<Long, SourceConfiguration> configurations = new HashMap<>();
        private long revision = 1;
        private boolean heldTorch;

        public long revision() { return revision; }

        public void observe(long id, SourceConfiguration configuration) {
            SourceConfiguration previous = configurations.put(id, Objects.requireNonNull(configuration, "configuration"));
            if (previous != null && !previous.equals(configuration)) revision++;
        }

        public void changed(long id) { configurations.remove(id); revision++; }
        public void forget(long id) { configurations.remove(id); }

        public void heldTorch(boolean active) {
            if (heldTorch == active) return;
            heldTorch = active;
            revision++;
        }
    }

    private static final class WorldLights {
        final Map<Long, Set<BlockPos>> chunks = new HashMap<>();
        final Map<BlockPos, FadingLight> active = new LinkedHashMap<>();
        final Map<BlockPos, FadingLight> emitters = new HashMap<>();
        final TransportTracker transport = new TransportTracker();
        FadingLight held;
        long lastCapture;
    }

    private static final class FadingLight {
        double x, y, z;
        float red, green, blue, radius, intensity;
        float emitterRadius = 0.12f;
        boolean selected;

        FadingLight(double x, double y, double z, int color, float radius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.red = (color >> 16 & 255) / 255f;
            this.green = (color >> 8 & 255) / 255f;
            this.blue = (color & 255) / 255f;
            this.radius = radius;
        }

        void update(Candidate candidate, double seconds) {
            this.selected = candidate != null;
            if (candidate != null) {
                float amount = (float) (1 - Math.exp(-seconds / FADE_IN_SECONDS));
                this.red += ((candidate.color >> 16 & 255) / 255f - this.red) * amount;
                this.green += ((candidate.color >> 8 & 255) / 255f - this.green) * amount;
                this.blue += ((candidate.color & 255) / 255f - this.blue) * amount;
                this.radius += (candidate.radius - this.radius) * amount;
                this.emitterRadius += (candidate.emitterRadius - this.emitterRadius) * amount;
            }
            this.fade(candidate == null ? 0 : candidate.intensity, seconds);
        }

        void fade(float target, double seconds) {
            double duration = target > this.intensity ? FADE_IN_SECONDS : FADE_OUT_SECONDS;
            this.intensity += (target - this.intensity) * (float) (1 - Math.exp(-seconds / duration));
        }

        Light relativeTo(double cameraX, double cameraY, double cameraZ) {
            return new Light((float) (this.x - cameraX), (float) (this.y - cameraY), (float) (this.z - cameraZ),
                this.red, this.green, this.blue, this.radius, this.intensity);
        }
    }

    private record Candidate(BlockPos position, int color, float radius, float intensity, double priority,
                             float emitterRadius) { }
}
