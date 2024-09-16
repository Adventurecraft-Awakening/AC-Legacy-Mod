package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.common.IEntityPather;
import dev.adventurecraft.awakening.extension.entity.ExMobEntity;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

@Mixin(Mob.class)
public abstract class MixinMobEntity extends MixinLivingEntity implements ExMobEntity, IEntityPather {

    @Shadow
    protected Entity entity;
    @Shadow
    private Path field_661;
    @Shadow
    protected boolean field_663;

    @Shadow
    protected abstract void method_632();

    @Shadow
    protected abstract boolean method_640();

    @Shadow
    protected abstract Entity getAttackTarget();

    @Shadow
    protected abstract void method_639(Entity arg, float f);

    @Shadow
    protected abstract void tryAttack(Entity arg, float f);

    @Shadow
    public abstract boolean method_633();

    public boolean canForgetTargetRandomly = true;
    public int timeBeforeForget = 0;
    public boolean canPathRandomly = true;

    @Overwrite
    public void tickHandSwing() {
        this.field_663 = this.method_640();
        float var1 = 16.0F;
        if (this.entity == null) {
            this.entity = this.getAttackTarget();
            if (this.entity != null) {
                this.field_661 = this.world.findPath((Entity) (Object) this, this.entity, var1);
                this.timeBeforeForget = 40;
            }
        } else if (!this.entity.isAlive()) {
            this.entity = null;
        } else {
            float var2 = this.entity.distanceTo((Entity) (Object) this);
            if (this.method_928(this.entity)) {
                this.tryAttack(this.entity, var2);
            } else {
                this.method_639(this.entity, var2);
            }
        }

        boolean var21 = false;
        if (this.entity != null) {
            var21 = this.method_928(this.entity);
        }

        if (!this.field_663 && this.entity != null && (this.field_661 == null || this.rand.nextInt(5) == 0 && ((ExEntityPath) this.field_661).needNewPath(this.entity)) && var21) {
            this.field_661 = this.world.findPath((Entity) (Object) this, this.entity, var1);
        } else if (this.canPathRandomly && !this.field_663 && (this.field_661 == null && this.rand.nextInt(80) == 0 || this.rand.nextInt(80) == 0)) {
            this.method_632();
        }

        if (this.entity != null && this.field_661 == null && !var21) {
            if (this.timeBeforeForget-- <= 0) {
                this.entity = null;
            }
        } else {
            this.timeBeforeForget = 40;
        }

        int var3 = Mth.floor(this.boundingBox.y0 + 0.5D);
        boolean var4 = this.method_1334();
        boolean var5 = this.method_1335();
        this.pitch = 0.0F;
        if (this.field_661 != null && (!this.canForgetTargetRandomly || this.rand.nextInt(300) != 0)) {
            Vec3 var6 = this.field_661.current((Entity) (Object) this);
            double var7 = this.width * 2.0F;

            while (var6 != null && var6.distanceToSqr(this.x, var6.y, this.z) < var7 * var7) {
                this.field_661.next();
                if (this.field_661.isDone()) {
                    var6 = null;
                    this.field_661 = null;
                } else {
                    var6 = this.field_661.current((Entity) (Object) this);
                }
            }

            this.jumping = false;
            if (var6 != null) {
                var7 = var6.x - this.x;
                double var9 = var6.z - this.z;
                double var11 = var6.y - (double) var3;
                float var13 = (float) (Math.atan2(var9, var7) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
                float var14 = var13 - this.yaw;

                this.forwardVelocity = this.movementSpeed;
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

                this.yaw += var14;
                if (this.field_663 && this.entity != null) {
                    double var15 = this.entity.x - this.x;
                    double var17 = this.entity.z - this.z;
                    float var19 = this.yaw;
                    this.yaw = (float) (Math.atan2(var17, var15) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
                    float var20 = (var19 - this.yaw + 90.0F) * 3.141593F / 180.0F;
                    this.horizontalVelocity = -Mth.sin(var20) * this.forwardVelocity * 1.0F;
                    this.forwardVelocity = Mth.cos(var20) * this.forwardVelocity * 1.0F;
                }

                if (var11 > 0.0D) {
                    this.jumping = true;
                }
            } else if (this.entity != null) {
                this.lookAt(this.entity, 30.0F, 30.0F);
            }

            if (this.field_1624 && !this.method_633()) {
                this.jumping = true;
            }

            if (this.rand.nextFloat() < 0.8F && (var4 || var5)) {
                this.jumping = true;
            }

        } else {
            super.tickHandSwing();
            this.field_661 = null;
        }
    }

    @Override
    public Path getCurrentPath() {
        return this.field_661;
    }

    @Override
    public void writeAdditional(CompoundTag compoundTag) {
        super.writeAdditional(compoundTag);
        compoundTag.putBoolean("canPathRandomly", this.canPathRandomly);
        compoundTag.putBoolean("canForgetTargetRandomly", this.canForgetTargetRandomly);
        if(!customData.isEmpty()) {
            CompoundTag customCompoundTag = new CompoundTag();
            for(String key : customData.keySet()){
                customCompoundTag.putString(key,customData.get(key));
            }
            compoundTag.putCompoundTag("custom",customCompoundTag);
        }
    }

    @Override
    public void readAdditional(CompoundTag compoundTag) {
        super.readAdditional(compoundTag);
        if (compoundTag.hasKey("canPathRandomly")) {
            this.canPathRandomly = compoundTag.getBoolean("canPathRandomly");
        }

        if (compoundTag.hasKey("canForgetTargetRandomly")) {
            this.canPathRandomly = compoundTag.getBoolean("canForgetTargetRandomly");
        }

        if(compoundTag.hasKey("custom")){
            for(Tag tags : (Collection<Tag>)compoundTag.getCompoundTag("custom").getTags()) {
                customData.put(tags.getType(),tags.toString());
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
