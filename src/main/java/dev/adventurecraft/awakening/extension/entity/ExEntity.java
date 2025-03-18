package dev.adventurecraft.awakening.extension.entity;

import dev.adventurecraft.awakening.entity.AC_IMultiAttackEntity;
import net.minecraft.world.phys.Vec3;

public interface ExEntity extends AC_IMultiAttackEntity {

    void setCanGetFallDamage(boolean arg);

    boolean getCanGetFallDamage();

    Vec3 getRotation(float deltaTime);

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

    void setCustomTagString(String key,String value);

    boolean hasCustomTagString(String key);

    String getOrCreateCustomTagString(String key,String defaultValue);

    String getCustomTagString(String key);


}
