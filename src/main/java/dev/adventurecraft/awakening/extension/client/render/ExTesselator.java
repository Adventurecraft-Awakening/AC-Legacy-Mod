package dev.adventurecraft.awakening.extension.client.render;

import dev.adventurecraft.awakening.client.render.VertexSink;
import org.lwjgl.util.vector.Vector3f;

/** Minecraft compatibility name for the native vertex-writing contract. */
public interface ExTesselator extends VertexSink {
    default void ac$normal32(Vector3f direction) { ac$normal32(direction.x, direction.y, direction.z); }
    default void ac$vertexUV(Vector3f position, float u, float v) { ac$vertexUV(position.x, position.y, position.z, u, v); }
}
