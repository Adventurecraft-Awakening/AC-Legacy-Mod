package dev.adventurecraft.awakening.extension.entity.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;

public interface ExEntityPath {

    boolean needNewPath(Entity var1);

    PathNodeNavigator getNavigator();

    void setNavigator(PathNodeNavigator value);

    PathNode getClearSize();

    void setClearSize(PathNode value);
}
