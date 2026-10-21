package dev.adventurecraft.awakening.client.render.shader.sources;

import java.io.Closeable;
import java.io.IOException;

/** A single map's shader source. Each loaded map owns and closes its own source. */
public abstract class ShaderpackSource implements Closeable {

    public static final String TypeSeparator = "$";

    public abstract String getName();

    public abstract String getType();

    @Override
    public void close() throws IOException {
    }
}
