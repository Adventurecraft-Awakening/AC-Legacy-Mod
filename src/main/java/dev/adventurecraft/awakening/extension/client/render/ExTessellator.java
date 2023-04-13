package dev.adventurecraft.awakening.extension.client.render;

public interface ExTessellator {

    int BUFFER_SIZE = 262144;
    int[] terrainTextures = new int[256];
    int[] itemTextures = new int[256];

    void setRenderingChunk(boolean value);
}
