package dev.adventurecraft.awakening.world.history;

import dev.adventurecraft.awakening.world.BlockPos;
import dev.adventurecraft.awakening.world.BlockRegion;
import net.minecraft.world.level.Level;

public final class AC_RegionEditAction implements AC_EditAction {

    private final BlockPos origin;
    private final BlockRegion prevRegion;
    private final BlockRegion nextRegion;

    public AC_RegionEditAction(BlockPos origin, BlockRegion prevRegion, BlockRegion nextRegion) {
        this.origin = origin;
        this.prevRegion = prevRegion;
        this.nextRegion = nextRegion;
    }

    @Override
    public void undo(Level level) {
        this.set(level, this.origin, this.prevRegion);
    }

    @Override
    public void redo(Level level) {
        this.set(level, this.origin, this.nextRegion);
    }

    private void set(Level level, BlockPos origin, BlockRegion region) {
        BlockPos end = origin.add(BlockPos.sub(region.getSize(), BlockPos.ONE));
        region.writeBlocks(level, origin, end);
        region.updateBlocks(level, origin, end);
    }
}
