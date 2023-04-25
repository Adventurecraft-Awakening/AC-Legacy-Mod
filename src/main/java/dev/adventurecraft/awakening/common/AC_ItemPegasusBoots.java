package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.item.ExItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;

public class AC_ItemPegasusBoots extends ArmorItem {
    public AC_ItemPegasusBoots(int var1) {
        super(var1, 0, 0, 3);
        this.setTexturePosition(183);
    }

    public void onAddToSlot(PlayerEntity var1, int var2, int var3) {
        ((ExItem) this).onAddToSlot(var1, var2, var3);
        if (var2 == 36) {
            ((ExPlayerEntity) var1).setCanWallJump(true);
            ((ExPlayerEntity) var1).setTimesCanJumpInAir(1);
        }
    }

    public void onRemovedFromSlot(PlayerEntity var1, int var2, int var3) {
        ((ExItem) this).onRemovedFromSlot(var1, var2, var3);
        if (var2 == 36) {
            ((ExPlayerEntity) var1).setCanWallJump(false);
            ((ExPlayerEntity) var1).setTimesCanJumpInAir(0);
        }
    }
}
