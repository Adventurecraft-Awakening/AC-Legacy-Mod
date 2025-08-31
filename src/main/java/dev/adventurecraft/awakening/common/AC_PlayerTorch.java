package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class AC_PlayerTorch {

    static boolean torchActive;
    static float posX;
    static float posY;
    static float posZ;
    static int iX;
    static int iY;
    static int iZ;

    final static float moveThreshold = 0.05F;
    final static int torchBrightness = 15;
    final static int range = torchBrightness * 2 + 1;
    final static float[] cache = new float[range * range * range];

    public static boolean isTorchActive() {
        return torchActive;
    }

    public static void setTorchState(Level world, boolean active) {
        if (torchActive != active) {
            torchActive = active;
            markBlocksDirty(world);
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
            iX = (int) posX;
            iY = (int) posY;
            iZ = (int) posZ;
            markBlocksDirty(world);
        }
    }

    private static float getCachedTorchLight(Level world, int x, int y, int z) {
        int bX = x - iX + torchBrightness;
        int bY = y - iY + torchBrightness;
        int bZ = z - iZ + torchBrightness;
        if (bX >= 0 && bX < range && bY >= 0 && bY < range && bZ >= 0 && bZ < range) {
            return cache[bX * range * range + bY * range + bZ];
        }
        return 0.0F;
    }

    public static float getTorchLight(Level world, int x, int y, int z) {
        if (torchActive) {
            return getCachedTorchLight(world, x, y, z);
        }
        return 0.0F;
    }

    private static void markBlocksDirty(Level world) {
        float baseX = (posX - (float) iX) - 0.5f;
        float baseY = (posY - (float) iY) - 0.5f;
        float baseZ = (posZ - (float) iZ) - 0.5f;
        int index = 0;

        final int range = torchBrightness;
        final float emission = torchBrightness;

        for (int rX = -range; rX <= range; ++rX) {
            int x = rX + iX;

            for (int rY = -range; rY <= range; ++rY) {
                int y = rY + iY;

                for (int rZ = -range; rZ <= range; ++rZ) {
                    int z = rZ + iZ;

                    // TODO: batch-get tiles, and maybe light?
                    int id = world.getTile(x, y, z);
                    float result = 0.0F;

                    if (id == 0 || ExBlock.neighborLit[id]) {
                        float xLight = Math.abs(rX - baseX);
                        float yLight = Math.abs(rY - baseY);
                        float zLight = Math.abs(rZ - baseZ);
                        float light = xLight + yLight + zLight;

                        if (light <= emission) {
                            if (emission - light > world.getLightLevel(x, y, z)) {
                                world.sendTileUpdated(x, y, z);
                            }
                            result = emission - light;
                        }
                    }
                    cache[index++] = result;
                }
            }
        }
    }
}
