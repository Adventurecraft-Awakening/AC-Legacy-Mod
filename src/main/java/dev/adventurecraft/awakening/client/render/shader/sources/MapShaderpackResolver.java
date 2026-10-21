package dev.adventurecraft.awakening.client.render.shader.sources;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** Discovers only the selected map's shaders; never reads game-wide shader settings. */
public final class MapShaderpackResolver {

    public static final String DIRECTORY = "shaders";
    public static final String CONFIG_FILE = "shaders.properties";
    public static final String PACK_PROPERTY = "shaderPack";

    private MapShaderpackResolver() {
    }

    /**
     * Opens a fresh source owned by the caller. Missing shaders and explicit OFF return
     * the null source. Invalid explicit selections throw, without choosing another pack.
     */
    public static ShaderpackSource open(File mapDirectory) throws IOException {
        if (mapDirectory == null) {
            return NullShaderpackSource.instance;
        }
        Path mapRoot = mapDirectory.toPath().toRealPath();
        Path shaderDirectory = mapRoot.resolve(DIRECTORY);
        if (!Files.exists(shaderDirectory, LinkOption.NOFOLLOW_LINKS)) {
            return NullShaderpackSource.instance;
        }
        Path shaderRoot = requireContained(mapRoot, shaderDirectory);
        if (!Files.isDirectory(shaderRoot)) {
            throw new IOException("Map shaders path is not a directory: " + shaderRoot);
        }

        Properties config = new Properties();
        Path configFile = shaderRoot.resolve(CONFIG_FILE);
        if (Files.exists(configFile, LinkOption.NOFOLLOW_LINKS)) {
            try (Reader reader = Files.newBufferedReader(requireContained(shaderRoot, configFile), StandardCharsets.UTF_8)) {
                config.load(reader);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid map shader configuration: " + configFile, ex);
            }
        }
        String selected = config.getProperty(PACK_PROPERTY, "").trim();
        if (selected.equalsIgnoreCase("OFF")) {
            return NullShaderpackSource.instance;
        }
        if (selected.equals(".")) {
            return openDirectory(shaderRoot);
        }
        if (!selected.isEmpty()) {
            // Pack selection is a child name, never a path or an external location.
            if (selected.equals("..") || selected.indexOf('/') >= 0 || selected.indexOf('\\') >= 0 || selected.indexOf(':') >= 0
                || ShaderpackFileSource.normalizePath(selected) == null) {
                throw new IOException("Map shaderPack must name a directory or zip inside shaders/: " + selected);
            }
            return openPack(requireContained(shaderRoot, shaderRoot.resolve(selected)));
        }

        if (hasShaderPrograms(shaderRoot)) {
            return new ShaderpackDirSource(shaderRoot.toFile());
        }

        List<Path> candidates;
        try (var files = Files.list(shaderRoot)) {
            candidates = files.filter(path -> Files.isDirectory(path) || isZip(path))
                .sorted(Comparator.comparing((Path path) -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(path -> path.getFileName().toString()))
                .toList();
        }
        IOException invalidPack = null;
        for (Path candidate : candidates) {
            try {
                return openPack(requireContained(shaderRoot, candidate));
            } catch (IOException ex) {
                invalidPack = ex;
            }
        }
        if (invalidPack != null) {
            throw new IOException("No usable shader pack in " + shaderRoot, invalidPack);
        }
        return NullShaderpackSource.instance;
    }

    private static Path requireContained(Path root, Path file) throws IOException {
        Path real = file.toRealPath();
        if (!real.startsWith(root)) {
            throw new IOException("Shader path escapes the map shader directory: " + file);
        }
        return real;
    }

    private static ShaderpackFileSource openPack(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Path nested = path.resolve(DIRECTORY);
            return openDirectory(Files.isDirectory(nested) ? requireContained(path, nested) : path);
        }
        if (isZip(path)) {
            ShaderpackZipSource source = new ShaderpackZipSource(path.toFile());
            if (source.hasShaderPrograms()) {
                return source;
            }
            source.close();
        }
        throw new IOException("Shader pack has no .vsh or .fsh programs: " + path);
    }

    private static ShaderpackDirSource openDirectory(Path path) throws IOException {
        if (!hasShaderPrograms(path)) {
            throw new IOException("Shader directory has no .vsh or .fsh programs: " + path);
        }
        return new ShaderpackDirSource(path.toFile());
    }

    private static boolean isZip(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    private static boolean hasShaderPrograms(Path root) throws IOException {
        try (var files = Files.list(root)) {
            if (files.anyMatch(path -> Files.isRegularFile(path) && isShaderProgram(path.getFileName().toString()))) {
                return true;
            }
        }
        Path world = root.resolve("world0");
        if (!Files.isDirectory(world) || !world.toRealPath().startsWith(root)) {
            return false;
        }
        try (var files = Files.list(world)) {
            return files.anyMatch(path -> Files.isRegularFile(path) && isShaderProgram("world0/" + path.getFileName()));
        }
    }

    static boolean isShaderProgram(String path) {
        String name = path.startsWith("world0/") ? path.substring("world0/".length()) : path;
        return name.indexOf('/') < 0 && (name.endsWith(".vsh") || name.endsWith(".fsh"));
    }
}
