package dev.adventurecraft.awakening.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.client.SingleplayerInteractionManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SingleplayerInteractionManager.class)
public abstract class MixinSingleplayerInteractionManager extends MixinClientInteractionManager {

    @Overwrite
    public boolean breakBlock(int x, int y, int z, int side) {
        AC_UndoStack undoStack = ((ExWorld) this.client.world).getUndoStack();
        undoStack.startRecording();
        boolean broken = false;

        int destroyWidth = getDestroyExtraWidth();
        int destroyDepth = getDestroyExtraDepth();

        for (int w = -destroyWidth; w <= destroyWidth; ++w) {
            for (int h = -destroyWidth; h <= destroyWidth; ++h) {
                for (int d = 0; d <= destroyDepth; ++d) {
                    if (side == 0) {
                        broken |= this._sendBlockRemoved(x + w, y + d, z + h, side);
                    } else if (side == 1) {
                        broken |= this._sendBlockRemoved(x + w, y - d, z + h, side);
                    } else if (side == 2) {
                        broken |= this._sendBlockRemoved(x + w, y + h, z + d, side);
                    } else if (side == 3) {
                        broken |= this._sendBlockRemoved(x + w, y + h, z - d, side);
                    } else if (side == 4) {
                        broken |= this._sendBlockRemoved(x + d, y + h, z + w, side);
                    } else if (side == 5) {
                        broken |= this._sendBlockRemoved(x - d, y + h, z + w, side);
                    }
                }
            }
        }

        undoStack.stopRecording();
        return broken;
    }

    private boolean _sendBlockRemoved(int x, int y, int z, int var4) {
        int id = this.client.world.getBlockId(x, y, z);
        if (id == 0) {
            return false;
        }

        int meta = this.client.world.getBlockMeta(x, y, z);
        boolean broken = super.breakBlock(x, y, z, var4);
        ItemStack heldItem = this.client.player.getHeldItem();
        boolean canBreak = this.client.player.canRemoveBlock(Block.BY_ID[id]);
        if (heldItem != null) {
            heldItem.postMine(id, x, y, z, this.client.player);
            if (heldItem.count == 0) {
                heldItem.unusedEmptyMethod1(this.client.player);
                this.client.player.breakHeldItem();
            }
        }

        if (broken && canBreak) {
            Block.BY_ID[id].afterBreak(this.client.world, this.client.player, x, y, z, meta);
        }

        return broken;
    }

    @WrapWithCondition(
        method = "destroyFireAndBreakBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/SingleplayerInteractionManager;breakBlock(IIII)Z"))
    private boolean onlyBreakBlockInDebugMode(SingleplayerInteractionManager instance, int var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    @Inject(method = "dig", at = @At("HEAD"), cancellable = true)
    private void onlyDigInDebugMode(int var1, int var2, int var3, int var4, CallbackInfo ci) {
        if (!AC_DebugMode.active) {
            ci.cancel();
        }
    }

    @Overwrite
    public float getBlockReachDistance() {
        if (this.client.player.getHeldItem() != null && this.client.player.getHeldItem().itemId == AC_Items.quill.id) {
            return 500.0F;
        }
        if (AC_DebugMode.active) {
            return (float) AC_DebugMode.reachDistance;
        }
        return 4.0F;
    }
}
