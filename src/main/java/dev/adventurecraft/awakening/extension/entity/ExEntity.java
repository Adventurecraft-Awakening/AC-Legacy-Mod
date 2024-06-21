package dev.adventurecraft.awakening.extension.entity;

import dev.adventurecraft.awakening.common.AC_IMultiAttackEntity;
import net.minecraft.util.math.Vec3d;

public interface ExEntity extends AC_IMultiAttackEntity {

    Vec3d getRotation(float deltaTime);

    void setRotation(double x, double y, double z);

    boolean handleFlying();

    void setIsFlying(boolean value);

    boolean getCollidesWithClipBlocks();

    void setCollidesWithClipBlocks(boolean value);

    int getStunned();

    void setStunned(int value);

    int getCollisionX();

    int getCollisionZ();

    void setIgnoreCobwebCollision(boolean value);

    boolean isIgnoreCobwebCollision();
}
