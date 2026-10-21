package dev.adventurecraft.awakening.client.render.shader.sources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ShaderpackZipSource extends ShaderpackFileSource {

    private final ZipFile zipFile;
    private final String contentPrefix;
    private boolean closed;

    public ShaderpackZipSource(File file) throws IOException {
        super(file);
        this.zipFile = new ZipFile(file);
        // Zip tools need not emit directory entries. Infer them from file prefixes.
        this.contentPrefix = this.hasPrefix("shaders/") ? "shaders/" : "";
    }

    private boolean hasPrefix(String prefix) {
        var entries = this.zipFile.entries();
        while (entries.hasMoreElements()) {
            if (entries.nextElement().getName().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDirectory(String path) {
        String normalized = normalizePath(path);
        if (this.closed || normalized == null) {
            return false;
        }
        String prefix = this.contentPrefix + normalized;
        return this.hasPrefix(prefix.isEmpty() || prefix.endsWith("/") ? prefix : prefix + '/');
    }

    @Override
    public InputStream getInputStream(String path) {
        String normalized = normalizePath(path);
        if (this.closed || normalized == null) {
            return null;
        }
        ZipEntry entry = this.zipFile.getEntry(this.contentPrefix + normalized);
        if (entry == null || entry.isDirectory()) {
            return null;
        }
        try {
            return this.zipFile.getInputStream(entry);
        } catch (IOException ex) {
            return null;
        }
    }

    public boolean hasShaderPrograms() {
        if (this.closed) {
            return false;
        }
        return this.zipFile.stream().anyMatch(entry -> {
            String name = entry.getName();
            if (!name.startsWith(this.contentPrefix)) {
                return false;
            }
            String relative = name.substring(this.contentPrefix.length());
            return MapShaderpackResolver.isShaderProgram(relative);
        });
    }

    @Override
    public String getType() {
        return "zip";
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.zipFile.close();
        }
    }
}
