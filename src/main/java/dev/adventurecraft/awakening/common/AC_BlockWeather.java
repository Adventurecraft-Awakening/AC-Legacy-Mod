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

public class AC_BlockWeather extends BlockWithEntity implements AC_ITriggerBlock {

    protected AC_BlockWeather(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected BlockEntity createBlockEntity() {
        return new AC_TileEntityWeather();
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }

    @Override
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(World world, int x, int y, int z) {
        var entity = (AC_TileEntityWeather) world.getBlockEntity(x, y, z);
        if (entity.changePrecipitate) {
            world.properties.setRaining(entity.precipitate);
            ((ExWorld) world).resetCoordOrder();
        }

        if (entity.changeTempOffset) {
            ((ExWorldProperties) world.properties).setTempOffset(entity.tempOffset);
            ((ExWorld) world).resetCoordOrder();
        }

        if (entity.changeTimeOfDay) {
            ((ExWorld) world).setTimeOfDay((long) entity.timeOfDay);
        }

        if (entity.changeTimeRate) {
            ((ExWorldProperties) world.properties).setTimeRate(entity.timeRate);
        }
    }

    @Override
    public void onTriggerDeactivated(World world, int x, int y, int z) {
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        if (AC_DebugMode.active && player.getHeldItem() != null && player.getHeldItem().itemId == AC_Items.cursor.id) {
            var entity = (AC_TileEntityWeather) world.getBlockEntity(x, y, z);
            AC_GuiWeather.showUI(world, entity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCollidable() {
        return AC_DebugMode.active;
    }
}
