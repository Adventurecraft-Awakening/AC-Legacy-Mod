package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityWeather;
import dev.adventurecraft.awakening.common.gui.AC_GuiWeather;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.ItemInstance;
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
        var entity = (AC_TileEntityWeather) world.getTileEntity(x, y, z);
        if (entity.changePrecipitate) {
            world.levelData.setRaining(entity.precipitate);
            ((ExWorld) world).resetCoordOrder();
        }

        if (entity.changeTempOffset) {
            ((ExWorldProperties) world.levelData).setTempOffset(entity.tempOffset);
            ((ExWorld) world).resetCoordOrder();
        }

        if (entity.changeTimeOfDay) {
            ((ExWorld) world).setTimeOfDay(entity.timeOfDay);
        }

        if (entity.changeTimeRate) {
            ((ExWorldProperties) world.levelData).setTimeRate(entity.timeRate);
        }
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!((ExPlayerEntity) player).isDebugMode()) {
            return false;
        }
        ItemInstance item = player.getSelectedItem();
        if (item == null || item.id == AC_Items.cursor.id) {
            var entity = (AC_TileEntityWeather) world.getTileEntity(x, y, z);
            AC_GuiWeather.showUI(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean mayPick() {
        return false;
    }
}
