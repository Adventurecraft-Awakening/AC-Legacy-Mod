package dev.adventurecraft.awakening.extension.entity;

import dev.adventurecraft.awakening.entity.AC_IMultiAttackEntity;
import dev.adventurecraft.awakening.extension.world.entity.EntityClass;
import net.minecraft.world.phys.Vec3;

public interface ExEntity extends AC_IMultiAttackEntity, EntityClass {

    void setCanGetFallDamage(boolean arg);

    boolean getCanGetFallDamage();

    Vec3 getPosition();

    Vec3 getRotation(float deltaTime);

    void setRotation(double x, double y, double z);

    boolean getIsFlying();

    void setIsFlying(boolean value);

    float getFlightSpeed();

    void setFlightSpeed(float value);

    boolean getNoPhysics();

    void setNoPhysics(boolean value);

    boolean getCollidesWithClipBlocks();

    void setCollidesWithClipBlocks(boolean value);

    int getStunned();

    void setStunned(int value);

    int getCollisionX();

    int getCollisionZ();

    void setIgnoreCobwebCollision(boolean value);

    boolean isIgnoreCobwebCollision();

    boolean hasTag(String key);

    Object getTag(String key);

    Object setTag(String key, Object value);

    void setRotUnwrapped(float yRot, float xRot);
}
