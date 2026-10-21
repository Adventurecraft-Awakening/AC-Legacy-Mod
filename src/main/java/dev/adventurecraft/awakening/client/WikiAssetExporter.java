package dev.adventurecraft.awakening.client;

import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlConstants;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.minecraft.client.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/**
 * Exports the authoritative AdventureCraft inventory renders consumed by the
 * generated GitHub wiki. The exporter is intentionally client-side: it uses
 * the same texture manager and item/block renderer as the running game.
 *
 * <p>Enable it with {@code -Dac.wiki.export=true} and select the destination
 * with {@code -Dac.wiki.exportDir=/path/to/output}. It runs after client
 * initialization, never creates a level, and stops the game once the files
 * have been written.</p>
 */
public final class WikiAssetExporter {

    public static final String ENABLE_PROPERTY = "ac.wiki.export";
    public static final String OUTPUT_PROPERTY = "ac.wiki.exportDir";
    public static final int IMAGE_SIZE = 128;

    private WikiAssetExporter() {
    }

    public static boolean isEnabled() {
        return Boolean.getBoolean(ENABLE_PROPERTY);
    }

    public static void export(Minecraft minecraft, Path outputRoot) throws IOException {
        Path root = outputRoot.toAbsolutePath().normalize();
        Path blockDir = root.resolve("blocks");
        Path itemDir = root.resolve("items");
        prepareDirectory(blockDir);
        prepareDirectory(itemDir);

        List<RegistryEntry<Tile>> blocks = registryEntries(AC_Blocks.class, Tile.class);
        List<RegistryEntry<Item>> items = registryEntries(AC_Items.class, Item.class);
        var renderer = new ItemRenderer();

        beginRendering();
        try {
            List<ManifestEntry> blockManifest = new ArrayList<>(blocks.size());
            for (RegistryEntry<Tile> entry : blocks) {
                Path file = blockDir.resolve(entry.field() + ".png");
                render(renderer, minecraft, new ItemInstance(entry.value(), 1, 0), file);
                blockManifest.add(new ManifestEntry(
                    entry.field(),
                    entry.value().id,
                    "blocks/" + entry.field() + ".png",
                    sha256(file),
                    "3D inventory render of " + humanize(entry.field())
                ));
            }

            List<ManifestEntry> itemManifest = new ArrayList<>(items.size());
            for (RegistryEntry<Item> entry : items) {
                Path file = itemDir.resolve(entry.field() + ".png");
                render(renderer, minecraft, new ItemInstance(entry.value(), 1, 0), file);
                itemManifest.add(new ManifestEntry(
                    entry.field(),
                    entry.value().id,
                    "items/" + entry.field() + ".png",
                    sha256(file),
                    "Inventory icon of " + humanize(entry.field())
                ));
            }

            writeManifest(root.resolve("render-manifest.json"), blockManifest, itemManifest);
            ACMod.LOGGER.info(
                "Exported {} block renders and {} item icons to {}",
                blockManifest.size(),
                itemManifest.size(),
                root
            );
        }
        finally {
            endRendering();
        }
    }

