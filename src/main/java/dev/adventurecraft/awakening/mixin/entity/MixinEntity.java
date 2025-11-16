package dev.adventurecraft.awakening.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.nbt.ExListTag;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.util.RandomUtil;
import dev.adventurecraft.awakening.util.TagUtil;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

@Mixin(Entity.class)
public abstract class MixinEntity implements ExEntity {

    @Shadow public int id;
    @Shadow public Entity rider;
    @Shadow public Entity riding;
    @Shadow public Level level;
    @Shadow public double xo;
    @Shadow public double yo;
    @Shadow public double zo;
    @Shadow public double x;
    @Shadow public double y;
    @Shadow public double z;
    @Shadow public double xd;
    @Shadow public double yd;
    @Shadow public double zd;
    @Shadow public float yRot;
    @Shadow public float xRot;
    @Shadow public float yRotO;
    @Shadow public float xRotO;
    @Shadow @Final public AABB bb;
    @Shadow public boolean onGround;
    @Shadow public boolean horizontalCollision;
    @Shadow public boolean stuckInBlock;
    @Shadow public boolean removed;
    @Shadow public float heightOffset;
    @Shadow public float bbWidth;
    @Shadow public float bbHeight;
    @Shadow public float walkDist;
    @Shadow public boolean noPhysics;
    @Shadow public float fallDistance;
    @Shadow public int nextStep;
    @Shadow protected Random random;
    @Shadow public int flameTime;
    @Shadow public int onFire;
    @Shadow public int invulnerableTime;

    @Unique public boolean ignoreCobwebCollision = false;
    @Unique public boolean isFlying;
    @Unique public int stunned;
    @Unique public boolean collidesWithClipBlocks = true;
    @Unique public int collisionX;
    @Unique public int collisionZ;
    @Unique public float moveYawOffset = 0.0F;

    @Unique private int cachedBrightnessKey = -1;
    @Unique private float cachedBrightness;

    // TODO: move into SynchedEntityData? String key makes that difficult...
    @Unique private Map<String, Object> customData;

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

    @Inject(
        method = "move",
        at = @At(value = "HEAD")
    )
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

    @Shadow
    public abstract float getBrightness(float partialTick);

    @Inject(
        method = "saveWithoutId",
        at = @At("TAIL")
    )
    protected void ac$saveWithoutId(CompoundTag tag, CallbackInfo ci) {
        var exTag = (ExCompoundTag) tag;
        var customTag = exTag.findCompound("custom");
        if (customTag.isPresent()) {
            var map = this.customData();
            for (Tag tags : (Collection<Tag>) customTag.get().getTags()) {
                map.put(tags.getType(), TagUtil.unwrap(tags));
            }
        }
    }

    @Inject(
        method = "load",
        at = @At("TAIL")
    )
    protected void ac$load(CompoundTag tag, CallbackInfo ci) {
        // Do not use customData() to not unnecessarily init map.
        if (this.customData != null && !this.customData.isEmpty()) {
            var customTag = new CompoundTag();
            this.customData.forEach((key, object) -> {
                customTag.putTag(key, TagUtil.wrap(object));
            });
            tag.putCompoundTag("custom", customTag);
        }
    }

