package dev.adventurecraft.awakening.client.render.material;

import org.spongepowered.include.com.google.gson.JsonElement;
import org.spongepowered.include.com.google.gson.JsonObject;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, sparse GPU overrides for the four terrain atlases. A zero mask leaves
 * every painted companion channel unchanged. Only explicit manifest tile entries
 * opt in: scalar entries override their supplied fields, while a material selector
 * expands the selected defaults/palette profile before applying the tile's fields.
 * A disabled parallax default applies globally without overriding painted channels.
 */
public final class TileMaterialTable {
    public static final int ATLAS_COUNT = 4;
    public static final int TILES_PER_ATLAS = 256;
    public static final int ENTRY_COUNT = ATLAS_COUNT * TILES_PER_ATLAS;
    public static final int STRIDE = 64;
    public static final int BYTES = ENTRY_COUNT * STRIDE;
    public static final int APPEARANCE_OFFSET = 0;
    public static final int OPTICS_OFFSET = 16;
    public static final int ABSORPTION_OFFSET = 32;
    public static final int CONTROL_OFFSET = 48;

    public static final int MASK_SMOOTHNESS = 1;
    public static final int MASK_F0 = 2;
    public static final int MASK_METALLIC = 4;
    public static final int MASK_POROSITY = 8;
    public static final int MASK_SSS = 16;
    public static final int MASK_EMISSION = 32;
    public static final int MASK_TRANSMISSION = 64;
    public static final int MASK_IOR = 128;
    public static final int MASK_ABSORPTION_R = 256;
    public static final int MASK_ABSORPTION_G = 512;
    public static final int MASK_ABSORPTION_B = 1024;
    public static final int MASK_PARALLAX = 2048;

    private static final int WORDS_PER_ENTRY = STRIDE / Integer.BYTES;
    private static final int MASK_WORD = CONTROL_OFFSET / Integer.BYTES;
    private static final TileMaterialTable EMPTY = new TileMaterialTable(new int[BYTES / Integer.BYTES], false);
    private final int[] words;
    private final boolean overrides;

    private TileMaterialTable(int[] words, boolean overrides) {
        // All callers transfer a fresh private array; neither profiles nor JSON are retained.
        this.words = words;
        this.overrides = overrides;
    }

    public static TileMaterialTable empty() { return EMPTY; }

    public boolean isEmpty() { return !overrides; }

