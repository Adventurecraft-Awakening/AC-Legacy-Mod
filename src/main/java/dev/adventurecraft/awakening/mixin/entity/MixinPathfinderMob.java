package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.common.IEntityPather;
import dev.adventurecraft.awakening.extension.entity.ExPathfinderMob;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathfinderMob.class)
public abstract class MixinPathfinderMob extends MixinMob implements ExPathfinderMob, IEntityPather {

    @Shadow
    protected Entity attackTarget;
    @Shadow
    private Path path;
    @Shadow
    protected boolean holdGround;

    @Shadow
    protected abstract void method_632();

    @Shadow
    protected abstract boolean shouldHoldGround();

    @Shadow
    protected abstract Entity findAttackTarget();

    @Shadow
    protected abstract void resetAttack(Entity arg, float f);

    @Shadow
    protected abstract void checkHurtTarget(Entity arg, float f);

    @Shadow
    public abstract boolean hasPath();

    public boolean canForgetTargetRandomly = true;
    public int timeBeforeForget = 0;
    public boolean canPathRandomly = true;

    @Overwrite
    public void serverAiStep() {
        this.holdGround = this.shouldHoldGround();
        float var1 = 16.0F;
        if (this.attackTarget == null) {
            this.attackTarget = this.findAttackTarget();
            if (this.attackTarget != null) {
                this.path = this.level.findPath((Entity) (Object) this, this.attackTarget, var1);
                this.timeBeforeForget = 40;
            }
        } else if (!this.attackTarget.isAlive()) {
            this.attackTarget = null;
        } else {
            float var2 = this.attackTarget.distanceTo((Entity) (Object) this);
            if (this.canSee(this.attackTarget)) {
                this.checkHurtTarget(this.attackTarget, var2);
            } else {
                this.resetAttack(this.attackTarget, var2);
            }
        }

        boolean var21 = false;
        if (this.attackTarget != null) {
            var21 = this.canSee(this.attackTarget);
        }

        if (!this.holdGround && this.attackTarget != null && (this.path == null || this.random.nextInt(5) == 0 && ((ExEntityPath) this.path).needNewPath(this.attackTarget)) && var21) {
            this.path = this.level.findPath((Entity) (Object) this, this.attackTarget, var1);
        } else if (this.canPathRandomly && !this.holdGround && (this.path == null && this.random.nextInt(80) == 0 || this.random.nextInt(80) == 0)) {
            this.method_632();
        }

        if (this.attackTarget != null && this.path == null && !var21) {
            if (this.timeBeforeForget-- <= 0) {
                this.attackTarget = null;
            }
        } else {
            this.timeBeforeForget = 40;
        }

        int var3 = Mth.floor(this.bb.y0 + 0.5D);
        boolean var4 = this.isInWater();
        boolean var5 = this.isInLava();
        this.xRot = 0.0F;
        if (this.path != null && (!this.canForgetTargetRandomly || this.random.nextInt(300) != 0)) {
            Vec3 var6 = this.path.current((Entity) (Object) this);
            double var7 = this.bbWidth * 2.0F;

            while (var6 != null && var6.distanceToSqr(this.x, var6.y, this.z) < var7 * var7) {
                this.path.next();
                if (this.path.isDone()) {
                    var6 = null;
                    this.path = null;
                } else {
                    var6 = this.path.current((Entity) (Object) this);
                }
            }

            this.jumping = false;
            if (var6 != null) {
                var7 = var6.x - this.x;
                double var9 = var6.z - this.z;
                double var11 = var6.y - (double) var3;
                float var13 = (float) (Math.atan2(var9, var7) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
                float var14 = var13 - this.yRot;

                this.zza = this.runSpeed;
                while (var14 < -180.0F) {
                    var14 += 360.0F;
                }

                while (var14 >= 180.0F) {
                    var14 -= 360.0F;
                }

                if (var14 > 30.0F) {
                    var14 = 30.0F;
                }

                if (var14 < -30.0F) {
                    var14 = -30.0F;
                }

                this.yRot += var14;
                if (this.holdGround && this.attackTarget != null) {
                    double var15 = this.attackTarget.x - this.x;
                    double var17 = this.attackTarget.z - this.z;
                    float var19 = this.yRot;
                    this.yRot = (float) (Math.atan2(var17, var15) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
                    float var20 = (var19 - this.yRot + 90.0F) * 3.141593F / 180.0F;
                    this.xxa = -Mth.sin(var20) * this.zza * 1.0F;
                    this.zza = Mth.cos(var20) * this.zza * 1.0F;
                }

                if (var11 > 0.0D) {
                    this.jumping = true;
                }
            } else if (this.attackTarget != null) {
                this.setLookAt(this.attackTarget, 30.0F, 30.0F);
            }

            if (this.horizontalCollision && !this.hasPath()) {
                this.jumping = true;
            }

            if (this.random.nextFloat() < 0.8F && (var4 || var5)) {
                this.jumping = true;
            }

        } else {
            super.serverAiStep();
            this.path = null;
        }
    }

    @Override
    public Path getCurrentPath() {
        return this.path;
    }

    @Override
    protected void ac$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$readAdditionalSaveData(tag, ci);

        tag.putBoolean("canPathRandomly", this.canPathRandomly);
        tag.putBoolean("canForgetTargetRandomly", this.canForgetTargetRandomly);

        if (!this.customData.isEmpty()) {
            var customCompoundTag = new CompoundTag();
            for (String key : this.customData.keySet()) {
                customCompoundTag.putString(key, this.customData.get(key));
            }
            tag.putCompoundTag("custom", customCompoundTag);
        }
    }

    @Override
    protected void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$addAdditionalSaveData(tag, ci);
        var exTag = (ExCompoundTag) tag;

        exTag.findBool("canPathRandomly").ifPresent(this::setCanPathRandomly);
        exTag.findBool("canForgetTargetRandomly").ifPresent(this::setCanForgetTargetRandomly);

        var customTag = exTag.findCompound("custom");
        if (customTag.isPresent()) {
            for (Tag tags : (Collection<Tag>) customTag.get().getTags()) {
                this.customData.put(tags.getType(), tags.toString());
            }
        }
    }

    @Override
    public boolean getCanForgetTargetRandomly() {
        return this.canForgetTargetRandomly;
    }

    @Override
    public void setCanForgetTargetRandomly(boolean value) {
        this.canForgetTargetRandomly = value;
    }

    @Override
    public boolean getCanPathRandomly() {
        return this.canPathRandomly;
    }

    @Override
    public void setCanPathRandomly(boolean value) {
        this.canPathRandomly = value;
    }
}
