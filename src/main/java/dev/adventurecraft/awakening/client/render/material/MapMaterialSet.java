package dev.adventurecraft.awakening.client.render.material;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.image.ImageLoadOptions;
import dev.adventurecraft.awakening.image.ImageLoader;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import dev.adventurecraft.awakening.tile.AC_BlockTransparent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.JsonArray;
import org.spongepowered.include.com.google.gson.JsonElement;
import org.spongepowered.include.com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-map material data. Behaviour is keyed by block id + metadata; appearance is
 * keyed by atlas + tile. The two namespaces are intentionally separate so a map
 * can repaint its atlas without changing which blocks emit light or transmit.
 */
public final class MapMaterialSet {

    public static final int ATLAS_COUNT = 4;
    public static final int TILE_COUNT = 256;
    public static final int BLOCK_COUNT = 256;
    public static final int META_COUNT = 16;
    private static final String MANIFEST_NAME = "materials.json";

    private final MaterialProfile defaults;
    private final Map<String, MaterialProfile> palette;
    private final MaterialProfile[] blocks = new MaterialProfile[BLOCK_COUNT * META_COUNT];
    private final MaterialProfile[] tiles = new MaterialProfile[ATLAS_COUNT * TILE_COUNT];

    private final int[] blockEmission = new int[BLOCK_COUNT * META_COUNT];
    private final int[] blockFlags = new int[BLOCK_COUNT * META_COUNT];
    private final int[] blockLightColor = new int[BLOCK_COUNT * META_COUNT];
    private final float[] blockLightRadius = new float[BLOCK_COUNT * META_COUNT];
    private final float[] blockLightIntensity = new float[BLOCK_COUNT * META_COUNT];
    private final float[] blockTransmission = new float[BLOCK_COUNT * META_COUNT];
    private final float[] blockIor = new float[BLOCK_COUNT * META_COUNT];
    private final float[] blockAbsorption = new float[BLOCK_COUNT * META_COUNT * 3];
    private final int[] blockGameplayLight = new int[BLOCK_COUNT * META_COUNT];
    private final MaterialProfile[] tileProfiles = new MaterialProfile[ATLAS_COUNT * TILE_COUNT];
    private final boolean[] mayEmit = new boolean[BLOCK_COUNT];
    private final boolean[] transmissive = new boolean[BLOCK_COUNT];
    private final int[] tileEmission = new int[ATLAS_COUNT * TILE_COUNT];

    private final String[] albedoPaths = {
        "/terrain.png", "/terrain.png", "/terrain2.png", "/terrain3.png"
    };
    private final String[] normalPaths = {
        "/terrain_n.png", "/terrain_n.png", "/terrain2_n.png", "/terrain3_n.png"
    };
    private final String[] specularPaths = {
        "/terrain_s.png", "/terrain_s.png", "/terrain2_s.png", "/terrain3_s.png"
    };

    private File directory;
    private boolean loadedFromFile;
    private JsonObject rawTiles;
    private TileMaterialTable tileParameters = TileMaterialTable.empty();

    private MapMaterialSet() {
        this.defaults = defaultProfile();
        this.palette = builtinPalette();
        java.util.Arrays.fill(this.blockEmission, -1);
        java.util.Arrays.fill(this.blockLightColor, -1);
        java.util.Arrays.fill(this.blockLightRadius, -1.0F);
        java.util.Arrays.fill(this.blockLightIntensity, -1.0F);
        java.util.Arrays.fill(this.blockGameplayLight, -1);
    }

    public static MapMaterialSet empty() {
        MapMaterialSet set = new MapMaterialSet();
        set.resolveAll();
        return set;
    }

    public static MapMaterialSet load(Level level, File directory) {
        MapMaterialSet set = generate(level);
        set.directory = directory;
        File manifest = directory == null ? null : new File(directory, MANIFEST_NAME);
        if (manifest != null && manifest.isFile()) {
            try (Reader reader = Files.newBufferedReader(manifest.toPath(), StandardCharsets.UTF_8)) {
                JsonObject root = new Gson().fromJson(reader, JsonObject.class);
                if (root != null) {
                    set.applyManifest(root);
                    set.loadedFromFile = true;
                }
            } catch (Exception error) {
                ACMod.LOGGER.warn("Cannot read map materials \"{}\"; using generated defaults.", manifest, error);
            }
        }
        set.loadEmissionAtlases();
        set.resolveAll();
        return set;
    }

