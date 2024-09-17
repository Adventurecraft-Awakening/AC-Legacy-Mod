package dev.adventurecraft.awakening.common;

import java.util.Random;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;

public class AC_BlockPushable extends AC_BlockColor {

    public AC_BlockPushable(int id, int texture, Material material) {
        super(id, texture, material);
    }

    @Override
    public void onPlace(Level world, int x, int y, int z) {
        world.addToTickNextTick(x, y, z, this.id, this.getTickDelay());
    }

    @Override
    public void neighborChanged(Level world, int x, int y, int z, int id) {
        world.addToTickNextTick(x, y, z, this.id, this.getTickDelay());
    }

    @Override
    public void tick(Level world, int x, int y, int z, Random rng) {
        this.tryToFall(world, x, y, z);
    }

    private void tryToFall(Level world, int x, int y, int z) {
        if (canFallBelow(world, x, y - 1, z) && y >= 0) {
            FallingTile var5 = new FallingTile(world, (float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F, this.id);
            ((ExFallingBlockEntity) var5).setMetadata(world.getData(x, y, z));
            world.addEntity(var5);
        }
    }

    @Override
    public int getTickDelay() {
        return 3;
    }

    public static boolean canFallBelow(Level world, int x, int y, int z) {
        int id = world.getTile(x, y, z);
        if (id == 0) {
            return true;
        } else if (id == Tile.FIRE.id) {
            return true;
        } else {
            Material var5 = Tile.tiles[id].material;
            if (var5 == Material.WATER) {
                return true;
            }
            return var5 == Material.LAVA;
        }
    }
}
