package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import java.util.ArrayDeque;

/** A renderer-local object pose; snapshots never read or mutate compatibility state. */
public final class PoseStack {
    private final ArrayDeque<DrawParameters> stack = new ArrayDeque<>();
    private DrawParameters parameters;
    public PoseStack(DrawParameters parameters) { this.parameters = parameters; }
    public DrawParameters parameters() { return this.parameters; }
    public void push() { this.stack.push(this.parameters); }
    public void pop() {
        if (this.stack.isEmpty()) throw new IllegalStateException("Unbalanced local pose stack");
        this.parameters = this.stack.pop();
    }
    public void model(Matrix4 model) { this.parameters = this.parameters.withObject(this.parameters.object().withModel(model)); }
    public void translate(float x, float y, float z) { this.model(this.parameters.object().model().translated(x, y, z)); }
    public void rotate(float angle, float x, float y, float z) { this.model(this.parameters.object().model().rotated(angle, x, y, z)); }
    public void scale(float x, float y, float z) { this.model(this.parameters.object().model().scaled(x, y, z)); }
    public void tint(float r, float g, float b, float a) {
        this.parameters = this.parameters.withObject(this.parameters.object().withTint(new Color4(r, g, b, a)));
    }
}
