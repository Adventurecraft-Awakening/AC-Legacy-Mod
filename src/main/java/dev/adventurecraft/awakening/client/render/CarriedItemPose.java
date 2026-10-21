package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.Matrix4;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.tile.Tile;

public final class CarriedItemPose {
    private CarriedItemPose() { }
    public static Matrix4 pose(Matrix4 arm,ItemInstance item,boolean player) {
        Matrix4 pose=arm.translated(-.0625F,.4375F,.0625F);
        if(item.id<256 && BlockMeshes.standard3D(Tile.tiles[item.id].getRenderShape()))
            return pose.translated(0,.1875F,-.3125F).rotated(20,1,0,0).rotated(45,0,1,0).scaled(.375F,-.375F,.375F);
        if(Item.items[item.id].isHandEquipped()) {
            if(player && Item.items[item.id].isMirroredArt()) pose=pose.rotated(180,0,0,1).translated(0,-.125F,0);
            return pose.translated(0,.1875F,0).scaled(.625F,-.625F,.625F).rotated(-100,1,0,0).rotated(45,0,1,0);
        }
        return pose.translated(.25F,.1875F,-.1875F).scaled(.375F,.375F,.375F)
            .rotated(60,0,0,1).rotated(-90,1,0,0).rotated(20,0,0,1);
    }
}
