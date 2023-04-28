package dev.adventurecraft.awakening.extension.entity.ai.pathing;

import net.minecraft.entity.ai.pathing.EntityPath;
import net.minecraft.entity.ai.pathing.PathNode;

public interface ExPathNodeNavigator {

    EntityPath simplifyPath(EntityPath var1, PathNode var2);
}