    private static void prepareDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.png")) {
            for (Path file : stream) {
                Files.delete(file);
            }
        }
    }

    private static void beginRendering() {
        LegacyGlAdapter.pushAttrib(LegacyGlConstants.ALL_ATTRIB_BITS);
        LegacyGlAdapter.viewport(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        LegacyGlAdapter.matrixMode(LegacyGlConstants.PROJECTION);
        LegacyGlAdapter.pushMatrix();
        LegacyGlAdapter.loadIdentity();
        LegacyGlAdapter.ortho(0.0, 16.0, 16.0, 0.0, -100.0, 100.0);

        LegacyGlAdapter.matrixMode(LegacyGlConstants.MODELVIEW);
        LegacyGlAdapter.pushMatrix();
        LegacyGlAdapter.loadIdentity();

        LegacyGlAdapter.enable(LegacyGlConstants.TEXTURE_2D);
        LegacyGlAdapter.enable(LegacyGlConstants.DEPTH_TEST);
        LegacyGlAdapter.depthFunc(LegacyGlConstants.LEQUAL);
        LegacyGlAdapter.enable(LegacyGlConstants.ALPHA_TEST);
        LegacyGlAdapter.alphaFunc(LegacyGlConstants.GREATER, 1.0F / 255.0F);
        LegacyGlAdapter.enable(LegacyGlConstants.BLEND);
        LegacyGlAdapter.blendFunc(LegacyGlConstants.SRC_ALPHA, LegacyGlConstants.ONE_MINUS_SRC_ALPHA);
        LegacyGlAdapter.enable(LegacyGlConstants.RESCALE_NORMAL);

        LegacyGlAdapter.pushMatrix();
        LegacyGlAdapter.rotatef(120.0F, 1.0F, 0.0F, 0.0F);
        Lighting.turnOn();
        LegacyGlAdapter.popMatrix();
    }

    private static void endRendering() {
        Lighting.turnOff();
        LegacyGlAdapter.matrixMode(LegacyGlConstants.MODELVIEW);
        LegacyGlAdapter.popMatrix();
        LegacyGlAdapter.matrixMode(LegacyGlConstants.PROJECTION);
        LegacyGlAdapter.popMatrix();
        LegacyGlAdapter.matrixMode(LegacyGlConstants.MODELVIEW);
        LegacyGlAdapter.popAttrib();
    }

    private static void render(ItemRenderer renderer, Minecraft minecraft, ItemInstance stack, Path output)
        throws IOException {
        LegacyGlAdapter.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        LegacyGlAdapter.clearDepth(1.0);
        LegacyGlAdapter.clear(LegacyGlConstants.COLOR_BUFFER_BIT | LegacyGlConstants.DEPTH_BUFFER_BIT);
        LegacyGlAdapter.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        LegacyGlAdapter.disable(LegacyGlConstants.CULL_FACE);
        renderer.renderAndDecorateItem(minecraft.font, minecraft.textures, stack, 0, 0);
        LegacyGlAdapter.finish();

        ByteBuffer pixels = BufferUtils.createByteBuffer(IMAGE_SIZE * IMAGE_SIZE * 4);
        LegacyGlAdapter.pixelStorei(LegacyGlConstants.PACK_ALIGNMENT, 1);
        LegacyGlAdapter.readPixels(0, 0, IMAGE_SIZE, IMAGE_SIZE, LegacyGlConstants.RGBA, LegacyGlConstants.UNSIGNED_BYTE, pixels);

        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < IMAGE_SIZE; y++) {
            int sourceY = IMAGE_SIZE - y - 1;
            for (int x = 0; x < IMAGE_SIZE; x++) {
                int offset = (sourceY * IMAGE_SIZE + x) * 4;
                int red = pixels.get(offset) & 0xff;
                int green = pixels.get(offset + 1) & 0xff;
                int blue = pixels.get(offset + 2) & 0xff;
                int alpha = pixels.get(offset + 3) & 0xff;
                image.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
            }
        }

        if (!ImageIO.write(image, "png", output.toFile())) {
            throw new IOException("No PNG writer is available for " + output);
        }
    }

    private static <T> List<RegistryEntry<T>> registryEntries(Class<?> owner, Class<T> type) {
        List<RegistryEntry<T>> result = new ArrayList<>();
        for (Field field : owner.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !type.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                result.add(new RegistryEntry<>(field.getName(), type.cast(field.get(null))));
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot read registry field " + owner.getName() + "." + field.getName(), e);
            }
        }
        result.sort(Comparator.comparing(RegistryEntry::field));
        return result;
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, count);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void writeManifest(
        Path file,
        List<ManifestEntry> blocks,
        List<ManifestEntry> items
    ) throws IOException {
        StringBuilder json = new StringBuilder(32_768);
        json.append("{\n");
        json.append("  \"format\": 1,\n");
        json.append("  \"width\": ").append(IMAGE_SIZE).append(",\n");
        json.append("  \"height\": ").append(IMAGE_SIZE).append(",\n");
        json.append("  \"contains_community_maps\": false,\n");
        appendEntries(json, "blocks", blocks);
        json.append(",\n");
        appendEntries(json, "items", items);
        json.append("\n}\n");
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    private static void appendEntries(StringBuilder json, String name, List<ManifestEntry> entries) {
        json.append("  \"").append(name).append("\": [\n");
        for (int i = 0; i < entries.size(); i++) {
            ManifestEntry entry = entries.get(i);
            json.append("    {\"field\": \"").append(jsonEscape(entry.field()))
                .append("\", \"id\": ").append(entry.id())
                .append(", \"file\": \"").append(jsonEscape(entry.file()))
                .append("\", \"sha256\": \"").append(entry.sha256())
                .append("\", \"alt\": \"").append(jsonEscape(entry.alt())).append("\"}");
            if (i + 1 < entries.size()) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]");
    }

    private static String jsonEscape(String value) {
        StringBuilder result = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20) {
                        result.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        result.append(c);
                    }
                }
            }
        }
        return result.toString();
    }

    private static String humanize(String field) {
        StringBuilder result = new StringBuilder(field.length() + 8);
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                result.append(' ');
            }
            result.append(i == 0 ? Character.toUpperCase(c) : c);
        }
        return result.toString();
    }

    private record RegistryEntry<T>(String field, T value) {
    }

    private record ManifestEntry(String field, int id, String file, String sha256, String alt) {
    }

}
