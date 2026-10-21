package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.client.render.lighting.HeldTorchLightCache;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_PlayerTorch {

    static boolean torchActive;
    static float posX;
    static float posY;
    static float posZ;

    final static float moveThreshold = 0.05F;
    private static final HeldTorchLightCache cache = new HeldTorchLightCache();

    public static boolean isTorchActive() {
        return torchActive;
    }

    public static void setTorchState(Level world, boolean active) {
        if (torchActive != active) {
            boolean wasActive = torchActive;
            torchActive = active;
            markBlocksDirty(world, wasActive);
        }
    }

    public static void setTorchPos(Level world, float x, float y, float z) {
        double avgFrameTime = ((ExMinecraft) Minecraft.instance).getFrameTime();
        int updateRate = 1;
        if (avgFrameTime > 1 / 30.0) {
            updateRate = 3;
        }
        else if (avgFrameTime > 1 / 60.0) {
            updateRate = 2;
        }

        float dX = Math.abs(x - posX);
        float dY = Math.abs(y - posY);
        float dZ = Math.abs(z - posZ);
        if ((dX > moveThreshold || dY > moveThreshold || dZ > moveThreshold) &&
            (int) world.getTime() % updateRate == 0L) {
            posX = x;
            posY = y;
            posZ = z;
            markBlocksDirty(world, torchActive);
        }
    }

    public static float getTorchLight(int x, int y, int z) {
        if (torchActive) {
            return cache.get(x, y, z);
        }
        return 0.0F;
    }

    public static HeldTorchLightCache.Statistics statistics() {
        return cache.statistics();
    }

    private static void markBlocksDirty(Level world, boolean wasActive) {
        boolean nonnegativeOpacity = true;
        for (int opacity : Tile.lightBlock) {
            if (opacity < 0) {
                nonnegativeOpacity = false;
                break;
            }
        }

        var client = Minecraft.instance;
        var renderer = client == null ? null : (ExWorldEventRenderer) client.levelRenderer;
        long rendererEpoch = renderer == null ? -1 : renderer.ac$beginTorchLightUpdate(world);
        try {
            cache.update(posX, posY, posZ, wasActive, torchActive, nonnegativeOpacity, new HeldTorchLightCache.Samples() {
                @Override
                public int opacity(int x, int y, int z) {
                    return Tile.lightBlock[world.getTile(x, y, z)];
                }

                @Override
                public int lightLevel(int x, int y, int z) {
                    return world.getLightLevel(x, y, z);
                }

                @Override
                public void tileChanged(int x, int y, int z) {
                    // Preserve the world's complete listener list and its callback order.
                    world.sendTileUpdated(x, y, z);
                }
            });
        } finally {
            // Only renderer work is delayed, until every cache sample is available.
            if (rendererEpoch != -1) renderer.ac$endTorchLightUpdate(world, rendererEpoch, cache.dirtySections());
        }
    }
}
