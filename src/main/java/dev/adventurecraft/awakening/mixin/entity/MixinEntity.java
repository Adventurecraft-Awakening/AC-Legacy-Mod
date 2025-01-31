package dev.adventurecraft.awakening.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.util.HashCode;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class MixinEntity implements ExEntity {

    @Shadow
    public int id;
    @Shadow
    public Entity rider;
    @Shadow
    public Entity riding;
    @Shadow
    public Level level;
    @Shadow
    public double xo;
    @Shadow
    public double yo;
    @Shadow
    public double zo;
    @Shadow
    public double x;
    @Shadow
    public double y;
    @Shadow
    public double z;
    @Shadow
    public double xd;
    @Shadow
    public double yd;
    @Shadow
    public double zd;
    @Shadow
    public float yRot;
    @Shadow
    public float xRot;
    @Shadow
    public float yRotO;
    @Shadow
    public float xRotO;
    @Final
    @Shadow
    public AABB bb;
    @Shadow
    public boolean onGround;
    @Shadow
    public boolean horizontalCollision;
    @Shadow
    public boolean stuckInBlock;
    @Shadow
    public boolean removed;
    @Shadow
    public float heightOffset;
    @Shadow
    public float bbWidth;
    @Shadow
    public float bbHeight;
    @Shadow
    public float walkDist;
    @Shadow
    public boolean noPhysics;
    @Shadow
    public float fallDistance;
    @Shadow
    public int nextStep;
    @Shadow
    protected Random random;
    @Shadow
    public int flameTime;
    @Shadow
    public int onFire;
    @Shadow
    public int invulnerableTime;

    public boolean ignoreCobwebCollision = false;
    public boolean isFlying;
    public int stunned;
    public boolean collidesWithClipBlocks = true;
    public int collisionX;
    public int collisionZ;
    public float moveYawOffset = 0.0F;

    private int cachedBrightnessKey = -1;
    private float cachedBrightness;

    protected Map<String, String> customData = new HashMap<>();

    @Shadow
    protected abstract void causeFallDamage(float f);

    @Shadow
    public abstract void push(double d, double e, double f);

    @Shadow
    public abstract boolean hurt(Entity arg, int i);

    @Shadow
    public void tick() {
        throw new AssertionError();
    }

    @Shadow
    public void remove() {
        throw new AssertionError();
    }

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    public abstract void setPos(double d, double e, double f);

    @Shadow
    protected abstract void setSize(float f, float g);

    @Shadow
    public abstract void move(double d, double e, double f);

    @Inject(method = "move", at = @At(value = "HEAD"))
    private void collideWithCobweb(CallbackInfo ci) {
        if (this.stuckInBlock && isIgnoreCobwebCollision()) {
            this.stuckInBlock = false;
        }
    }

    @Shadow
    public abstract ItemEntity spawnAtLocation(int i, int j);

    @Shadow
    public abstract boolean isInLava();

    @Shadow
    protected abstract void markHurt();

    @Shadow
    public abstract boolean isFree(double d, double e, double f);

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract float distanceTo(Entity arg);

    @Inject(method = "move", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/entity/Entity;z:D",
        shift = At.Shift.AFTER,
        ordinal = 2))
    private void collideInMove(
        double var1,
        double var2,
        double var3,
        CallbackInfo ci,
        @Local(argsOnly = true, ordinal = 2) double var5,
        @Local(ordinal = 5) double var11,
        @Local(ordinal = 6) double var13,
        @Local(ordinal = 7) double var15) {
        this.collisionX = Double.compare(var11, var1);

        int yPos;
        boolean isCollidingWithBlock = false;
        int blockId;
        if (this.collisionX != 0) {
            for (yPos = 0; (double) yPos < (double) this.bbHeight + this.y - (double) this.heightOffset - Math.floor(this.y - (double) this.heightOffset); ++yPos) {
                blockId = this.level.getTile((int) Math.floor(this.x) + this.collisionX, (int) Math.floor(this.y + (double) yPos - (double) this.heightOffset), (int) Math.floor(this.z));
                if (blockId != 0 && blockId != AC_Blocks.clipBlock.id) {
                    isCollidingWithBlock = true;
                    break;
                }
            }

            if (!isCollidingWithBlock) {
                this.collisionX = 0;
            }
        }

        this.collisionZ = Double.compare(var15, var5);

        if (this.collisionZ != 0) {
            isCollidingWithBlock = false;

            for (yPos = 0; (double) yPos < (double) this.bbHeight + this.y - this.heightOffset - Math.floor(this.y - (double) this.heightOffset); ++yPos) {
                blockId = this.level.getTile((int) Math.floor(this.x), (int) Math.floor(this.y + (double) yPos - (double) this.heightOffset), (int) Math.floor(this.z) + this.collisionZ);
                if (blockId != 0 && blockId != AC_Blocks.clipBlock.id) {
                    isCollidingWithBlock = true;
                    break;
                }
            }

            if (!isCollidingWithBlock) {
                this.collisionZ = 0;
            }
        }
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;nextStep:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0))
    private void moveCeil(Entity instance, int value) {
        instance.nextStep = (int) ((double) this.nextStep + Math.ceil((this.walkDist - (float) this.nextStep)));
    }

    @Overwrite
    public void checkFallDamage(double var1, boolean var3) {
        if (var3) {
            if (this.yd < 0.0D) {
                this.causeFallDamage(-((float) this.yd));
            }
        } else if (var1 < 0.0D) {
            this.fallDistance = (float) ((double) this.fallDistance - var1);
        }
    }

    @Overwrite
    public void moveRelative(float x, float z, float speed) {
        float inputSqr = x * x + z * z;
        if (inputSqr >= 1.0E-4F) {
            x *= speed;
            z *= speed;
            double sin = Math.sin((this.yRot + this.moveYawOffset) * Math.PI / 180.0D);
            double cos = Math.cos((this.yRot + this.moveYawOffset) * Math.PI / 180.0D);
            this.xd += x * cos - z * sin;
            this.zd += z * cos + x * sin;
        }
    }

    // Level.getBrightness returns the default value of an empty chunk for missing chunks.
    // The hasChunksAt check almost always returns true anyway.
    @Redirect(
        method = "getBrightness",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;hasChunksAt(IIIIII)Z"))
    private boolean alwaysGetBrightness(Level instance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return true;
    }

    @Redirect(
        method = "getBrightness",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBrightness(III)F"))
    private float cacheGetBrightness(Level instance, int x, int y, int z) {
        int key = HashCode.combine(x, y, z);
        if (this.cachedBrightnessKey != key) {
            this.cachedBrightnessKey = key;
            // Simple hashcode saves a lot of time for entities that move slowly,
            // especially relevant for particles.
            this.cachedBrightness = instance.getBrightness(x, y, z);
        }
        return this.cachedBrightness;
    }

    @Redirect(
        method = "push(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(DDD)V",
            ordinal = 1))
    private void reverseAccelerateDir(Entity instance, double var2, double var3, double var4) {
        if (instance.isPushable()) {
            instance.push(var2, var3, var4);
        } else {
            this.push(-var2, var3, -var4);
        }
    }

    @Redirect(method = "isInWall", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;isSolidBlockingTile(III)Z"))
    private boolean preventSuffocate(Level instance, int x, int y, int z) {
        return instance.isSolidBlockingTile(x, y, z) && instance.isSolidTile(x, y, z);
    }

    @Overwrite
    public Vec3 getLookAngle() {
        return this.getRotation(1.0f);
    }

    @Override
    public Vec3 getRotation(float deltaTime) {
        double pitch = this.xRotO + (this.xRot - this.xRotO) * deltaTime;
        double yaw = this.yRotO + (this.yRot - this.yRotO) * deltaTime;
        double yCos = Math.cos(-yaw * (Math.PI / 180) - Math.PI);
        double ySin = Math.sin(-yaw * (Math.PI / 180) - Math.PI);
        double pCos = -Math.cos(-pitch * (Math.PI / 180));
        double pSin = Math.sin(-pitch * (Math.PI / 180));
        return Vec3.newTemp(ySin * pCos, pSin, yCos * pCos);
    }

    @Override
    public void setRotation(double x, double y, double z) {
        double root = Math.sqrt(x * x + z * z);
        double yDeg = (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0;
        double pDeg = -(Math.atan2(y, root) * 180.0 / Math.PI);
        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
        this.xRot = (float) pDeg;
        this.yRot = (float) yDeg;
    }

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        return this.hurt(entity, damage);
    }

    @Override
    public boolean handleFlying() {
        return this.isFlying;
    }

    @Override
    public void setIsFlying(boolean value) {
        this.isFlying = value;
    }

    @Override
    public boolean getCollidesWithClipBlocks() {
        return this.collidesWithClipBlocks;
    }

    @Override
    public void setCollidesWithClipBlocks(boolean value) {
        this.collidesWithClipBlocks = value;
    }

    @Override
    public int getStunned() {
        return this.stunned;
    }

    @Override
    public void setStunned(int value) {
        this.stunned = value;
    }

    @Override
    public int getCollisionX() {
        return this.collisionX;
    }

    @Override
    public int getCollisionZ() {
        return this.collisionZ;
    }

    @Override
    public void setIgnoreCobwebCollision(boolean value) {
        this.ignoreCobwebCollision = value;
    }

    @Override
    public boolean isIgnoreCobwebCollision() {
        return ignoreCobwebCollision;
    }

    @Override
    public void setCustomTagString(String key, String value) {
        this.customData.put(key, value);
    }

    @Override
    public boolean hasCustomTagString(String key) {
        return this.customData.containsKey(key);
    }

    @Override
    public String getOrCreateCustomTagString(String key, String defaultValue) {
        if (this.customData.containsKey(key)) {
            return this.customData.get(key);
        }
        this.customData.put(key, defaultValue);
        return defaultValue;
    }

    @Override
    public String getCustomTagString(String key) {
        return customData.get(key);
    }
}
