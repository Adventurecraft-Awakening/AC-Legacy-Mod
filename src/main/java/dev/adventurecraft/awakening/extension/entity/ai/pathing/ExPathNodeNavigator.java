package dev.adventurecraft.awakening.extension.entity.ai.pathing;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public interface ExPathNodeNavigator {

    Path simplifyPath(Path path, Node var2);
}
