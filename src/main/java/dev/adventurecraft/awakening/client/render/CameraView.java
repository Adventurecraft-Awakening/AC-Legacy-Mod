package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.DrawParameters;
import dev.adventurecraft.awakening.client.render.state.Matrix4;
import dev.adventurecraft.awakening.client.render.state.Rect;

/** Pure projection and picking using the same Vulkan-space matrices as a render pass. */
public final class CameraView {
    private final Matrix4 transform;
    private final Rect viewport;

    public CameraView(DrawParameters parameters) {
        this.transform = parameters.view().projection().multiply(parameters.view().view()).multiply(parameters.object().model());
        this.viewport = java.util.Objects.requireNonNull(parameters.raster().viewport(), "Camera viewport");
        if (this.viewport.width() <= 0 || this.viewport.height() <= 0) throw new IllegalArgumentException("Empty camera viewport");
    }

    public record Ray(double originX, double originY, double originZ, double directionX, double directionY, double directionZ) { }

    public Ray screenRay(double x, double y) {
        double[] inverse = RenderMath.inverse(this.transform);
        double nx = (x - this.viewport.x()) / this.viewport.width() * 2 - 1;
        double ny = (y - this.viewport.y()) / this.viewport.height() * 2 - 1;
        double[] near = RenderMath.transform(inverse, nx, ny, 0);
        double[] far = RenderMath.transform(inverse, nx, ny, 1);
        double dx = far[0] - near[0], dy = far[1] - near[1], dz = far[2] - near[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (!(length > 0)) throw new IllegalStateException("Invalid camera ray");
        return new Ray(near[0], near[1], near[2], dx / length, dy / length, dz / length);
    }

    public Rect projectRect(dev.adventurecraft.awakening.layout.Rect rectangle) {
        double left = Double.POSITIVE_INFINITY, top = left, right = Double.NEGATIVE_INFINITY, bottom = right;
        double[] matrix = new double[16];
        for (int i = 0; i < 16; i++) matrix[i] = this.transform.get(i);
        for (int corner = 0; corner < 4; corner++) {
            double x = (corner & 1) == 0 ? rectangle.left() : rectangle.right();
            double y = (corner & 2) == 0 ? rectangle.top() : rectangle.bot();
            double[] clip = RenderMath.transform(matrix, x, y, 0);
            double px = this.viewport.x() + (clip[0] + 1) * this.viewport.width() * 0.5;
            double py = this.viewport.y() + (clip[1] + 1) * this.viewport.height() * 0.5;
            left = Math.min(left, px); top = Math.min(top, py);
            right = Math.max(right, px); bottom = Math.max(bottom, py);
        }
        int x = (int) Math.floor(left + 1e-5), y = (int) Math.floor(top + 1e-5);
        return new Rect(x, y, Math.max(0, (int) Math.ceil(right - 1e-5) - x), Math.max(0, (int) Math.ceil(bottom - 1e-5) - y));
    }

    public static Rect intersect(Rect a, Rect b) {
        int x = Math.max(a.x(), b.x()), y = Math.max(a.y(), b.y());
        return new Rect(x, y, Math.max(0, Math.min(a.x() + a.width(), b.x() + b.width()) - x),
            Math.max(0, Math.min(a.y() + a.height(), b.y() + b.height()) - y));
    }

}
