package dev.adventurecraft.awakening.extension.entity.ai.pathing;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;

public interface ExEntityPath {

    boolean needNewPath(Entity var1);

    PathFinder getNavigator();

    void setNavigator(PathFinder value);

    Node getClearSize();

    void setClearSize(Node value);
}
