package dev.adventurecraft.awakening.extension.nbt;

import net.minecraft.nbt.ListTag;

import java.util.List;

public interface ExListTag extends ExTag {

    void setInnerList(List<?> list);

    static ListTag wrap(List<?> list) {
        var tag = new ListTag();
        ((ExListTag) tag).setInnerList(list);
        return tag;
    }
}