    @Inject(
        method = "move",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;z:D",
            shift = At.Shift.AFTER,
            ordinal = 2
        )
    )
    private void collideInMove(
        double var1,
        double var2,
        double var3,
        CallbackInfo ci,
        @Local(
            argsOnly = true,
            ordinal = 2
        ) double var5,
        @Local(ordinal = 5) double var11,
        @Local(ordinal = 6) double var13,
        @Local(ordinal = 7) double var15
    ) {
        this.collisionX = Double.compare(var11, var1);

        // TODO: handle clipBlock elsewhere (probably in AABB producer)

        int yPos;
        boolean isCollidingWithBlock = false;
        int blockId;
        if (this.collisionX != 0) {
            for (yPos = 0;
                 (double) yPos < (double) this.bbHeight + this.y - (double) this.heightOffset -
                     Math.floor(this.y - (double) this.heightOffset);
                 ++yPos) {
                blockId = this.level.getTile(
                    (int) Math.floor(this.x) + this.collisionX,
                    (int) Math.floor(this.y + (double) yPos - (double) this.heightOffset),
                    (int) Math.floor(this.z)
                );
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

            for (yPos = 0;
                 (double) yPos < (double) this.bbHeight + this.y - this.heightOffset -
                     Math.floor(this.y - (double) this.heightOffset);
                 ++yPos) {
                blockId = this.level.getTile(
                    (int) Math.floor(this.x),
                    (int) Math.floor(this.y + (double) yPos - (double) this.heightOffset),
                    (int) Math.floor(this.z) + this.collisionZ
                );
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
            ordinal = 0
        )
    )
    private void moveCeil(Entity instance, int value) {
        instance.nextStep = (int) ((double) this.nextStep + Math.ceil((this.walkDist - (float) this.nextStep)));
    }

    @Overwrite
    public void checkFallDamage(double var1, boolean var3) {
        if (var3) {
            if (this.yd < 0.0D) {
                this.causeFallDamage(-((float) this.yd));
            }
        }
        else if (var1 < 0.0D) {
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
            target = "Lnet/minecraft/world/level/Level;hasChunksAt(IIIIII)Z"
        )
    )
    private boolean alwaysGetBrightness(Level instance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return true;
    }

    /**
     * Simple hashcode saves a lot of time for entities that move slowly,
     * especially relevant for particles.
     */
    @Redirect(
        method = "getBrightness",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBrightness(III)F"
        )
    )
    private float cacheGetBrightness(Level level, int x, int y, int z) {
        int key = ((ExWorld) level).getLightUpdateHash(x, y, z);
        if (this.cachedBrightnessKey != key) {
            this.cachedBrightnessKey = key; // Store the low bits as variation.
            this.cachedBrightness = level.getBrightness(x, y, z);
        }
        return this.cachedBrightness;
    }

    @Redirect(
        method = "push(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(DDD)V",
            ordinal = 1
        )
    )
    private void reverseAccelerateDir(Entity instance, double var2, double var3, double var4) {
        if (instance.isPushable()) {
            instance.push(var2, var3, var4);
        }
        else {
            this.push(-var2, var3, -var4);
        }
    }

    @Redirect(
        method = "isInWall",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;isSolidBlockingTile(III)Z"
        )
    )
    private boolean preventSuffocate(Level instance, int x, int y, int z) {
        return instance.isSolidBlockingTile(x, y, z) && instance.isSolidTile(x, y, z);
    }

    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public ListTag newDoubleList(double... doubles) {
        return ExListTag.wrap(DoubleArrayList.wrap(doubles));
    }

    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public ListTag newFloatList(float... floats) {
        return ExListTag.wrap(FloatArrayList.wrap(floats));
    }

    @Overwrite
    public Vec3 getLookAngle() {
        return this.getRotation(1.0f);
    }

    @Override
    public Vec3 getRotation(float deltaTime) {
        double pitch = -Math.toRadians(this.xRotO + (this.xRot - this.xRotO) * deltaTime);
        double yaw = -Math.toRadians(this.yRotO + (this.yRot - this.yRotO) * deltaTime);
        double yCos = Math.cos(yaw - Math.PI);
        double ySin = Math.sin(yaw - Math.PI);
        double pCos = -Math.cos(pitch);
        double pSin = Math.sin(pitch);
        return Vec3.newTemp(ySin * pCos, pSin, yCos * pCos);
    }

    @Override
    public void setRotation(double x, double y, double z) {
        double root = Math.sqrt(x * x + z * z);
        double yDeg = Math.toDegrees(Math.atan2(z, x)) - 90.0;
        double pDeg = -Math.toDegrees(Math.atan2(y, root));
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
    public boolean getIsFlying() {
        return this.isFlying;
    }

    @Override
    public void setIsFlying(boolean value) {
        this.isFlying = value;
    }

    @Override
    public boolean getNoPhysics() {
        return this.noPhysics;
    }

    @Override
    public void setNoPhysics(boolean value) {
        this.noPhysics = value;
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
    public boolean hasTag(String key) {
        return this.customData().containsKey(key);
    }

    @Override
    public Object getTag(String key) {
        return this.customData().get(key);
    }

    @Override
    public Object setTag(String key, Object value) {
        return this.customData().put(key, value);
    }

    @Unique
    protected Map<String, Object> customData() {
        if (this.customData == null) {
            this.customData = new Object2ObjectOpenHashMap<>();
        }
        return this.customData;
    }
}
