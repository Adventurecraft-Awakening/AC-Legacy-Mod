package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LightUpdate;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.*;

@Mixin(LightUpdate.class)
public abstract class MixinClass_417 {

    @Unique private static final int MAX_LIGHT = 15;

    @Shadow public @Final LightLayer type;
    @Shadow public int x0;
    @Shadow public int y0;
    @Shadow public int z0;
    @Shadow public int x1;
    @Shadow public int y1;
    @Shadow public int z1;

    @Overwrite
    public void update(Level level) {
        int n = this.x1 - this.x0 + 1;
        int n2 = this.y1 - this.y0 + 1;
        int n3 = this.z1 - this.z0 + 1;
        int n4 = n * n2 * n3;
        if (n4 > 32768) {
            ACMod.LOGGER.warn("Light too large, skipping!");
            return;
        }

        int ex = 0;
        int ez = 0;
        boolean bl = false;
        boolean bl2 = false;
        for (int x = this.x0; x <= this.x1; ++x) {
            for (int z = this.z0; z <= this.z1; ++z) {
                int cx = x >> 4;
                int cz = z >> 4;

                boolean hasChunks;
                if (bl && cx == ex && cz == ez) {
                    hasChunks = bl2;
                }
                else {
                    hasChunks = level.hasChunksAt(x, 0, z, 1);
                    if (hasChunks && level.getChunk(x >> 4, z >> 4).isEmpty()) {
                        hasChunks = false;
                    }
                    bl2 = hasChunks;
                    ex = cx;
                    ez = cz;
                }
                if (!hasChunks) {
                    continue;
                }
                this.y0 = MathF.clamp(this.y0, 0, 127);

                for (int y = this.y0; y <= this.y1; ++y) {
                    int n10 = level.getBrightness(this.type, x, y, z);
                    int id = level.getTile(x, y, z);
                    int n13 = Tile.lightBlock[id];
                    if (n13 == 0) {
                        n13 = 1;
                    }

                    int n14 = 0;
                    if (this.type == LightLayer.SKY) {
                        if (level.isSkyLit(x, y, z)) {
                            n14 = MAX_LIGHT;
                        }
                    }
                    else if (this.type == LightLayer.BLOCK) {
                        Tile block = Tile.tiles[id];
                        if (block != null) {
                            n14 = ((ExBlock) block).getBlockLightValue(level, x, y, z);
                        }
                        else {
                            n14 = Tile.lightEmission[id];
                        }
                    }

                    int l1;
                    if (n13 >= MAX_LIGHT && n14 == 0) {
                        l1 = 0;
                    }
                    else {
                        l1 = level.getBrightness(this.type, x, y - 1, z);
                        l1 = this.maxOrGetBrightness(l1, level, x, y + 1, z);
                        l1 = this.maxOrGetBrightness(l1, level, x - 1, y, z);
                        l1 = this.maxOrGetBrightness(l1, level, x + 1, y, z);
                        l1 = this.maxOrGetBrightness(l1, level, x, y, z - 1);
                        l1 = this.maxOrGetBrightness(l1, level, x, y, z + 1);
                        l1 = Math.max(l1 - n13, 0);
                        l1 = Math.max(n14, l1);
                    }

                    if (n10 == l1) {
                        continue;
                    }
                    level.setBrightness(this.type, x, y, z, l1);

                    int l2 = Math.max(l1 - 1, 0);
                    level.updateLightIfOtherThan(this.type, x - 1, y, z, l2);
                    level.updateLightIfOtherThan(this.type, x, y - 1, z, l2);
                    level.updateLightIfOtherThan(this.type, x, y, z - 1, l2);
                    if (x + 1 >= this.x1) {
                        level.updateLightIfOtherThan(this.type, x + 1, y, z, l2);
                    }
                    if (y + 1 >= this.y1) {
                        level.updateLightIfOtherThan(this.type, x, y + 1, z, l2);
                    }
                    if (z + 1 >= this.z1) {
                        level.updateLightIfOtherThan(this.type, x, y, z + 1, l2);
                    }
                }
            }
        }
    }

    @Unique
    private int maxOrGetBrightness(int value, Level level, int x, int y, int z) {
        if (value >= MAX_LIGHT) {
            return value;
        }
        return Math.max(value, level.getBrightness(this.type, x, y, z));
    }
}
