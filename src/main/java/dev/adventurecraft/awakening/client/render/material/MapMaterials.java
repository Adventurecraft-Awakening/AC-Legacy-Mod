package dev.adventurecraft.awakening.client.render.material;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.chunk.LevelRegion;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.tile.Tile;

import java.io.File;
import java.util.Objects;

/**
 * Process-wide view of the current map's material set. The table is immutable
 * after loading; a map reload swaps the reference between frames.
 */
public final class MapMaterials {

    /** A material table and its generation are always published together. */
    public record Snapshot(MapMaterialSet materials, Level level, long revision) {
        public Snapshot {
            Objects.requireNonNull(materials, "materials");
        }
    }

    private static volatile Snapshot snapshot = new Snapshot(MapMaterialSet.empty(), null, 0);

    private MapMaterials() { }

    public static long revision() {
        return snapshot.revision();
    }

    public static MapMaterialSet current() {
        return snapshot.materials();
    }

    public static boolean isCurrent(Level level) {
        return level != null && level == snapshot.level();
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public static boolean isCurrent(Snapshot captured) {
        return captured != null && captured == snapshot;
    }

    private static synchronized void publish(Level level, MapMaterialSet materials) {
        snapshot = new Snapshot(materials, level, snapshot.revision() + 1);
    }

    public static void load(Level level, File directory) {
        MapMaterialSet loaded = MapMaterialSet.load(level, directory);
        publish(level, loaded);
        ACMod.LOGGER.info("Map materials: {} ({})", directory,
            loaded.loadedFromFile() ? "materials.json" : "generated defaults");
    }

    public static synchronized void unload(Level level) {
        if (level != null && level != snapshot.level()) return;
        publish(null, MapMaterialSet.empty());
    }

    public static boolean createDefaults(Level level, File directory) {
        boolean created = MapMaterialSet.createDefaults(level, directory);
        if (created) {
            load(level, directory);
        }
        return created;
    }

    public static boolean save(Level level, File directory) {
        boolean saved = MapMaterialSet.save(level, directory);
        if (saved) {
            load(level, directory);
        }
        return saved;
    }

    private static int textureNum(Tile tile) {
        int atlas = tile instanceof ExBlock ex ? ex.getTextureNum() : 0;
        return Math.max(0, Math.min(3, atlas));
    }

    private static MapMaterialSet materials(LevelSource view) {
        return view instanceof LevelRegion region ? region.materials() : current();
    }

    private static MaterialProfile tileProfile(MapMaterialSet materials, LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return null;
        int meta = view.getData(x, y, z) & 15;
        int atlas = textureNum(tile);
        try {
            return materials.tileProfile(atlas, tile.getTexture(0, meta));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static int emission(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return 0;
        MapMaterialSet materials = materials(view);
        int meta = view.getData(x, y, z) & 15;
        int emission = materials.emission(tile.id, meta);
        if (emission >= 0) return emission;
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        if (tileProfile != null && tileProfile.emission != null) {
            return Math.max(0, Math.min(15, tileProfile.emission));
        }
        try {
            int atlas = textureNum(tile);
            int texture = tile.getTexture(0, meta);
            int textureEmission = materials.tileEmission(atlas, texture);
            if (textureEmission > 0) return textureEmission;
        } catch (RuntimeException ignored) { }
        return legacyEmission(view, tile, x, y, z);
    }

    /**
     * Stable block emission for vertex tags. Per-tile and texture emission stays
     * in the shader so an animated companion can become dark without remeshing.
     */
    public static int vertexEmission(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return 0;
        MapMaterialSet materials = materials(view);
        int emission = materials.emission(tile.id, view.getData(x, y, z) & 15);
        return emission >= 0 ? emission : legacyEmission(view, tile, x, y, z);
    }

    private static int legacyEmission(LevelSource view, Tile tile, int x, int y, int z) {
        int blockLight = tile instanceof ExBlock ex
            ? ex.getBlockLightValue(view, x, y, z)
            : Tile.lightEmission[tile.id];
        return Math.clamp(blockLight, 0, 15);
    }

    public static int flags(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return 0;
        MapMaterialSet materials = materials(view);
        int flags = materials.flags(tile.id, view.getData(x, y, z) & 15);
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        if (tileProfile != null && tileProfile.flags != null) flags |= tileProfile.flags;
        return flags;
    }

    public static int lightColor(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return -1;
        MapMaterialSet materials = materials(view);
        int color = materials.lightColor(tile.id, view.getData(x, y, z) & 15);
        if (color >= 0) return color;
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        return tileProfile != null && tileProfile.lightColor != null ? tileProfile.lightColor : -1;
    }

    public static float lightRadius(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return -1.0F;
        MapMaterialSet materials = materials(view);
        float radius = materials.lightRadius(tile.id, view.getData(x, y, z) & 15);
        if (radius > 0.0F) return radius;
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        return tileProfile != null && tileProfile.lightRadius != null ? tileProfile.lightRadius : -1.0F;
    }

    public static float lightIntensity(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return -1.0F;
        MapMaterialSet materials = materials(view);
        float intensity = materials.lightIntensity(tile.id, view.getData(x, y, z) & 15);
        if (intensity >= 0.0F) return intensity;
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        return tileProfile != null && tileProfile.lightIntensity != null ? tileProfile.lightIntensity : -1.0F;
    }

    public static float transmission(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return 0.0F;
        MapMaterialSet materials = materials(view);
        float transmission = materials.transmission(tile.id, view.getData(x, y, z) & 15);
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        if (tileProfile != null && tileProfile.transmission != null) {
            transmission = Math.max(transmission, tileProfile.transmission);
        }
        return transmission;
    }

    public static float ior(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return 1.5F;
        MapMaterialSet materials = materials(view);
        float ior = materials.ior(tile.id, view.getData(x, y, z) & 15);
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        if (ior == 1.5F && tileProfile != null && tileProfile.ior != null) return tileProfile.ior;
        return ior;
    }

    public static float absorption(LevelSource view, Tile tile, int x, int y, int z, int channel) {
        if (tile == null) return 0.0F;
        MapMaterialSet materials = materials(view);
        float value = materials.absorption(tile.id, view.getData(x, y, z) & 15, channel);
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        if (value == 0.0F && tileProfile != null) {
            if (channel == 0 && tileProfile.absorptionR != null) return tileProfile.absorptionR;
            if (channel == 1 && tileProfile.absorptionG != null) return tileProfile.absorptionG;
            if (channel == 2 && tileProfile.absorptionB != null) return tileProfile.absorptionB;
        }
        return value;
    }

    public static int gameplayLight(LevelSource view, Tile tile, int x, int y, int z) {
        if (tile == null) return -1;
        MapMaterialSet materials = materials(view);
        int light = materials.gameplayLight(tile.id, view.getData(x, y, z) & 15);
        if (light >= 0) return light;
        MaterialProfile tileProfile = tileProfile(materials, view, tile, x, y, z);
        return tileProfile != null && tileProfile.gameplayLight != null ? tileProfile.gameplayLight : -1;
    }

    public static boolean mayEmit(Tile tile) {
        return tile != null && current().mayEmit(tile.id);
    }

    public static boolean isTransmissive(int id) {
        return current().isTransmissive(id);
    }

    public static String albedoPath(int atlas) {
        return current().albedoPath(atlas);
    }

    public static String normalPath(int atlas) {
        return current().normalPath(atlas);
    }

    public static String specularPath(int atlas) {
        return current().specularPath(atlas);
    }

    public static File directory() {
        return current().directory();
    }
}
