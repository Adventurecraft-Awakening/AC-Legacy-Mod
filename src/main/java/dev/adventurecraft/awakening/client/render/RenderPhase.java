package dev.adventurecraft.awakening.client.render;

/** Ordered recording boundaries. Transparent draws and UI keep their submitted order. */
public enum RenderPhase {
    PREPARE(false), SKY(false), TERRAIN_OPAQUE(true), ENTITIES(true), PARTICLES(false),
    TERRAIN_TRANSLUCENT(false), WEATHER(false), CLOUDS(false), HANDS(false), UI(false), DEBUG(false), COMPAT(false);
    private final boolean groupOpaque;
    RenderPhase(boolean groupOpaque) { this.groupOpaque = groupOpaque; }
    public boolean groupOpaque() { return this.groupOpaque; }
}
