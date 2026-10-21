package dev.adventurecraft.awakening.client.render.shader.sources;

import java.io.File;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

/** Paths are always relative to the shader content root, including in zipped packs. */
public abstract class ShaderpackFileSource extends ShaderpackSource {

    protected final File rootPath;

    protected ShaderpackFileSource(File rootPath) {
        this.rootPath = Objects.requireNonNull(rootPath);
    }

    public abstract boolean isDirectory(String path);

    /** Returns null for missing files, escaped paths, or a source that has been closed. */
    public abstract InputStream getInputStream(String path);

    @Override
    public String getName() {
        return getType() + TypeSeparator + this.rootPath.getName();
    }

    protected static String normalizePath(String path) {
        if (path == null || path.indexOf('\\') >= 0 || path.indexOf(':') >= 0) {
            return null;
        }
        try {
            Path relative = Path.of(path);
            if (relative.isAbsolute()) {
                return null;
            }
            relative = relative.normalize();
            if (relative.startsWith("..")) {
                return null;
            }
            return relative.toString().replace(File.separatorChar, '/');
        } catch (InvalidPathException ex) {
            return null;
        }
    }
}
