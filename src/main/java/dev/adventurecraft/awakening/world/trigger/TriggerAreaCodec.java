package dev.adventurecraft.awakening.world.trigger;

import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.world.BlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import java.util.Map;

/** The existing map NBT format, shared by trigger saving and loading. */
public final class TriggerAreaCodec {
    private TriggerAreaCodec() { }

    public static CompoundTag write(Map<BlockPos, Int2ObjectOpenHashMap<AC_TriggerArea>> sources) {
        var result = new CompoundTag();
        int coordinate = 0;
        for (var source : sources.entrySet()) {
            var position = new CompoundTag();
            position.putInt("x", source.getKey().x());
            position.putInt("y", source.getKey().y());
            position.putInt("z", source.getKey().z());
            int index = 0;
            for (var entry : source.getValue().int2ObjectEntrySet()) {
                var area = entry.getValue().getTagCompound();
                area.putInt("areaID", entry.getIntKey());
                position.putTag("area" + index++, area);
            }
            position.putInt("numAreas", index);
            result.putTag("coord" + coordinate++, position);
        }
        result.putInt("numCoords", coordinate);
        return result;
    }

    public static void read(CompoundTag source, Map<BlockPos, Int2ObjectOpenHashMap<AC_TriggerArea>> target) {
        target.clear();
        for (int coordinate = 0; coordinate < source.getInt("numCoords"); coordinate++) {
            var position = source.getCompoundTag("coord" + coordinate);
            var key = new BlockPos(position.getInt("x"), position.getInt("y"), position.getInt("z"));
            var areas = new Int2ObjectOpenHashMap<AC_TriggerArea>();
            for (int index = 0; index < position.getInt("numAreas"); index++) {
                var area = position.getCompoundTag("area" + index);
                areas.put(area.getInt("areaID"), AC_TriggerArea.getFromTagCompound(area));
            }
            target.put(key, areas);
        }
    }
}
