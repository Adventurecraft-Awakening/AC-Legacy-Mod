package dev.adventurecraft.awakening.world.item;

import net.minecraft.util.Facing;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.SoundType;
import net.minecraft.world.level.tile.Tile;

public class TopSnowTileItem extends TileItem {

    public TopSnowTileItem(int id) {
        super(id);
    }

    @Override
    public boolean useOn(ItemInstance item, Player player, Level level, int x, int y, int z, int face) {
        if (item.count == 0) {
            return false;
        }
        if (y == 127 && Tile.tiles[this.tileId].material.isSolid()) {
            return false;
        }

        int meta = 0;
        if (level.getTile(x, y, z) == Tile.SNOW_LAYER.id) {
            face = 0;
            meta = Math.min((level.getData(x, y, z) + 1), 7);
        }
        else {
            if (face == 0) {
                --y;
            }
            if (face == Facing.UP) {
                ++y;
            }
            if (face == Facing.NORTH) {
                --z;
            }
            if (face == Facing.SOUTH) {
                ++z;
            }
            if (face == Facing.WEST) {
                --x;
            }
            if (face == Facing.EAST) {
                ++x;
            }
        }

        if (!level.mayPlace(this.tileId, x, y, z, false, face)) {
            return false;
        }
        if (level.setTileAndData(x, y, z, this.tileId, meta)) {
            Tile tile = Tile.tiles[this.tileId];
            tile.setPlacedOnFace(level, x, y, z, face);
            tile.setPlacedBy(level, x, y, z, player);

            SoundType soundType = tile.soundType;
            level.playSound(
                x + 0.5f,
                y + 0.5f,
                z + 0.5f,
                soundType.getStepSound(),
                (soundType.getVolume() + 1.0f) / 2.0f,
                soundType.getPitch() * 0.8f
            );
            --item.count;
        }
        return true;
    }
}
