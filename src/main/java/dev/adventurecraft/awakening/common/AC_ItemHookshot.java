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

    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        boolean swapped;
        AC_EntityHookshot mainHook;
        AC_EntityHookshot offHook;
        if (!((ExPlayerEntity) player).areSwappedItems()) {
            mainHook = this.mainHookshot;
            offHook = this.offHookshot;
            swapped = true;
        } else {
            mainHook = this.offHookshot;
            offHook = this.mainHookshot;
            swapped = false;
        }

        if ((mainHook == null || mainHook.removed) && (offHook != null && offHook.attachedToSurface || player.onGround || player.method_1335() || player.method_1393())) {
            mainHook = new AC_EntityHookshot(world, player, swapped, stack);
            world.spawnEntity(mainHook);
            player.swingHand();
            if (swapped) {
                this.mainActiveHookshot = stack;
                this.mainActiveHookshot.setMeta(1);
            } else {
                this.offActiveHookshot = stack;
                this.offActiveHookshot.setMeta(1);
            }

            this.player = player;
        } else {
            this.releaseHookshot(mainHook);
        }

        if (swapped) {
            this.mainHookshot = mainHook;
        } else {
            this.offHookshot = mainHook;
        }

        return stack;
    }

    public void releaseHookshot(AC_EntityHookshot entity) {
        if (entity != null) {
            entity.turningAround = true;
            entity.attachedToSurface = false;
            entity.entityGrabbed = null;
            if (entity == this.mainHookshot && this.mainActiveHookshot != null) {
                this.mainActiveHookshot.setMeta(0);
                this.mainActiveHookshot = null;
            } else if (this.offActiveHookshot != null) {
                this.offActiveHookshot.setMeta(0);
                this.offActiveHookshot = null;
            }
        }
    }
}
