package dev.adventurecraft.awakening.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AC_BlockLockedDoor extends Tile implements AC_ITriggerBlock {

    int doorKeyToUse;

    protected AC_BlockLockedDoor(int var1, int var2, int var3) {
        super(var1, Material.METAL);
        this.tex = var2;
        this.doorKeyToUse = var3;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public HitResult clip(Level var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6) {
        int var7 = var1.getData(var2, var3, var4);
        return !AC_DebugMode.active && var7 == 1 ? null : super.clip(var1, var2, var3, var4, var5, var6);
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        int var5 = var1.getData(var2, var3, var4);
        return !AC_DebugMode.active && var5 != 1 ? super.getAABB(var1, var2, var3, var4) : null;
    }

    @Override
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        int var5 = view.getData(x, y, z);
        return AC_DebugMode.active || var5 == 0;
    }

    @Override
    public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
        if (var5 != 0 && var5 != 1) {
            int var6 = 1;
            while (var1.getTile(var2, var3 + var6, var4) == this.id) {
                ++var6;
            }

            int var7;
            for (var7 = 1; var1.getTile(var2, var3 - var7, var4) == this.id; ++var7) {
                ++var6;
            }

            int var8 = this.tex;
            if (var6 > 2) {
                if (var6 / 2 == var7 - 1) {
                    int var9 = 1;

                    var7 = 1;
                    while (var1.getTile(var2 + var9, var3, var4) == this.id) {
                        ++var9;
                    }

                    while (var1.getTile(var2 - var7, var3, var4) == this.id) {
                        ++var9;
                        ++var7;
                    }

                    if (var9 == 1) {
                        while (var1.getTile(var2, var3, var4 + var9) == this.id) {
                            ++var9;
                        }

                        while (var1.getTile(var2, var3, var4 - var7) == this.id) {
                            ++var9;
                            ++var7;
                        }
                    }

                    if (var9 / 2 == var7 - 1) {
                        ++var8;
                    }
                }
            } else {
                var8 += 16;
                if (var1.getTile(var2, var3 - 1, var4) != this.id) {
                    var8 += 16;
                }

                if (var5 == 2) {
                    if (var1.getTile(var2 + 1, var3, var4) == this.id) {
                        ++var8;
                    }
                } else if (var5 == 3) {
                    if (var1.getTile(var2 - 1, var3, var4) == this.id) {
                        ++var8;
                    }
                } else if (var5 == 4) {
                    if (var1.getTile(var2, var3, var4 - 1) == this.id) {
                        ++var8;
                    }
                } else if (var5 == 5 && var1.getTile(var2, var3, var4 + 1) == this.id) {
                    ++var8;
                }
            }

            return var8;
        } else {
            return this.tex;
        }
    }

    @Override
    public void attack(Level var1, int var2, int var3, int var4, Player var5) {
        if (var5.inventory.removeResource(this.doorKeyToUse)) {
            var1.playSound((double) var2 + 0.5D, (double) var3 + 0.5D, (double) var4 + 0.5D, "random.door_open", 1.0F, var1.random.nextFloat() * 0.1F + 0.9F);

            int var6;
            int var7;
            for (var6 = 0; var1.getTile(var2, var3 + var6, var4) == this.id; ++var6) {
                for (var7 = 0; var1.getTile(var2 + var7, var3 + var6, var4) == this.id; ++var7) {
                    var1.setData(var2 + var7, var3 + var6, var4, 1);
                    var1.sendTileUpdated(var2 + var7, var3 + var6, var4);
                }

                for (var7 = 1; var1.getTile(var2 - var7, var3 + var6, var4) == this.id; ++var7) {
                    var1.setData(var2 - var7, var3 + var6, var4, 1);
                    var1.sendTileUpdated(var2 - var7, var3 + var6, var4);
                }

                for (var7 = 1; var1.getTile(var2, var3 + var6, var4 + var7) == this.id; ++var7) {
                    var1.setData(var2, var3 + var6, var4 + var7, 1);
                    var1.sendTileUpdated(var2, var3 + var6, var4 + var7);
                }

                for (var7 = 1; var1.getTile(var2, var3 + var6, var4 - var7) == this.id; ++var7) {
                    var1.setData(var2, var3 + var6, var4 - var7, 1);
                    var1.sendTileUpdated(var2, var3 + var6, var4 - var7);
                }
            }

            for (var6 = -1; var1.getTile(var2, var3 + var6, var4) == this.id; --var6) {
                for (var7 = 0; var1.getTile(var2 + var7, var3 + var6, var4) == this.id; ++var7) {
                    var1.setData(var2 + var7, var3 + var6, var4, 1);
                    var1.sendTileUpdated(var2 + var7, var3 + var6, var4);
                }

                for (var7 = 1; var1.getTile(var2 - var7, var3 + var6, var4) == this.id; ++var7) {
                    var1.setData(var2 - var7, var3 + var6, var4, 1);
                    var1.sendTileUpdated(var2 - var7, var3 + var6, var4);
                }

                for (var7 = 1; var1.getTile(var2, var3 + var6, var4 + var7) == this.id; ++var7) {
                    var1.setData(var2, var3 + var6, var4 + var7, 1);
                    var1.sendTileUpdated(var2, var3 + var6, var4 + var7);
                }

                for (var7 = 1; var1.getTile(var2, var3 + var6, var4 - var7) == this.id; ++var7) {
                    var1.setData(var2, var3 + var6, var4 - var7, 1);
                    var1.sendTileUpdated(var2, var3 + var6, var4 - var7);
                }
            }
        }
    }

    @Override
    public void reset(Level world, int x, int y, int z, boolean forDeath) {
        if (!forDeath) {
            world.setData(x, y, z, 0);
        }
    }

    @Override
    public int alwaysUseClick(Level world, int x, int y, int z) {
        return 0;
    }
}
