package dev.adventurecraft.awakening.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.item.AC_Items;
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
    public boolean destroyBlock(int x, int y, int z, int side) {
        AC_UndoStack undoStack = ((ExWorld) this.minecraft.level).getUndoStack();
        boolean hasRecording = undoStack.startRecording();
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
        int id = this.minecraft.level.getTile(x, y, z);
        if (id == 0) {
            return false;
        }

        int meta = this.minecraft.level.getData(x, y, z);
        boolean broken = super.destroyBlock(x, y, z, side);
        ItemInstance heldItem = this.minecraft.player.getSelectedItem();
        boolean canBreak = this.minecraft.player.canDestroy(Tile.tiles[id]);
        if (heldItem != null) {
            heldItem.mineBlock(id, x, y, z, this.minecraft.player);
            if (heldItem.count == 0) {
                heldItem.snap(this.minecraft.player);
                this.minecraft.player.removeSelectedItem();
            }
        }

        if (broken && canBreak) {
            Tile.tiles[id].playerDestroy(this.minecraft.level, this.minecraft.player, x, y, z, meta);
        }

        return broken;
    }

    @WrapWithCondition(
        method = "startDestroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gamemode/SurvivalGameMode;destroyBlock(IIII)Z"))
    private boolean onlyBreakBlockInDebugMode(SurvivalGameMode instance, int var1, int var2, int var3, int var4) {
        return AC_DebugMode.active;
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onlyDigInDebugMode(int var1, int var2, int var3, int var4, CallbackInfo ci) {
        if (!AC_DebugMode.active) {
            ci.cancel();
        }
    }

    @Overwrite
    public float getPickRange() {
        ItemInstance heldItem = this.minecraft.player.getSelectedItem();
        if (heldItem != null && heldItem.id == AC_Items.quill.id) {
            return 500.0F;
        }
        if (AC_DebugMode.active) {
            return (float) AC_DebugMode.reachDistance;
        }
        return 4.0F;
    }
}
