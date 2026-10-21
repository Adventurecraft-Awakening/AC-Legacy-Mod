package dev.adventurecraft.awakening.client.render.shader.sources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ShaderpackDirSource extends ShaderpackFileSource {

    private final Path contentRoot;
    private boolean closed;

    /** The caller supplies the actual shader content root, without a pack wrapper. */
    public ShaderpackDirSource(File shaderDirectory) throws IOException {
        super(shaderDirectory);
        this.contentRoot = shaderDirectory.toPath().toRealPath();
        if (!Files.isDirectory(this.contentRoot)) {
            throw new IOException("Shader source is not a directory: " + shaderDirectory);
        }
    }

    private Path resolve(String path) throws IOException {
        String normalized = normalizePath(path);
        if (this.closed || normalized == null) {
            return null;
        }
        Path file = this.contentRoot.resolve(normalized).toRealPath();
        return file.startsWith(this.contentRoot) ? file : null;
    }

    @Override
    public boolean isDirectory(String path) {
        try {
            Path file = this.resolve(path);
            return file != null && Files.isDirectory(file);
        } catch (IOException | SecurityException ex) {
            return false;
        }
    }

    @Override
    public InputStream getInputStream(String path) {
        try {
            Path file = this.resolve(path);
            return file != null && Files.isRegularFile(file) ? Files.newInputStream(file) : null;
        } catch (IOException | SecurityException ex) {
            return null;
        }
    }

    @Override
    public String getType() {
        return "dir";
    }

    @Override
    public void close() {
        this.closed = true;
    }
}
