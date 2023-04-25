package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockTriggeredDoor extends Block {
    protected AC_BlockTriggeredDoor(int var1) {
        super(var1, Material.WOOD);
        this.texture = 208;
    }

    public boolean isFullOpaque() {
        return false;
    }

    public boolean isCollidable() {
        return AC_DebugMode.active;
    }

    public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
        return AC_DebugMode.active || ((ExWorld) Minecraft.instance.world).getTriggerManager().isActivated(var2, var3, var4);
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        if (((ExWorld) var1).getTriggerManager().isActivated(var2, var3, var4) && !AC_DebugMode.active) {
            return super.getCollisionShape(var1, var2, var3, var4);
        }
        return null;
    }

    public boolean canBeTriggered() {
        return true;
    }

    public void onTriggerActivated(World var1, int var2, int var3, int var4) {
        var1.playSound((double) var2 + 0.5D, (double) var3 + 0.5D, (double) var4 + 0.5D, "random.door_open", 1.0F, var1.rand.nextFloat() * 0.1F + 0.9F);
        var1.notifyListeners(var2, var3, var4);
    }

    public void onTriggerDeactivated(World var1, int var2, int var3, int var4) {
        var1.playSound((double) var2 + 0.5D, (double) var3 + 0.5D, (double) var4 + 0.5D, "random.door_close", 1.0F, var1.rand.nextFloat() * 0.1F + 0.9F);
        var1.notifyListeners(var2, var3, var4);
    }
}
