package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_ITriggerBlock;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityScript;
import dev.adventurecraft.awakening.common.gui.AC_GuiScript;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockScript extends TileEntityTile implements AC_ITriggerBlock {

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
    public boolean shouldRender(LevelSource view, int x, int y, int z) {
        return AC_DebugMode.active;
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
    public boolean canBeTriggered() {
        return true;
    }

    @Override
    public void onTriggerActivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityScript) world.getTileEntity(x, y, z);
        if (!entity.onTriggerScriptFile.equals("")) {
            ((ExWorld) world).getScriptHandler().runScript(entity.onTriggerScriptFile, entity.scope);
        }

        entity.isActivated = true;
    }

    @Override
    public void onTriggerDeactivated(Level world, int x, int y, int z) {
        var entity = (AC_TileEntityScript) world.getTileEntity(x, y, z);
        if (!entity.onDetriggerScriptFile.equals("")) {
            ((ExWorld) world).getScriptHandler().runScript(entity.onDetriggerScriptFile, entity.scope);
        }

        entity.isActivated = false;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityScript) world.getTileEntity(x, y, z);
            AC_GuiScript.showUI(entity);
        }
        return true;
    }
}
