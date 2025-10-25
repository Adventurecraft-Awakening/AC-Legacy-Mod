package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiWeather;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityWeather;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockWeather extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockWeather(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityWeather();
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {

        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityWeather entityWeather)) {
            return;
        }
        if (entityWeather.changePrecipitate) {
            world.levelData.setRaining(entityWeather.precipitate);
            ((ExWorld) world).resetCoordOrder();
        }

        if (entityWeather.changeTempOffset) {
            ((ExWorldProperties) world.levelData).setTempOffset(entityWeather.tempOffset);
            ((ExWorld) world).resetCoordOrder();
        }

        if (entityWeather.changeTimeOfDay) {
            ((ExWorld) world).setTimeOfDay(entityWeather.timeOfDay);
        }

        if (entityWeather.changeTimeRate) {
            ((ExWorldProperties) world.levelData).setTimeRate(entityWeather.timeRate);
        }

    }
    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityWeather entityWeather) {
                AC_GuiWeather.showUI(entityWeather);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }
}
