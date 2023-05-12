package dev.adventurecraft.awakening.extension.item;

import dev.adventurecraft.awakening.common.AC_ILeftClickItem;
import net.minecraft.entity.player.PlayerEntity;

public interface ExItem extends AC_ILeftClickItem {

    boolean getDecrementDamage();

    void setDecrementDamage(boolean value);

    int getItemUseDelay();

    void setItemUseDelay(int value);

    void onAddToSlot(PlayerEntity var1, int var2, int var3);

    void onRemovedFromSlot(PlayerEntity var1, int var2, int var3);
}