    /**
     * Builds a snapshot without loading a game registry. Defaults and palette are
     * the already merged CPU profiles. They supply values only when a tile has an
     * explicit material selector. Parallax eligibility additionally inherits the
     * manifest default, so a map can disable that client effect across all tiles.
     */
    public static TileMaterialTable fromManifest(
        JsonObject manifest, MaterialProfile defaults, Map<String, MaterialProfile> palette) {
        JsonObject entries = object(manifest, "tiles");
        MaterialProfile[] tiles = new MaterialProfile[ENTRY_COUNT];
        if (entries != null) {
            for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                int index = tileIndex(entry.getKey());
                if (index < 0) continue;
                MaterialProfile parsed = parse(entry.getValue().getAsJsonObject());
                if (tiles[index] == null) tiles[index] = parsed;
                else tiles[index].copyFrom(parsed);
            }
        }
        int[] words = new int[BYTES / Integer.BYTES];
        boolean any = false;
        Boolean defaultParallax = defaults == null ? null : defaults.parallax;
        for (int index = 0; index < tiles.length; index++) {
            MaterialProfile tile = tiles[index];
            // Parallax is an opt-in client effect over painted height. Unlike
            // scalar channels, a disabled manifest default applies to every tile.
            if (tile == null) {
                if (Boolean.FALSE.equals(defaultParallax)) {
                    words[index * WORDS_PER_ENTRY + MASK_WORD] = MASK_PARALLAX;
                    any = true;
                }
                continue;
            }
            MaterialProfile resolved = tile.material == null ? tile : tile.resolve(defaults, palette);
            int base = index * WORDS_PER_ENTRY;
            int mask = 0;
            mask |= scalar(words, base, 0, resolved.smoothness, 0, 1, MASK_SMOOTHNESS);
            mask |= scalar(words, base, 1, resolved.f0, 0, 1, MASK_F0);
            if (resolved.metallic != null) {
                words[base + 2] = Float.floatToIntBits(resolved.metallic ? 1 : 0);
                mask |= MASK_METALLIC;
            }
            mask |= scalar(words, base, 3, resolved.porosity, 0, 1, MASK_POROSITY);
            mask |= scalar(words, base, 4, resolved.sss, 0, 1, MASK_SSS);
            if (resolved.emission != null) {
                words[base + 5] = Float.floatToIntBits(Math.clamp(resolved.emission, 0, 15) / 15f);
                mask |= MASK_EMISSION;
            }
            mask |= scalar(words, base, 6, resolved.transmission, 0, 1, MASK_TRANSMISSION);
            mask |= scalar(words, base, 7, resolved.ior, 1, 4, MASK_IOR);
            mask |= scalar(words, base, 8, resolved.absorptionR, 0, 64, MASK_ABSORPTION_R);
            mask |= scalar(words, base, 9, resolved.absorptionG, 0, 64, MASK_ABSORPTION_G);
            mask |= scalar(words, base, 10, resolved.absorptionB, 0, 64, MASK_ABSORPTION_B);
            Boolean parallax = resolved.parallax == null ? defaultParallax : resolved.parallax;
            if (parallax != null) {
                words[base + 11] = Float.floatToIntBits(parallax ? 1 : 0);
                mask |= MASK_PARALLAX;
            }
            words[base + MASK_WORD] = mask;
            any |= mask != 0;
        }
        return any ? new TileMaterialTable(words, true) : EMPTY;
    }

    /**
     * Writes the std430 ABI using native byte order and advances position by BYTES.
     * Preflight failures leave the buffer unchanged; its limit and byte order are preserved.
     */
    public void writeTo(ByteBuffer target) {
        Objects.requireNonNull(target, "target");
        if (target.isReadOnly()) throw new ReadOnlyBufferException();
        if (target.remaining() < BYTES) throw new BufferOverflowException();
        ByteBuffer output = target.duplicate().order(ByteOrder.nativeOrder());
        output.asIntBuffer().put(words);
        target.position(target.position() + BYTES);
    }

    private static int scalar(int[] words, int base, int word, Float value, float min, float max, int mask) {
        if (value == null || !Float.isFinite(value)) return 0;
        words[base + word] = Float.floatToIntBits(Math.clamp(value, min, max));
        return mask;
    }

    private static int tileIndex(String key) {
        String text = key.trim();
        int colon = text.indexOf(':');
        if (colon < 0) return -1;
        try {
            int atlas = Integer.parseInt(text.substring(0, colon));
            int tile = Integer.parseInt(text.substring(colon + 1));
            return atlas >= 0 && atlas < ATLAS_COUNT && tile >= 0 && tile < TILES_PER_ATLAS
                ? atlas * TILES_PER_ATLAS + tile : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    /** Parse only the GPU fields; unrelated behavior cannot introduce nonfinite GPU values. */
    private static MaterialProfile parse(JsonObject object) {
        MaterialProfile result = new MaterialProfile();
        JsonElement material = field(object, "material");
        if (material != null && material.isJsonPrimitive()) result.material = material.getAsString();
        result.smoothness = number(field(object, "smoothness"));
        result.f0 = number(field(object, "f0"));
        JsonElement metallic = field(object, "metallic");
        if (metallic != null && metallic.isJsonPrimitive()) result.metallic = metallic.getAsBoolean();
        JsonElement parallax = field(object, "parallax");
        if (parallax != null && parallax.isJsonPrimitive()) result.parallax = parallax.getAsBoolean();
        result.porosity = number(field(object, "porosity"));
        result.sss = number(field(object, "sss"));
        Float emission = number(field(object, "emission"));
        if (emission != null) result.emission = (int) Math.clamp(emission, 0, 15);
        result.transmission = number(field(object, "transmission"));
        result.ior = number(field(object, "ior"));
        JsonElement absorption = field(object, "absorption");
        if (absorption != null && absorption.isJsonArray()) {
            int index = 0;
            for (JsonElement component : absorption.getAsJsonArray()) {
                if (index == 0) result.absorptionR = number(component);
                else if (index == 1) result.absorptionG = number(component);
                else if (index == 2) result.absorptionB = number(component);
                if (++index >= 3) break;
            }
        }
        return result;
    }

    private static Float number(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) return null;
        try {
            float value = (float) element.getAsDouble();
            return Float.isFinite(value) ? value : null;
        } catch (NumberFormatException | UnsupportedOperationException ignored) {
            return null;
        }
    }

    private static JsonObject object(JsonObject object, String name) {
        JsonElement element = field(object, name);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonElement field(JsonObject object, String name) {
        if (object == null) return null;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (name.equals(entry.getKey())) return entry.getValue();
        }
        return null;
    }
}
