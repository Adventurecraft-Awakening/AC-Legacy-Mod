package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.FrameView;
import dev.adventurecraft.awakening.client.render.state.Matrix4;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.world.phys.AABB;

/** Culls world bounds against Vulkan's -W..W XY and 0..W depth clip volume. */
public final class ViewFrustum implements Culler, ExCameraView {
    private final double[][] planes = new double[6][4];
    private double x, y, z;

    public ViewFrustum(FrameView view, double cameraX, double cameraY, double cameraZ) {
        Matrix4 clip = view.projection().multiply(view.view());
        plane(0, clip, 3, 0, 1);
        plane(1, clip, 3, 0, -1);
        plane(2, clip, 3, 1, 1);
        plane(3, clip, 3, 1, -1);
        for (int column = 0; column < 4; column++) planes[4][column] = clip.get(column * 4 + 2);
        plane(5, clip, 3, 2, -1);
        for (double[] plane : planes) {
            double length = Math.sqrt(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]);
            if (length > 0 && Double.isFinite(length)) {
                for (int component = 0; component < 4; component++) plane[component] /= length;
            }
        }
        prepare(cameraX, cameraY, cameraZ);
    }

    private void plane(int index, Matrix4 matrix, int a, int b, int sign) {
        for (int column = 0; column < 4; column++) {
            planes[index][column] = matrix.get(column * 4 + a) + sign * matrix.get(column * 4 + b);
        }
    }

    @Override public void prepare(double cameraX, double cameraY, double cameraZ) {
        x = cameraX;
        y = cameraY;
        z = cameraZ;
    }

    @Override public boolean isVisible(AABB box) {
        for (double[] plane : planes) {
            double px = (plane[0] >= 0 ? box.x1 : box.x0) - x;
            double py = (plane[1] >= 0 ? box.y1 : box.y0) - y;
            double pz = (plane[2] >= 0 ? box.z1 : box.z0) - z;
            if (plane[0] * px + plane[1] * py + plane[2] * pz + plane[3] < -.0001) return false;
        }
        return true;
    }

    /** Fancy occlusion requires side-plane containment and near/far intersection. */
    @Override public boolean isBoundingBoxInFrustumFully(AABB box) {
        if (!isVisible(box)) return false;
        for (int side = 0; side < 4; side++) {
            double[] plane = planes[side];
            double px = (plane[0] >= 0 ? box.x0 : box.x1) - x;
            double py = (plane[1] >= 0 ? box.y0 : box.y1) - y;
            double pz = (plane[2] >= 0 ? box.z0 : box.z1) - z;
            if (plane[0] * px + plane[1] * py + plane[2] * pz + plane[3] <= .0001) return false;
        }
        return true;
    }
}
