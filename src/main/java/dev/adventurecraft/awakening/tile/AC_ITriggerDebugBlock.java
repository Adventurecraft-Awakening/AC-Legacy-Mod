package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.editor.EditorSession;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.world.level.LevelSource;

public interface AC_ITriggerDebugBlock extends AC_ITriggerBlock {

    default @Override int getRenderShape(LevelSource view, int x, int y, int z) {
        if (EditorSession.current().active) {
            return AC_ITriggerBlock.super.getRenderShape(view, x, y, z);
        }
        return BlockShapes.NONE;
    }

    default @Override boolean canBeTriggered() {
        return true;
    }
}