    /** Writes a fresh manifest and neutral companion atlases without overwriting existing files. */
    public static boolean createDefaults(Level level, File directory) {
        if (directory == null) return false;
        MapMaterialSet set = generate(level);
        set.directory = directory;
        set.resolveAll();
        File manifest = new File(directory, MANIFEST_NAME);
        if (manifest.isFile()) return false;
        try {
            set.ensureCompanions(true);
            Files.writeString(manifest.toPath(), set.toJson().toString(), StandardCharsets.UTF_8);
            ACMod.LOGGER.info("Created default material set at {}", manifest);
            return true;
        } catch (IOException error) {
            ACMod.LOGGER.warn("Cannot create default material set at {}", manifest, error);
            return false;
        }
    }

    public static boolean save(Level level, File directory) {
        if (directory == null) return false;
        MapMaterialSet set = load(level, directory);
        set.directory = directory;
        try {
            set.ensureCompanions(false);
            Files.writeString(new File(directory, MANIFEST_NAME).toPath(), set.toJson().toString(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException error) {
            ACMod.LOGGER.warn("Cannot save material set at {}", directory, error);
            return false;
        }
    }

    public int emission(int id, int meta) {
        return this.blockEmission[blockIndex(id, meta)];
    }

    public int tileEmission(int atlas, int tile) {
        if (atlas < 0 || atlas >= ATLAS_COUNT || tile < 0 || tile >= TILE_COUNT) return 0;
        return this.tileEmission[atlas * TILE_COUNT + tile];
    }

    public MaterialProfile tileProfile(int atlas, int tile) {
        if (atlas < 0 || atlas >= ATLAS_COUNT || tile < 0 || tile >= TILE_COUNT) return null;
        return this.tileProfiles[atlas * TILE_COUNT + tile];
    }

    /** Immutable sparse shader overrides; unmentioned tiles keep their companion texture values. */
    public TileMaterialTable tileParameters() {
        return this.tileParameters;
    }

    public int flags(int id, int meta) {
        return this.blockFlags[blockIndex(id, meta)];
    }

    public int lightColor(int id, int meta) {
        return this.blockLightColor[blockIndex(id, meta)];
    }

    public float lightRadius(int id, int meta) {
        return this.blockLightRadius[blockIndex(id, meta)];
    }

    public float lightIntensity(int id, int meta) {
        return this.blockLightIntensity[blockIndex(id, meta)];
    }

    public float transmission(int id, int meta) {
        return this.blockTransmission[blockIndex(id, meta)];
    }

    public float ior(int id, int meta) {
        return this.blockIor[blockIndex(id, meta)];
    }

    public float absorption(int id, int meta, int channel) {
        return this.blockAbsorption[blockIndex(id, meta) * 3 + channel];
    }

    public int gameplayLight(int id, int meta) {
        return this.blockGameplayLight[blockIndex(id, meta)];
    }

    public boolean mayEmit(int id) {
        return id >= 0 && id < BLOCK_COUNT && this.mayEmit[id];
    }

    public boolean isTransmissive(int id) {
        return id >= 0 && id < BLOCK_COUNT && this.transmissive[id];
    }

    public String albedoPath(int atlas) {
        return this.albedoPaths[clampAtlas(atlas)];
    }

    public String normalPath(int atlas) {
        return this.normalPaths[clampAtlas(atlas)];
    }

    public String specularPath(int atlas) {
        return this.specularPaths[clampAtlas(atlas)];
    }

    public File directory() {
        return this.directory;
    }

    public boolean loadedFromFile() {
        return this.loadedFromFile;
    }

    private static int blockIndex(int id, int meta) {
        return (id & 255) * META_COUNT + (meta & 15);
    }

    private static int clampAtlas(int atlas) {
        return Math.max(0, Math.min(ATLAS_COUNT - 1, atlas));
    }

    private static int textureNum(Tile tile) {
        return tile instanceof ExBlock ex ? ex.getTextureNum() : 0;
    }

    private static MapMaterialSet generate(Level level) {
        MapMaterialSet set = new MapMaterialSet();
        for (int id = 0; id < BLOCK_COUNT; id++) {
            Tile tile = Tile.tiles[id];
            if (tile == null) continue;
            int atlas = clampAtlas(textureNum(tile));
            int subtypes = Math.max(1, Math.min(META_COUNT, ExBlock.subTypes[id]));
            String preset = presetFor(tile);
            for (int meta = 0; meta < subtypes; meta++) {
                MaterialProfile profile = new MaterialProfile();
                profile.material = preset;
                applyBehavior(profile, tile);
                set.blocks[blockIndex(id, meta)] = profile;
                for (int side = 0; side < 6; side++) {
                    try {
                        int texture = tile.getTexture(side, meta);
                        if (texture >= 0 && texture < TILE_COUNT) {
                            set.tiles[atlas * TILE_COUNT + texture] = new MaterialProfile(profile);
                        }
                    } catch (RuntimeException ignored) {
                        // Some blocks only accept a subset of metadata values.
                    }
                }
            }
        }
        return set;
    }

    private static String presetFor(Tile tile) {
        if (tile.material == Material.WATER) return "water";
        if (tile.material == Material.LAVA || tile.id == 10 || tile.id == 11) return "lava";
        if (tile.material == Material.GLASS || tile instanceof AC_BlockTransparent) return "glass";
        if (tile.id == 79) return "ice"; // Beta ice block
        if (Tile.lightEmission[tile.id] > 0.0F) return "torch";
        if (tile.material == Material.LEAVES) return "foliage";
        if (tile.material == Material.PLANT) return "foliage";
        if (tile.material == Material.METAL) return "metal";
        if (tile.material == Material.WOOD) return "wood";
        if (tile.material == Material.STONE) return "stone";
        if (tile.material == Material.DIRT || tile.material == Material.SAND || tile.material == Material.GRASS) {
            return "granular";
        }
        return "stone";
    }

    private static void applyBehavior(MaterialProfile profile, Tile tile) {
        // 171 = AC light bulb, 161 = AC redstone power. IDs keep this class usable
        // without forcing AC_Blocks static initialization.
        boolean dynamicEmitter = tile.id == 171 || tile.id == 161;
        float light = Tile.lightEmission[tile.id];
        if (light > 0.0F && !dynamicEmitter) {
            int emission = Math.max(1, Math.min(15, Math.round(light * 15.0F)));
            profile.emission = emission;
            profile.lightRadius = emission + 1.0F;
            profile.lightIntensity = emission / 15.0F;
        }
        if (tile.id == 171) {
            profile.material = "lamp";
            profile.lightColor = 0xFFD194;
        } else if (tile.id == 161) {
            profile.material = "lamp";
            profile.lightColor = 0xFF3020;
        } else if (tile.id == 50 || tile.id == 51) {
            profile.material = "torch";
            profile.lightColor = 0xFFD194;
        } else if (tile.id == 76) {
            profile.material = "torch";
            profile.lightColor = 0xFF3020;
        } else if (tile.id == 10 || tile.id == 11) {
            profile.material = "lava";
            profile.lightColor = 0xFFB060;
            profile.lightRadius = 18.0F;
            profile.lightIntensity = 1.2F;
        }

        if (tile.material == Material.WATER) {
            profile.addFlag(MaterialProfile.FLAG_WATER | MaterialProfile.FLAG_TRANSMISSIVE);
            profile.transmission = 1.0F;
            profile.ior = 1.333F;
            profile.absorptionR = 0.07F;
            profile.absorptionG = 0.025F;
            profile.absorptionB = 0.012F;
        } else if (tile.material == Material.GLASS || tile instanceof AC_BlockTransparent) {
            profile.addFlag(MaterialProfile.FLAG_TRANSMISSIVE);
            profile.transmission = 1.0F;
            profile.ior = 1.5F;
        } else if (tile.id == 79) {
            profile.addFlag(MaterialProfile.FLAG_TRANSMISSIVE);
            profile.transmission = 0.8F;
            profile.ior = 1.31F;
        }
        if (tile.material == Material.LEAVES || tile.material == Material.PLANT) {
            profile.addFlag(MaterialProfile.FLAG_THIN);
        }
        int shape = tile.getRenderShape();
        if (shape == BlockShapes.REEDS || shape == BlockShapes.CROP || shape == AC_BlockShapes.BLOCK_TREE) {
            profile.addFlag(MaterialProfile.FLAG_THIN);
        }
    }

    private void applyManifest(JsonObject root) {
        JsonElement defaultsElement = field(root, "defaults");
        if (defaultsElement != null && defaultsElement.isJsonObject()) {
            this.defaults.copyFrom(MaterialProfile.parse(defaultsElement.getAsJsonObject()));
        }

        JsonElement paletteElement = field(root, "palette");
        if (paletteElement != null && paletteElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : paletteElement.getAsJsonObject().entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                MaterialProfile parsed = MaterialProfile.parse(entry.getValue().getAsJsonObject());
                MaterialProfile existing = this.palette.get(entry.getKey());
                if (existing == null) {
                    this.palette.put(entry.getKey(), parsed);
                } else {
                    existing.copyFrom(parsed);
                }
            }
        }

        JsonElement texturesElement = field(root, "textures");
        if (texturesElement != null && texturesElement.isJsonObject()) {
            JsonElement atlases = field(texturesElement.getAsJsonObject(), "atlases");
            if (atlases != null && atlases.isJsonArray()) {
                for (JsonElement element : atlases.getAsJsonArray()) {
                    if (!element.isJsonObject()) continue;
                    JsonObject atlas = element.getAsJsonObject();
                    JsonElement idElement = field(atlas, "id");
                    if (idElement == null || !idElement.isJsonPrimitive()) continue;
                    int id = clampAtlas(idElement.getAsInt());
                    this.albedoPaths[id] = stringOrDefault(atlas, "albedo", this.albedoPaths[id]);
                    this.normalPaths[id] = stringOrDefault(atlas, "normal", this.normalPaths[id]);
                    this.specularPaths[id] = stringOrDefault(atlas, "specular", this.specularPaths[id]);
                }
            }
        }

        JsonElement blocksElement = field(root, "blocks");
        if (blocksElement != null && blocksElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : blocksElement.getAsJsonObject().entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                MaterialProfile parsed = MaterialProfile.parse(entry.getValue().getAsJsonObject());
                String key = entry.getKey().trim();
                int colon = key.indexOf(':');
                try {
                    if (colon < 0) {
                        int id = Integer.parseInt(key);
                        if (id < 0 || id >= BLOCK_COUNT) continue;
                        for (int meta = 0; meta < META_COUNT; meta++) {
                            mergeBlock(id, meta, parsed);
                        }
                    } else {
                        int id = Integer.parseInt(key.substring(0, colon));
                        int meta = Integer.parseInt(key.substring(colon + 1));
                        if (id < 0 || id >= BLOCK_COUNT || meta < 0 || meta >= META_COUNT) continue;
                        mergeBlock(id, meta, parsed);
                    }
                } catch (NumberFormatException error) {
                    ACMod.LOGGER.warn("Invalid material block key \"{}\" in map materials.", key);
                }
            }
        }

        JsonElement tilesElement = field(root, "tiles");
        if (tilesElement != null && tilesElement.isJsonObject()) {
            this.rawTiles = tilesElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : tilesElement.getAsJsonObject().entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                MaterialProfile parsed = MaterialProfile.parse(entry.getValue().getAsJsonObject());
                String key = entry.getKey().trim();
                int colon = key.indexOf(':');
                if (colon < 0) continue;
                try {
                    int atlas = Integer.parseInt(key.substring(0, colon));
                    int tile = Integer.parseInt(key.substring(colon + 1));
                    if (atlas < 0 || atlas >= ATLAS_COUNT || tile < 0 || tile >= TILE_COUNT) continue;
                    int index = atlas * TILE_COUNT + tile;
                    MaterialProfile existing = this.tiles[index];
                    if (existing == null) this.tiles[index] = parsed;
                    else existing.copyFrom(parsed);
                } catch (NumberFormatException error) {
                    ACMod.LOGGER.warn("Invalid material tile key \"{}\" in map materials.", key);
                }
            }
        }
        this.tileParameters = TileMaterialTable.fromManifest(root, this.defaults, this.palette);
    }

    private void mergeBlock(int id, int meta, MaterialProfile parsed) {
        int index = blockIndex(id, meta);
        MaterialProfile existing = this.blocks[index];
        if (existing == null) this.blocks[index] = new MaterialProfile(parsed);
        else existing.copyFrom(parsed);
    }

    private void resolveAll() {
        java.util.Arrays.fill(this.blockEmission, -1);
        java.util.Arrays.fill(this.blockFlags, 0);
        java.util.Arrays.fill(this.blockLightColor, -1);
        java.util.Arrays.fill(this.blockLightRadius, -1.0F);
        java.util.Arrays.fill(this.blockLightIntensity, -1.0F);
        java.util.Arrays.fill(this.blockTransmission, 0.0F);
        java.util.Arrays.fill(this.blockIor, 1.5F);
        java.util.Arrays.fill(this.blockAbsorption, 0.0F);
        java.util.Arrays.fill(this.blockGameplayLight, -1);
        java.util.Arrays.fill(this.mayEmit, false);
        java.util.Arrays.fill(this.transmissive, false);

        for (int index = 0; index < this.tiles.length; index++) {
            MaterialProfile profile = this.tiles[index];
            this.tileProfiles[index] = profile == null ? null : profile.resolve(this.defaults, this.palette);
        }

        for (int id = 0; id < BLOCK_COUNT; id++) {
            for (int meta = 0; meta < META_COUNT; meta++) {
                int index = blockIndex(id, meta);
                MaterialProfile profile = this.blocks[index];
                if (profile == null) continue;
                MaterialProfile resolved = profile.resolve(this.defaults, this.palette);
                if (resolved.emission != null) {
                    this.blockEmission[index] = Math.max(0, Math.min(15, resolved.emission));
                }
                if (resolved.flags != null) this.blockFlags[index] = resolved.flags;
                if (resolved.lightColor != null) this.blockLightColor[index] = resolved.lightColor;
                if (resolved.lightRadius != null) this.blockLightRadius[index] = resolved.lightRadius;
                if (resolved.lightIntensity != null) this.blockLightIntensity[index] = resolved.lightIntensity;
                if (resolved.transmission != null) {
                    this.blockTransmission[index] = Math.max(0.0F, Math.min(1.0F, resolved.transmission));
                }
                if (resolved.ior != null) this.blockIor[index] = Math.max(1.0F, resolved.ior);
                if (resolved.absorptionR != null) this.blockAbsorption[index * 3] = resolved.absorptionR;
                if (resolved.absorptionG != null) this.blockAbsorption[index * 3 + 1] = resolved.absorptionG;
                if (resolved.absorptionB != null) this.blockAbsorption[index * 3 + 2] = resolved.absorptionB;
                if (resolved.gameplayLight != null) {
                    this.blockGameplayLight[index] = Math.max(0, Math.min(15, resolved.gameplayLight));
                }
                if (this.blockEmission[index] > 0) this.mayEmit[id] = true;
                Tile tile = Tile.tiles[id];
                if (tile != null) {
                    int atlas = clampAtlas(textureNum(tile));
                    try {
                        int texture = tile.getTexture(0, meta);
                        if (texture >= 0 && texture < TILE_COUNT) {
                            int tileIndex = atlas * TILE_COUNT + texture;
                            if (this.tileEmission[tileIndex] > 0) this.mayEmit[id] = true;
                            MaterialProfile tileProfile = this.tileProfiles[tileIndex];
                            if (tileProfile != null) {
                                if (tileProfile.flags != null
                                    && (tileProfile.flags & (MaterialProfile.FLAG_TRANSMISSIVE | MaterialProfile.FLAG_WATER)) != 0) {
                                    this.transmissive[id] = true;
                                }
                                if (tileProfile.transmission != null && tileProfile.transmission > 0.0F) {
                                    this.transmissive[id] = true;
                                }
                                if (tileProfile.emission != null && tileProfile.emission > 0) {
                                    this.mayEmit[id] = true;
                                }
                            }
                        }
                    } catch (RuntimeException ignored) { }
                }
                if ((this.blockFlags[index] & (MaterialProfile.FLAG_TRANSMISSIVE | MaterialProfile.FLAG_WATER)) != 0
                    || this.blockTransmission[index] > 0.0F) {
                    this.transmissive[id] = true;
                }
            }
            if (Tile.tiles[id] != null && Tile.lightEmission[id] > 0.0F) this.mayEmit[id] = true;
            if (id == 171 || id == 161) {
                this.mayEmit[id] = true;
            }
        }

    }

    private JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("version", new org.spongepowered.include.com.google.gson.JsonPrimitive(1));
        root.add("generator", new org.spongepowered.include.com.google.gson.JsonPrimitive("AC-Legacy"));

        JsonObject textures = new JsonObject();
        JsonArray atlases = new JsonArray();
        for (int atlas = 0; atlas < ATLAS_COUNT; atlas++) {
            JsonObject entry = new JsonObject();
            entry.add("id", new org.spongepowered.include.com.google.gson.JsonPrimitive(atlas));
            entry.add("albedo", new org.spongepowered.include.com.google.gson.JsonPrimitive(this.albedoPaths[atlas]));
            entry.add("normal", new org.spongepowered.include.com.google.gson.JsonPrimitive(this.normalPaths[atlas]));
            entry.add("specular", new org.spongepowered.include.com.google.gson.JsonPrimitive(this.specularPaths[atlas]));
            atlases.add(entry);
        }
        textures.add("atlases", atlases);
        root.add("textures", textures);
        root.add("defaults", this.defaults.toJson());

        JsonObject palette = new JsonObject();
        for (Map.Entry<String, MaterialProfile> entry : this.palette.entrySet()) {
            palette.add(entry.getKey(), entry.getValue().toJson());
        }
        root.add("palette", palette);

        JsonObject blocks = new JsonObject();
        for (int id = 0; id < BLOCK_COUNT; id++) {
            MaterialProfile first = this.blocks[blockIndex(id, 0)];
            if (first == null) continue;
            boolean any = false;
            for (int meta = 0; meta < META_COUNT; meta++) {
                MaterialProfile profile = this.blocks[blockIndex(id, meta)];
                if (profile != null && !isDefaultStone(profile)) {
                    any = true;
                    break;
                }
            }
            if (!any) continue;
            boolean uniform = true;
            for (int meta = 1; meta < META_COUNT; meta++) {
                MaterialProfile profile = this.blocks[blockIndex(id, meta)];
                if (profile != null && !sameBehaviour(first, profile)) {
                    uniform = false;
                    break;
                }
            }
            if (uniform) {
                blocks.add(Integer.toString(id), first.toJson());
            } else {
                for (int meta = 0; meta < META_COUNT; meta++) {
                    MaterialProfile profile = this.blocks[blockIndex(id, meta)];
                    if (profile != null) blocks.add(id + ":" + meta, profile.toJson());
                }
            }
        }
        root.add("blocks", blocks);
        root.add("tiles", this.rawTiles == null ? new JsonObject() : this.rawTiles);
        return root;
    }

    private void ensureCompanions(boolean copyAlbedo) throws IOException {
        for (int atlas = 0; atlas < ATLAS_COUNT; atlas++) {
            ImageBuffer albedo = loadImage(this.albedoPaths[atlas]);
            int width = albedo == null ? 256 : albedo.getWidth();
            int height = albedo == null ? 256 : albedo.getHeight();
            if (copyAlbedo && albedo != null && atlas != 1) {
                File target = new File(this.directory, stripLeadingSlash(this.albedoPaths[atlas]));
                if (!target.isFile()) PngWriter.write(target, albedo);
            }
            writeMaterialAtlases(atlas, width, height,
                new File(this.directory, this.normalPaths[atlas]),
                new File(this.directory, this.specularPaths[atlas]));
        }
    }

    private void writeMaterialAtlases(int atlas, int width, int height, File normalFile, File specularFile)
        throws IOException {
        int tileWidth = Math.max(1, width / 16);
        int tileHeight = Math.max(1, height / 16);
        ImageBuffer normal = ImageBuffer.create(width, height, ImageFormat.RGBA_U8);
        ImageBuffer specular = ImageBuffer.create(width, height, ImageFormat.RGBA_U8);
        fill(normal, 128, 128, 255, 255);
        fill(specular, 0, 10, 0, 0);

        for (int tileY = 0; tileY < (height >= 16 ? 16 : 0); tileY++) {
            for (int tileX = 0; tileX < (width >= 16 ? 16 : 0); tileX++) {
                MaterialProfile profile = this.tileProfiles[atlas * TILE_COUNT + tileY * 16 + tileX];
                if (profile == null) continue;
                int smoothness = Math.round(clamp01(profile.smoothness == null ? 0.0F : profile.smoothness) * 255.0F);
                int f0 = profile.metallic != null && profile.metallic
                    ? 255
                    : Math.round(clamp01(profile.f0 == null ? 0.04F : profile.f0) * 255.0F);
                int blue;
                if (profile.sss != null && profile.sss > 0.0F) {
                    blue = Math.round(65.0F + clamp01(profile.sss) * 190.0F);
                } else {
                    blue = Math.round(clamp01(profile.porosity == null ? 0.0F : profile.porosity) * 64.0F);
                }
                int emission = profile.emission == null || profile.emission <= 0
                    ? 255
                    : Math.max(1, Math.min(254, Math.round(profile.emission / 15.0F * 254.0F)));
                fillTile(specular, tileX * tileWidth, tileY * tileHeight, tileWidth, tileHeight,
                    smoothness, f0, blue, emission);
            }
        }
        if (!normalFile.isFile()) PngWriter.write(normalFile, normal);
        if (!specularFile.isFile()) PngWriter.write(specularFile, specular);
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static void fill(ImageBuffer image, int r, int g, int b, int a) {
        ByteBuffer buffer = image.getBuffer();
        buffer.position(0);
        for (int i = 0; i < image.getWidth() * image.getHeight(); i++) {
            buffer.put((byte) r).put((byte) g).put((byte) b).put((byte) a);
        }
    }

    private static void fillTile(ImageBuffer image, int x0, int y0, int width, int height,
                                 int r, int g, int b, int a) {
        for (int y = 0; y < height; y++) {
            ByteBuffer row = image.getRowSlice(y0 + y);
            for (int x = 0; x < width; x++) {
                int offset = (x0 + x) * 4;
                row.put(offset, (byte) r).put(offset + 1, (byte) g)
                    .put(offset + 2, (byte) b).put(offset + 3, (byte) a);
            }
        }
    }

    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private void loadEmissionAtlases() {
        java.util.Arrays.fill(this.tileEmission, 0);
        for (int atlas = 0; atlas < ATLAS_COUNT; atlas++) {
            ImageBuffer image = loadImage(this.specularPaths[atlas]);
            if (image == null) continue;
            int width = image.getWidth();
            int height = image.getHeight();
            int tileWidth = width / 16;
            int tileHeight = height / 16;
            if (tileWidth <= 0 || tileHeight <= 0) continue;
            for (int tileY = 0; tileY < 16; tileY++) {
                for (int tileX = 0; tileX < 16; tileX++) {
                    int maxAlpha = 0;
                    for (int y = 0; y < tileHeight; y++) {
                        var row = image.getRowSlice(tileY * tileHeight + y);
                        for (int x = 0; x < tileWidth; x++) {
                            int alpha = row.get(tileX * tileWidth * 4 + x * 4 + 3) & 255;
                            if (alpha < 255 && alpha > maxAlpha) maxAlpha = alpha;
                        }
                    }
                    int emission = Math.round(maxAlpha / 254.0F * 15.0F);
                    this.tileEmission[atlas * TILE_COUNT + tileY * 16 + tileX] = emission;
                }
            }
        }
    }

    private ImageBuffer loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        String relative = path.startsWith("/") ? path.substring(1) : path;
        try {
            if (this.directory != null) {
                File file = new File(this.directory, relative);
                if (file.isFile()) {
                    return ImageLoader.load(file, ImageLoadOptions.withFormat(ImageFormat.RGBA_U8));
                }
            }
            if (Minecraft.instance != null && Minecraft.instance.textures != null) {
                try {
                    return ((ExTextureManager) Minecraft.instance.textures).getTextureImage(path);
                } catch (IOException | RuntimeException ignored) {
                    // Fall through to the built-in resource.
                }
            }
            try (InputStream stream = ACMod.class.getResourceAsStream("/assets/adventurecraft/" + relative)) {
                if (stream != null) {
                    return ImageLoader.load(stream, ImageLoadOptions.withFormat(ImageFormat.RGBA_U8));
                }
            }
        } catch (Exception error) {
            ACMod.LOGGER.warn("Cannot read material atlas \"{}\".", path, error);
        }
        return null;
    }

    private void writeNeutral(File file, int width, int height, boolean normal) throws IOException {
        if (file.isFile()) return;
        ImageBuffer image = ImageBuffer.create(width, height, ImageFormat.RGBA_U8);
        var buffer = image.getBuffer();
        for (int i = 0; i < width * height; i++) {
            if (normal) {
                buffer.put((byte) 128).put((byte) 128).put((byte) 255).put((byte) 255);
            } else {
                buffer.put((byte) 0).put((byte) 10).put((byte) 0).put((byte) 0);
            }
        }
        PngWriter.write(file, image);
    }

    private static boolean isDefaultStone(MaterialProfile profile) {
        return "stone".equals(profile.material)
            && profile.emission == null
            && profile.flags == null
            && profile.lightColor == null
            && profile.lightRadius == null
            && profile.lightIntensity == null
            && profile.transmission == null
            && profile.ior == null
            && profile.absorptionR == null
            && profile.absorptionG == null
            && profile.absorptionB == null
            && profile.smoothness == null
            && profile.f0 == null
            && profile.metallic == null
            && profile.porosity == null
            && profile.sss == null
            && profile.parallax == null
            && profile.gameplayLight == null;
    }

    private static boolean sameBehaviour(MaterialProfile a, MaterialProfile b) {
        return java.util.Objects.equals(a.material, b.material)
            && java.util.Objects.equals(a.emission, b.emission)
            && java.util.Objects.equals(a.flags, b.flags)
            && java.util.Objects.equals(a.lightColor, b.lightColor)
            && java.util.Objects.equals(a.lightRadius, b.lightRadius)
            && java.util.Objects.equals(a.lightIntensity, b.lightIntensity)
            && java.util.Objects.equals(a.transmission, b.transmission)
            && java.util.Objects.equals(a.ior, b.ior)
            && java.util.Objects.equals(a.absorptionR, b.absorptionR)
            && java.util.Objects.equals(a.absorptionG, b.absorptionG)
            && java.util.Objects.equals(a.absorptionB, b.absorptionB)
            && java.util.Objects.equals(a.smoothness, b.smoothness)
            && java.util.Objects.equals(a.f0, b.f0)
            && java.util.Objects.equals(a.metallic, b.metallic)
            && java.util.Objects.equals(a.porosity, b.porosity)
            && java.util.Objects.equals(a.sss, b.sss)
            && java.util.Objects.equals(a.parallax, b.parallax)
            && java.util.Objects.equals(a.gameplayLight, b.gameplayLight);
    }

    private static JsonElement field(JsonObject object, String key) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (entry.getKey().equals(key)) return entry.getValue();
        }
        return null;
    }

    private static String stringOrDefault(JsonObject object, String key, String fallback) {
        JsonElement element = field(object, key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : fallback;
    }

    private static MaterialProfile defaultProfile() {
        MaterialProfile profile = new MaterialProfile();
        profile.material = "stone";
        profile.smoothness = 0.0F;
        profile.f0 = 0.04F;
        profile.porosity = 0.3F;
        profile.parallax = true;
        return profile;
    }

    private static Map<String, MaterialProfile> builtinPalette() {
        Map<String, MaterialProfile> palette = new LinkedHashMap<>();
        palette.put("stone", profile(0.05F, 0.04F, false, 0.30F, false));
        palette.put("granular", profile(0.0F, 0.04F, false, 0.55F, false));
        palette.put("wood", profile(0.04F, 0.04F, false, 0.12F, false));
        palette.put("metal", profile(0.65F, 0.90F, true, 0.0F, false));
        palette.put("cloth", profile(0.0F, 0.04F, false, 0.40F, false));
        palette.put("foliage", profile(0.10F, 0.04F, false, 0.0F, true));
        palette.put("glass", profile(0.95F, 0.04F, false, 0.0F, false));
        palette.put("ice", profile(0.85F, 0.04F, false, 0.0F, false));
        palette.put("water", profile(0.98F, 0.02F, false, 0.0F, false));
        palette.put("torch", profile(0.0F, 0.04F, false, 0.0F, false));
        palette.put("lamp", profile(0.0F, 0.04F, false, 0.0F, false));
        palette.put("lava", profile(0.0F, 0.04F, false, 0.0F, false));

        MaterialProfile glass = palette.get("glass");
        glass.transmission = 1.0F;
        glass.ior = 1.5F;
        glass.addFlag(MaterialProfile.FLAG_TRANSMISSIVE);
        MaterialProfile ice = palette.get("ice");
        ice.transmission = 0.8F;
        ice.ior = 1.31F;
        ice.addFlag(MaterialProfile.FLAG_TRANSMISSIVE);
        MaterialProfile water = palette.get("water");
        water.transmission = 1.0F;
        water.ior = 1.333F;
        water.absorptionR = 0.07F;
        water.absorptionG = 0.025F;
        water.absorptionB = 0.012F;
        water.addFlag(MaterialProfile.FLAG_WATER | MaterialProfile.FLAG_TRANSMISSIVE);
        MaterialProfile foliage = palette.get("foliage");
        foliage.sss = 0.5F;
        foliage.addFlag(MaterialProfile.FLAG_THIN);
        MaterialProfile torch = palette.get("torch");
        torch.emission = 15;
        torch.lightColor = 0xFFD194;
        torch.lightRadius = 16.0F;
        torch.lightIntensity = 1.0F;
        MaterialProfile lamp = palette.get("lamp");
        lamp.lightColor = 0xFFD194;
        lamp.lightRadius = 16.0F;
        lamp.lightIntensity = 1.0F;
        MaterialProfile lava = palette.get("lava");
        lava.emission = 15;
        lava.lightColor = 0xFFB060;
        lava.lightRadius = 18.0F;
        lava.lightIntensity = 1.2F;
        return palette;
    }

    private static MaterialProfile profile(float smoothness, float f0, boolean metallic, float porosity, boolean sss) {
        MaterialProfile profile = new MaterialProfile();
        profile.smoothness = smoothness;
        profile.f0 = f0;
        profile.metallic = metallic;
        profile.porosity = porosity;
        if (sss) profile.sss = 0.5F;
        return profile;
    }
}
