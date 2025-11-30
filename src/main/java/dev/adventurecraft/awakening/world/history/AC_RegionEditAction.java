package dev.adventurecraft.awakening.world.history;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.world.BlockRegion;
import net.minecraft.world.level.Level;

public final class AC_RegionEditAction implements AC_EditAction {

    private final Coord origin;
    private final BlockRegion prevRegion;
    private final BlockRegion nextRegion;

    public AC_RegionEditAction(Coord origin, BlockRegion prevRegion, BlockRegion nextRegion) {
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

    private void set(Level level, Coord origin, BlockRegion region) {
        Coord end = origin.add(region.getSize().sub(Coord.one));
        region.writeBlocks(level, origin, end);
        region.updateBlocks(level, origin, end);
    }
}
