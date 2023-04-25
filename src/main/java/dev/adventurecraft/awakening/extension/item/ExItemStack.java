package dev.adventurecraft.awakening.extension.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface ExItemStack {

    boolean useItemLeftClick(PlayerEntity var1, World var2, int var3, int var4, int var5, int var6);

    boolean isReloading();

    int getTimeLeft();

    void setTimeLeft(int value);
}
