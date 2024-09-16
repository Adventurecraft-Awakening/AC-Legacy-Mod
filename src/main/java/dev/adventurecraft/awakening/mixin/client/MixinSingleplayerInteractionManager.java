package dev.adventurecraft.awakening.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.gamemode.SurvivalGameMode;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SurvivalGameMode.class)
public abstract class MixinSingleplayerInteractionManager extends MixinClientInteractionManager {

    @Overwrite
    public boolean breakBlock(int x, int y, int z, int side) {
        AC_UndoStack undoStack = ((ExWorld) this.client.level).getUndoStack();
        boolean hasRecording = false;
        if (!undoStack.isRecording()) {
            undoStack.startRecording();
            hasRecording = true;
        }
        boolean broken = false;

        int destroyWidth = this.getDestroyExtraWidth();
        int destroyDepth = this.getDestroyExtraDepth();

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

        if (hasRecording) {
            undoStack.stopRecording();
        }
        return broken;
    }

    private boolean _sendBlockRemoved(int x, int y, int z, int side) {
        int id = this.client.level.getTile(x, y, z);
        if (id == 0) {
            return false;
        }

        int meta = this.client.level.getData(x, y, z);
        boolean broken = super.breakBlock(x, y, z, side);
        ItemInstance heldItem = this.client.player.getSelectedItem();
        boolean canBreak = this.client.player.canDestroy(Tile.tiles[id]);
        if (heldItem != null) {
            heldItem.mineBlock(id, x, y, z, this.client.player);
            if (heldItem.count == 0) {
                heldItem.snap(this.client.player);
                this.client.player.removeSelectedItem();
            }
        }

        if (broken && canBreak) {
            Tile.tiles[id].playerDestroy(this.client.level, this.client.player, x, y, z, meta);
        }

        return broken;
    }

    @WrapWithCondition(
        method = "destroyFireAndBreakBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/SingleplayerInteractionManager;breakBlock(IIII)Z"))
    private boolean onlyBreakBlockInDebugMode(SurvivalGameMode instance, int var1, int var2, int var3, int var4) {
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
        ItemInstance heldItem = this.client.player.getSelectedItem();
        if (heldItem != null && heldItem.id == AC_Items.quill.id) {
            return 500.0F;
        }
        if (AC_DebugMode.active) {
            return (float) AC_DebugMode.reachDistance;
        }
        return 4.0F;
    }
}
