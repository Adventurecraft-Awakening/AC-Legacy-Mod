package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_PlayerTorch {

    static boolean torchActive;
    static float moveThreshold = 0.05F;
    static float posX;
    static float posY;
    static float posZ;
    static int iX;
    static int iY;
    static int iZ;
    static int torchBrightness = 15;
    static int range = torchBrightness * 2 + 1;
    static float[] cache = new float[range * range * range];

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
        float baseX = posX - (float) iX;
        float baseY = posY - (float) iY;
        float baseZ = posZ - (float) iZ;
        int index = 0;

        for (int rX = -torchBrightness; rX <= torchBrightness; ++rX) {
            int x = rX + iX;

            for (int rY = -torchBrightness; rY <= torchBrightness; ++rY) {
                int y = rY + iY;

                for (int rZ = -torchBrightness; rZ <= torchBrightness; ++rZ) {
                    int z = rZ + iZ;

                    int id = world.getTile(x, y, z);
                    float result = 0.0F;
                    // TODO: use ExBlock.neighborLit?
                    if (id == 0 || !Tile.tiles[id].isSolidRender() || id == Tile.SLAB.id || id == Tile.FARMLAND.id) {
                        double xLight = Math.abs(rX + 0.5D - baseX);
                        double yLight = Math.abs(rY + 0.5D - baseY);
                        double zLight = Math.abs(rZ + 0.5D - baseZ);
                        float light = (float) (xLight + yLight + zLight);

                        if (light <= torchBrightness) {
                            if (torchBrightness - light > world.getLightLevel(x, y, z)) {
                                world.sendTileUpdated(x, y, z);
                            }
                            result = torchBrightness - light;
                        }
                    }
                    cache[index++] = result;
                }
            }
        }
    }
}
