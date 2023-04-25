package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemHookshot extends Item {
    public AC_EntityHookshot mainHookshot = null;
    public AC_EntityHookshot offHookshot = null;
    public ItemStack mainActiveHookshot = null;
    public ItemStack offActiveHookshot = null;
    public PlayerEntity player = null;

    public AC_ItemHookshot(int var1) {
        super(var1);
        this.setTexturePosition(3, 10);
        this.maxStackSize = 1;
    }

    public boolean shouldSpinWhenRendering() {
        return true;
    }

    public int getTexturePosition(int var1) {
        return var1 == 1 ? this.texturePosition + 1 : this.texturePosition;
    }

    public ItemStack use(ItemStack var1, World var2, PlayerEntity var3) {
        boolean var4 = true;
        AC_EntityHookshot var5;
        AC_EntityHookshot var6;
        if (!((ExPlayerEntity) var3).areSwappedItems()) {
            var5 = this.mainHookshot;
            var6 = this.offHookshot;
        } else {
            var5 = this.offHookshot;
            var6 = this.mainHookshot;
            var4 = false;
        }

        if ((var5 == null || var5.removed) && (var6 != null && var6.attachedToSurface || var3.onGround || var3.method_1335() || var3.method_1393())) {
            var5 = new AC_EntityHookshot(var2, var3, var4, var1);
            var2.spawnEntity(var5);
            var3.swingHand();
            if (var4) {
                this.mainActiveHookshot = var1;
                this.mainActiveHookshot.setMeta(1);
            } else {
                this.offActiveHookshot = var1;
                this.offActiveHookshot.setMeta(1);
            }

            this.player = var3;
        } else {
            this.releaseHookshot(var5);
        }

        if (var4) {
            this.mainHookshot = var5;
        } else {
            this.offHookshot = var5;
        }

        return var1;
    }

    public void releaseHookshot(AC_EntityHookshot var1) {
        if (var1 != null) {
            var1.turningAround = true;
            var1.attachedToSurface = false;
            var1.entityGrabbed = null;
            if (var1 == this.mainHookshot && this.mainActiveHookshot != null) {
                this.mainActiveHookshot.setMeta(0);
                this.mainActiveHookshot = null;
            } else if (this.offActiveHookshot != null) {
                this.offActiveHookshot.setMeta(0);
                this.offActiveHookshot = null;
            }
        }
    }
}
