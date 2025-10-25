package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiScript;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityScript;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockScript extends TileEntityTile implements AC_ITriggerDebugBlock {

    protected AC_BlockScript(int var1, int var2) {
        super(var1, var2, Material.AIR);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityScript();
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
    public int getTexture(LevelSource view, int x, int y, int z, int side) {
        return super.getTexture(view, x, y, z, side);
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityScript entityScript)) {
            return;
        }
        if (!entityScript.onTriggerScriptFile.isEmpty()) {
            ((ExWorld) world).getScriptHandler().runScript(entityScript.onTriggerScriptFile, entityScript.scope);
        }

        entityScript.isActivated = true;
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof AC_TileEntityScript entityScript)) {
            return;
        }
        if (!entityScript.onDetriggerScriptFile.isEmpty()) {
            ((ExWorld) world).getScriptHandler().runScript(entityScript.onDetriggerScriptFile, entityScript.scope);
        }
        entityScript.isActivated = false;

    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.active) {
            return false;
        }
        if ((player.getSelectedItem() == null || player.getSelectedItem().id == AC_Items.cursor.id)) {
            if (world.getTileEntity(x, y, z) instanceof AC_TileEntityScript entityScript) {
                AC_GuiScript.showUI(entityScript);
            }
            return true;
        }
        return false;
    }
}
