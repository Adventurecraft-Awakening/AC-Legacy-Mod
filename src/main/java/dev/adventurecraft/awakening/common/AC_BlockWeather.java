package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockWeather extends BlockWithEntity {
    protected AC_BlockWeather(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityWeather();
    }

    public boolean isFullOpaque() {
        return false;
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        AC_TileEntityWeather var5 = (AC_TileEntityWeather) var1.getBlockEntity(var2, var3, var4);
        if (var5.changePrecipitate) {
            var1.properties.setRaining(var5.precipitate);
            ((ExWorld) var1).resetCoordOrder();
        }

        if (var5.changeTempOffset) {
            ((ExWorldProperties) var1.properties).setTempOffset(var5.tempOffset);
            ((ExWorld) var1).resetCoordOrder();
        }

        if (var5.changeTimeOfDay) {
            ((ExWorld) var1).setTimeOfDay((long) var5.timeOfDay);
        }

        if (var5.changeTimeRate) {
            ((ExWorldProperties) var1.properties).setTimeRate(var5.timeRate);
        }

    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        if (AC_DebugMode.active && var5.getHeldItem() != null && var5.getHeldItem().itemId == AC_Items.cursor.id) {
            AC_TileEntityWeather var6 = (AC_TileEntityWeather) var1.getBlockEntity(var2, var3, var4);
            AC_GuiWeather.showUI(var1, var6);
            return true;
        } else {
            return false;
        }
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
