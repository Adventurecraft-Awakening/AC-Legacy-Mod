package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_ItemWrench extends Item {

    protected AC_ItemWrench(int var1) {
        super(var1);
    }

    public @Override boolean useOn(ItemInstance item, Player player, Level level, int x, int y, int z, int face) {
        if (!AC_ItemCursor.bothSet) {
            return false;
        }

        int tileId = level.getTile(x, y, z);
        int tileMeta = level.getData(x, y, z);
        Minecraft.instance.gui.addMessage(String.format("Swapping blocks with BlockID %d", tileId));

        Coord min = AC_ItemCursor.one().min(AC_ItemCursor.two());
        Coord max = AC_ItemCursor.one().max(AC_ItemCursor.two());

        for (int bX = min.x; bX <= max.x; bX++) {
            for (int bY = min.y; bY <= max.y; bY++) {
                for (int bZ = min.z; bZ <= max.z; bZ++) {
                    int foundId = level.getTile(bX, bY, bZ);
                    // TODO: clean this switch up...
                    switch (foundId) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                        case 20:
                        case 21:
                        case 22:
                        case 24:
                        case 26:
                        case 27:
                        case 28:
                        case 29:
                        case 30:
                        case 31:
                        case 32:
                        case 33:
                        case 34:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 39:
                        case 40:
                        case 41:
                        case 42:
                        case 43:
                        case 44:
                        case 45:
                        case 47:
                        case 48:
                        case 49:
                        case 50:
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 56:
                        case 57:
                        case 58:
                        case 59:
                        case 60:
                        case 63:
                        case 65:
                        case 66:
                        case 67:
                        case 68:
                        case 73:
                        case 74:
                        case 78:
                        case 79:
                        case 80:
                        case 81:
                        case 82:
                        case 83:
                        case 84:
                        case 85:
                        case 86:
                        case 87:
                        case 88:
                        case 89:
                        case 90:
                        case 91:
                        case 92:
                        case 95:
                        case 96:
                        case 97:
                        case 98:
                        case 99:
                        case 100:
                        case 105:
                        case 106:
                        case 112:
                        case 113:
                        case 114:
                        case 116:
                        default:
                            level.setTileAndData(bX, bY, bZ, tileId, tileMeta);
                            break;
                        case 0:
                        case 23:
                        case 25:
                        case 46:
                        case 55:
                        case 61:
                        case 62:
                        case 64:
                        case 69:
                        case 70:
                        case 71:
                        case 72:
                        case 75:
                        case 76:
                        case 77:
                        case 93:
                        case 94:
                        case 101:
                        case 102:
                        case 103:
                        case 104:
                        case 107:
                        case 108:
                        case 109:
                        case 110:
                        case 111:
                        case 115:
                        case 117:
                        case 118:
                            break;
                    }
                }
            }
        }
        return false;
    }

    public @Override float getDestroySpeed(ItemInstance item, Tile tile) {
        return 32.0F;
    }

    public @Override boolean canDestroySpecial(Tile tile) {
        return true;
    }

    public @Override boolean isMirroredArt() {
        return true;
    }
}
