package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.common.IEntityPather;
import dev.adventurecraft.awakening.extension.entity.ExPathfinderMob;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathfinderMob.class)
public abstract class MixinPathfinderMob extends MixinMob implements ExPathfinderMob, IEntityPather {

    @Shadow protected Entity attackTarget;
    @Shadow private Path path;
    @Shadow protected boolean holdGround;

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

    @Unique private boolean canForgetTargetRandomly = true;
    @Unique protected int timeBeforeForget = 0;
    @Unique private boolean canPathRandomly = true;
    @Unique private float targetSearchRange = 16.0F; // TODO: expose to script

    @Overwrite
    public void serverAiStep() {
        this.holdGround = this.shouldHoldGround();
        if (this.attackTarget == null) {
            this.attackTarget = this.findAttackTarget();
            if (this.attackTarget != null) {
                this.path = this.level.findPath((Entity) (Object) this, this.attackTarget, this.targetSearchRange);
                this.timeBeforeForget = 40;
            }
        }
        else if (!this.attackTarget.isAlive()) {
            this.attackTarget = null;
        }
        else {
            float dist = this.attackTarget.distanceTo((Entity) (Object) this);
            if (this.canSee(this.attackTarget)) {
                this.checkHurtTarget(this.attackTarget, dist);
            }
            else {
                this.resetAttack(this.attackTarget, dist);
            }
        }

        boolean canSee = this.attackTarget != null && this.canSee(this.attackTarget);

        if (!this.holdGround && this.attackTarget != null && (this.path == null ||
            this.random.nextInt(5) == 0 && ((ExEntityPath) this.path).needNewPath(this.attackTarget)
        ) && canSee) {
            this.path = this.level.findPath((Entity) (Object) this, this.attackTarget, this.targetSearchRange);
        }
        else if (this.canPathRandomly && !this.holdGround &&
            (this.path == null && this.random.nextInt(80) == 0 || this.random.nextInt(80) == 0)) {
            this.method_632();
        }

        if (this.attackTarget != null && this.path == null && !canSee) {
            if (this.timeBeforeForget-- <= 0) {
                this.attackTarget = null;
            }
        }
        else {
            this.timeBeforeForget = 40;
        }

        int y = Mth.floor(this.bb.y0 + 0.5D);
        this.xRot = 0.0F;

        if (this.path != null && (!this.canForgetTargetRandomly || this.random.nextInt(300) != 0)) {
            boolean inWater = this.isInWater();
            boolean inLava = this.isInLava();

            Vec3 node = this.path.current((Entity) (Object) this);
            double bbWidth = this.bbWidth * 2.0F;

            while (node != null && node.distanceToSqr(this.x, node.y, this.z) < bbWidth * bbWidth) {
                this.path.next();
                if (this.path.isDone()) {
                    node = null;
                    this.path = null;
                }
                else {
                    node = this.path.current((Entity) (Object) this);
                }
            }

            this.jumping = false;
            if (node != null) {
                double dX = node.x - this.x;
                double dZ = node.z - this.z;
                double dY = node.y - (double) y;
                float moveDir = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F;
                float moveRot = MathF.clampAngle(moveDir - this.yRot, -30.0F, 30.0F);

                this.zza = this.runSpeed;
                this.yRot += moveRot;
                if (this.holdGround && this.attackTarget != null) {
                    double atdX = this.attackTarget.x - this.x;
                    double atdZ = this.attackTarget.z - this.z;
                    float yRot0 = this.yRot;
                    this.yRot = (float) Math.toDegrees(Math.atan2(atdZ, atdX)) - 90.0F;
                    float attackDir = MathF.toRadians(yRot0 - this.yRot + 90.0F);
                    this.xxa = -Mth.sin(attackDir) * this.zza;
                    this.zza = Mth.cos(attackDir) * this.zza;
                }

                if (dY > 0.0D) {
                    this.jumping = true;
                }
            }
            else if (this.attackTarget != null) {
                this.setLookAt(this.attackTarget, 30.0F, 30.0F);
            }

            if (!this.jumping) {
                if (this.horizontalCollision && !this.hasPath()) {
                    this.jumping = true;
                }

                if (this.random.nextFloat() < 0.8F && (inWater || inLava)) {
                    this.jumping = true;
                }
            }
        }
        else {
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
    }

    @Override
    protected void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$addAdditionalSaveData(tag, ci);
        var exTag = (ExCompoundTag) tag;

        exTag.findBool("canPathRandomly").ifPresent(this::setCanPathRandomly);
        exTag.findBool("canForgetTargetRandomly").ifPresent(this::setCanForgetTargetRandomly);
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
