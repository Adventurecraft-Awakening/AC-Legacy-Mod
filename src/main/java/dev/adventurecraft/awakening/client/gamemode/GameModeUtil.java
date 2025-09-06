package dev.adventurecraft.awakening.client.gamemode;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.AC_UndoStack;
import dev.adventurecraft.awakening.extension.client.gamemode.ExGameMode;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_Items;
import net.minecraft.client.gamemode.GameMode;
import net.minecraft.world.ItemInstance;

import java.util.Optional;

public final class GameModeUtil {

    public static Optional<Float> getPickRange(GameMode self) {
        ItemInstance heldItem = ((ExGameMode) self).getMinecraft().player.getSelectedItem();
        if (heldItem != null && heldItem.id == AC_Items.quill.id) {
            return Optional.of(500.0F);
        }
        if (AC_DebugMode.isActive()) {
            return Optional.of((float) AC_DebugMode.reachDistance);
        }
        return Optional.empty();
    }

    public static boolean destroyBlocks(GameMode self, int x, int y, int z, int face, BlockBreaker breaker) {
        if (!AC_DebugMode.isActive()) {
            return false;
        }

        var exSelf = (ExGameMode) self;
        AC_UndoStack undoStack = ((ExWorld) exSelf.getMinecraft().level).getUndoStack();
        boolean hasRecording = undoStack.startRecording();
        boolean broken = false;

        int destroyWidth = exSelf.getDestroyExtraWidth();
        int destroyDepth = exSelf.getDestroyExtraDepth();

        for (int w = -destroyWidth; w <= destroyWidth; ++w) {
            for (int h = -destroyWidth; h <= destroyWidth; ++h) {
                for (int d = 0; d <= destroyDepth; ++d) {
                    if (face == 0) {
                        broken |= breaker.destroyBlock(x + w, y + d, z + h, face);
                    }
                    else if (face == 1) {
                        broken |= breaker.destroyBlock(x + w, y - d, z + h, face);
                    }
                    else if (face == 2) {
                        broken |= breaker.destroyBlock(x + w, y + h, z + d, face);
                    }
                    else if (face == 3) {
                        broken |= breaker.destroyBlock(x + w, y + h, z - d, face);
                    }
                    else if (face == 4) {
                        broken |= breaker.destroyBlock(x + d, y + h, z + w, face);
                    }
                    else if (face == 5) {
                        broken |= breaker.destroyBlock(x - d, y + h, z + w, face);
                    }
                }
            }
        }

        if (hasRecording) {
            undoStack.stopRecording();
        }
        return broken;
    }

    @FunctionalInterface
    public interface BlockBreaker {
        boolean destroyBlock(int x, int y, int z, int face);
    }
}
