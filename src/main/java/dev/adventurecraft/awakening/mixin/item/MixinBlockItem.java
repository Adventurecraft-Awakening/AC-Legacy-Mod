package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.extension.item.ExTileItem;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileItem.class)
public abstract class MixinBlockItem implements ExTileItem {

    @Shadow
    private int tileId;
    @Overwrite
    public boolean useOn(ItemInstance item, Player player, Level level, int x, int y, int z, int face) {
        if(!AC_DebugMode.active) {
            return false;
        }
        if (level.getTile(x, y, z) == Tile.SNOW_LAYER.id) {
            face = 0;
        } else {
            if (face == 0) {
                --y;
            }

            if (face == 1) {
                ++y;
            }

            if (face == 2) {
                --z;
            }

            if (face == 3) {
                ++z;
            }

            if (face == 4) {
                --x;
            }

            if (face == 5) {
                ++x;
            }
        }

        if (item.count == 0) {
            return false;
        } else if (y == 128 && Tile.tiles[this.tileId].material.isSolid()) {
            return false;
        } else if (level.mayPlace(this.tileId, x, y, z, false, face)) {
            Tile var8 = Tile.tiles[this.tileId];
            if (level.setTileAndData(x, y, z, this.tileId, this.getLevelDataForAuxValue(item.getAuxValue()))) {
                Tile.tiles[this.tileId].setPlacedOnFace(level, x, y, z, face);
                Tile.tiles[this.tileId].setPlacedBy(level, x, y, z, player);
                level.playSound((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), var8.soundType.getStepSound(), (var8.soundType.getVolume() + 1.0F) / 2.0F, var8.soundType.getPitch() * 0.8F);
                --item.count;
            }

            return true;
        } else {
            return false;
        }
    }
}
