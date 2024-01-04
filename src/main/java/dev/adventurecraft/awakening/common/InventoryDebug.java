package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryDebug implements Inventory {

    private String inventoryTitle;
    private int size;
    private ItemStack[] inventoryContents;
    public int firstItem;
    public int lastItem;
    public boolean atEnd;

    public InventoryDebug(String var1, int var2) {
        this.inventoryTitle = var1;
        this.size = var2;
        this.inventoryContents = new ItemStack[var2];
    }

    private int getID(int var1) {
        int var2;
        for (var2 = 0; var2 < 4; ++var2) {
            if (var1 > Block.GRASS.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > Block.SAND.id) {
                --var1;
            }
        }

        if (var1 > Block.LOG.id) {
            --var1;
        }

        if (var1 > Block.LOG.id) {
            --var1;
        }

        for (var2 = 0; var2 < 2; ++var2) {
            if (var1 > Block.TALLGRASS.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > Block.WOOL.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > Block.STONE_SLAB.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.pillarStone.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.pillarMetal.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.plant1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.trees.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.glassBlocks.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 9; ++var2) {
            if (var1 > AC_Blocks.cageBlocks.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.stoneBlocks1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.stoneBlocks2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.stoneBlocks3.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.woodBlocks.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.halfSteps1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.halfSteps2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.halfSteps3.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.tableBlocks.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.chairBlocks1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.chairBlocks2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 14; ++var2) {
            if (var1 > AC_Blocks.ropes1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 14; ++var2) {
            if (var1 > AC_Blocks.ropes2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 8; ++var2) {
            if (var1 > AC_Blocks.chains.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.ladders1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.ladders2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.ladders3.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.ladders4.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 13; ++var2) {
            if (var1 > AC_Blocks.lights1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.plant2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > AC_Blocks.plant3.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 6; ++var2) {
            if (var1 > AC_Blocks.overlay1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.stairs1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.stairs2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.stairs3.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.stairs4.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.slopes1.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.slopes2.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.slopes3.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 3; ++var2) {
            if (var1 > AC_Blocks.slopes4.id) {
                --var1;
            }
        }

        for (var2 = 0; var2 < 15; ++var2) {
            if (var1 > Item.DYE_POWDER.id) {
                --var1;
            }
        }

        return var1;
    }

    private int getSubtype(int var1) {
        int var2 = 0;

        int var3;
        for (var3 = 0; var3 < 4; ++var3) {
            if (var1 > Block.GRASS.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > Block.GRASS.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > Block.SAND.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > Block.SAND.id) {
            var2 = 0;
        }

        if (var1 > Block.LOG.id) {
            --var1;
            ++var2;
        }

        if (var1 > Block.LOG.id) {
            --var1;
            ++var2;
        }

        if (var1 > Block.LOG.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 2; ++var3) {
            if (var1 > Block.TALLGRASS.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > Block.TALLGRASS.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > Block.WOOL.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > Block.WOOL.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > Block.STONE_SLAB.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > Block.STONE_SLAB.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.pillarStone.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.pillarStone.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.pillarMetal.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.pillarMetal.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.plant1.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.plant1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.trees.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.trees.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.glassBlocks.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.glassBlocks.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 9; ++var3) {
            if (var1 > AC_Blocks.cageBlocks.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.cageBlocks.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.stoneBlocks1.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.stoneBlocks1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.stoneBlocks2.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.stoneBlocks2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.stoneBlocks3.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.stoneBlocks3.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.woodBlocks.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.woodBlocks.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.halfSteps1.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.halfSteps1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.halfSteps2.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.halfSteps2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.halfSteps3.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.halfSteps3.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.tableBlocks.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.tableBlocks.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.chairBlocks1.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.chairBlocks1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.chairBlocks2.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.chairBlocks2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 14; ++var3) {
            if (var1 > AC_Blocks.ropes1.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.ropes1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 14; ++var3) {
            if (var1 > AC_Blocks.ropes2.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.ropes2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 8; ++var3) {
            if (var1 > AC_Blocks.chains.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.chains.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.ladders1.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.ladders1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.ladders2.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.ladders2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.ladders3.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.ladders3.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.ladders4.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.ladders4.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 13; ++var3) {
            if (var1 > AC_Blocks.lights1.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.lights1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.plant2.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.plant2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > AC_Blocks.plant3.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.plant3.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 6; ++var3) {
            if (var1 > AC_Blocks.overlay1.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > AC_Blocks.overlay1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.stairs1.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.stairs1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.stairs2.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.stairs2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.stairs3.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.stairs3.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.stairs4.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.stairs4.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.slopes1.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.slopes1.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.slopes2.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.slopes2.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.slopes3.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.slopes3.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 3; ++var3) {
            if (var1 > AC_Blocks.slopes4.id) {
                --var1;
                var2 += 4;
            }
        }

        if (var1 > AC_Blocks.slopes4.id) {
            var2 = 0;
        }

        for (var3 = 0; var3 < 15; ++var3) {
            if (var1 > Item.DYE_POWDER.id) {
                --var1;
                ++var2;
            }
        }

        if (var1 > Item.DYE_POWDER.id) {
            var2 = 0;
        }

        return var2;
    }

    public void fillInventory(int firstBlockID) {
        boolean firstItemSet = false;
        this.atEnd = false;

        for (int relativeBlockID = 0; relativeBlockID < this.size; ++relativeBlockID) {
            int currentBlockID = this.getID(relativeBlockID + firstBlockID);
            Item currentBlockItem = Item.byId[currentBlockID];
            if (currentBlockItem != null) {
                this.inventoryContents[relativeBlockID] = new ItemStack(currentBlockItem, -64);
                this.inventoryContents[relativeBlockID].setMeta(this.getSubtype(relativeBlockID + firstBlockID));
                this.lastItem = relativeBlockID + firstBlockID;
                if (!firstItemSet) {
                    this.firstItem = relativeBlockID + firstBlockID;
                    firstItemSet = true;
                }
            } else {
                if (currentBlockID >= 31999) {
                    for (this.atEnd = true; relativeBlockID < this.size; ++relativeBlockID) {
                        this.inventoryContents[relativeBlockID] = null;
                    }

                    return;
                }

                --relativeBlockID;
                ++firstBlockID;
            }
        }

    }

    public void fillInventoryBackwards(int var1) {
        boolean var2 = false;
        this.atEnd = false;

        for (int var3 = 0; var3 < this.size; ++var3) {
            int var4 = this.getID(var1 - var3);
            if (var4 > 0 && Item.byId[var4] != null) {
                this.inventoryContents[this.size - var3 - 1] = new ItemStack(Item.byId[var4], -64);
                this.inventoryContents[this.size - var3 - 1].setMeta(this.getSubtype(var1 - var3));
                this.firstItem = var1 - var3;
                if (!var2) {
                    this.lastItem = var1 - var3;
                    var2 = true;
                }
            } else {
                if (var1 - var3 <= 1) {
                    while (var3 < this.size) {
                        this.inventoryContents[this.size - var3 - 1] = null;
                        ++var3;
                    }

                    return;
                }

                --var3;
                --var1;
            }
        }

    }

    public ItemStack getInventoryItem(int var1) {
        return this.inventoryContents[var1];
    }

    public ItemStack takeInventoryItem(int var1, int var2) {
        return this.inventoryContents[var1] != null ? this.inventoryContents[var1].copy() : null;
    }

    public void setInventoryItem(int var1, ItemStack var2) {
        if (this.inventoryContents[var1] != null) {
            this.inventoryContents[var1] = this.inventoryContents[var1].copy();
        }

    }

    public int getInventorySize() {
        return this.size;
    }

    public String getContainerName() {
        return this.inventoryTitle;
    }

    public int getMaxItemCount() {
        return 64;
    }

    public void markDirty() {
    }

    public boolean canPlayerUse(PlayerEntity var1) {
        return true;
    }
}
