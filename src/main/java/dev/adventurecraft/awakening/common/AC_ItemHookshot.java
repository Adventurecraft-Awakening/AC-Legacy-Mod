package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AC_ItemHookshot extends Item {

    public AC_EntityHookshot mainHookshot = null;
    public AC_EntityHookshot offHookshot = null;
    public ItemInstance mainActiveHookshot = null;
    public ItemInstance offActiveHookshot = null;
    public Player player = null;

    public AC_ItemHookshot(int var1) {
        super(var1);
        this.setIcon(3, 10);
        this.maxStackSize = 1;
    }

    /**
     * Removes the existing hookshots and references to them.
     * NOTE: Should this information be saved in the player entity instead for multiplayer purposes or future proofing?
     *       Maybe as an implemented interface for extra cleanliness?
     */
    public void resetPlayerHookshotState() {
        if (mainHookshot != null) {
            mainHookshot.remove();
            mainHookshot = null;
        }
        if (offHookshot != null) {
            offHookshot.remove();
            offHookshot = null;
        }
    }

    public boolean isMirroredArt() {
        return true;
    }

    public int getIcon(int var1) {
        return var1 == 1 ? this.texture + 1 : this.texture;
    }

    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        boolean onMainHand;
        AC_EntityHookshot mainHook;
        AC_EntityHookshot offHook;
        if (!((ExPlayerEntity) player).areSwappedItems()) {
            mainHook = this.mainHookshot;
            offHook = this.offHookshot;
            onMainHand = true;
        } else {
            mainHook = this.offHookshot;
            offHook = this.mainHookshot;
            onMainHand = false;
        }

        // The hook exists if there is a reference, it is not removed, and it is on the current world.
        boolean mainHookExists = !(mainHook == null || mainHook.removed || mainHook.level != world);
        boolean offHookExists = !(offHook == null || offHook.removed || offHook.level != world);
        boolean playerIsSwimming = player.isInLava() || player.checkInWater(); // In lava and water respectively.
        boolean playerInValidTerrain = (offHookExists && offHook.attachedToSurface) || player.onGround || playerIsSwimming;

        if (!mainHookExists && playerInValidTerrain) {
            mainHook = new AC_EntityHookshot(world, player, onMainHand, stack);
            world.addEntity(mainHook);
            player.swing();
            if (onMainHand) {
                this.mainActiveHookshot = stack;
                this.mainActiveHookshot.setDamage(1);
            } else {
                this.offActiveHookshot = stack;
                this.offActiveHookshot.setDamage(1);
            }

            this.player = player;
        } else {
            this.releaseHookshot(mainHook);
        }

        if (onMainHand) {
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
                this.mainActiveHookshot.setDamage(0);
                this.mainActiveHookshot = null;
            } else if (this.offActiveHookshot != null) {
                this.offActiveHookshot.setDamage(0);
                this.offActiveHookshot = null;
            }
        }
    }
}
