package dev.adventurecraft.awakening.client.render.shader.sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/** Expands shader includes without accessing OpenGL or a global shader directory. */
public final class ShaderSourceReader {

    private static final int MAX_INCLUDE_DEPTH = 64;
    private static final int MAX_FILES = 4096;
    private static final int MAX_OUTPUT_CHARACTERS = 8 * 1024 * 1024;
    private static final Pattern INCLUDE_DIRECTIVE = Pattern.compile("^#\\s*include\\b.*");
    private static final Pattern INCLUDE = Pattern.compile("^#\\s*include\\s*\"([^\"]+)\"\\s*(?://.*|/\\*.*\\*/\\s*)?$");

    private ShaderSourceReader() {
    }

    /** Missing optional top-level programs return false; invalid includes fail the program. */
    public static boolean read(
        ShaderpackFileSource source,
        String shadersDir,
        StringBuilder output,
        String filename,
        Consumer<String> onLine) throws IOException {

        String root = ShaderpackFileSource.normalizePath(shadersDir);
        if (root == null) {
            throw new IOException("Invalid shader root: " + shadersDir);
        }
        return new Expansion(source, root, output, onLine).read(filename, 0);
    }

    private static final class Expansion {

        private final ShaderpackFileSource source;
        private final String rootPrefix;
        private final StringBuilder output;
        private final Consumer<String> onLine;
        private final Set<String> stack = new HashSet<>();
        private int filesRead;

        private Expansion(ShaderpackFileSource source, String root, StringBuilder output, Consumer<String> onLine) {
            this.source = source;
            this.rootPrefix = root.isEmpty() ? "" : root + '/';
            this.output = output;
            this.onLine = onLine;
        }

        private boolean read(String filename, int depth) throws IOException {
            String normalized = ShaderpackFileSource.normalizePath(filename);
            if (normalized == null || !normalized.startsWith(this.rootPrefix)) {
                throw new IOException("Shader include escapes its shader root: " + filename);
            }
            if (depth >= MAX_INCLUDE_DEPTH) {
                throw new IOException("Shader include depth exceeds " + MAX_INCLUDE_DEPTH + " at " + normalized);
            }
            if (!this.stack.add(normalized)) {
                throw new IOException("Cyclic shader include: " + normalized);
            }
            try {
                InputStream stream = this.source.getInputStream(normalized);
                if (stream == null) {
                    return false;
                }
                try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    if (++this.filesRead > MAX_FILES) {
                        throw new IOException("Shader expansion exceeds " + MAX_FILES + " files at " + normalized);
                    }
                    int lineIndex = 0;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lineIndex++;
                        this.onLine.accept(line);
                        String stripped = line.stripLeading();
                        if (INCLUDE_DIRECTIVE.matcher(stripped).matches()) {
                            var include = INCLUDE.matcher(stripped);
                            if (!include.matches()) {
                                throw new IOException("Malformed shader include in " + normalized + ':' + lineIndex + ": " + line);
                            }
                            String includeName = include.group(1);
                            int slash = normalized.lastIndexOf('/');
                            String includePath = includeName.startsWith("/")
                                ? this.rootPrefix + includeName.substring(1)
                                : normalized.substring(0, slash + 1) + includeName;
                            if (!this.read(includePath, depth + 1)) {
                                throw new IOException("Missing shader include \"" + includeName + "\" in " + normalized + ':' + lineIndex);
                            }
                            continue;
                        }
                        if ((long) this.output.length() + line.length() + 1 > MAX_OUTPUT_CHARACTERS) {
                            throw new IOException("Expanded shader exceeds " + MAX_OUTPUT_CHARACTERS + " characters at " + normalized);
                        }
                        this.output.append(line).append('\n');
                    }
                }
                return true;
            } finally {
                this.stack.remove(normalized);
            }
        }
    }
}
