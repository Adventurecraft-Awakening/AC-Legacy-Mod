package dev.adventurecraft.awakening.mixin.entity.projectile;

import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ArrowEntity.class)
public abstract class MixinArrowEntity extends MixinEntity {

    @Shadow
    private int blockX;
    @Shadow
    private int blockY;
    @Shadow
    private int blockZ;
    @Shadow
    private boolean inGround;
    @Shadow
    public int shake;
    @Shadow
    private int inBlock;
    @Shadow
    private int ticksInGround;
    @Shadow
    private int inData;
    @Shadow
    public int ticksFlying;
    @Shadow
    public LivingEntity owner;

    private int attackStrength = 2;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Overwrite
    public void tick() {
        super.tick();
        if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
            float var1 = MathHelper.sqrt(this.xVelocity * this.xVelocity + this.zVelocity * this.zVelocity);
            this.prevYaw = this.yaw = (float) (Math.atan2(this.xVelocity, this.zVelocity) * 180.0D / (double) ((float) Math.PI));
            this.prevPitch = this.pitch = (float) (Math.atan2(this.yVelocity, var1) * 180.0D / (double) ((float) Math.PI));
        }

        int var16 = this.world.getBlockId(this.blockX, this.blockY, this.blockZ);
        if (var16 > 0 && var16 != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(var16)) {
            Block.BY_ID[var16].updateBoundingBox(this.world, this.blockX, this.blockY, this.blockZ);
            AxixAlignedBoundingBox var2 = Block.BY_ID[var16].getCollisionShape(this.world, this.blockX, this.blockY, this.blockZ);
            if (var2 != null && var2.contains(Vec3d.from(this.x, this.y, this.z))) {
                this.inGround = true;
            }
        }

        if (this.shake > 0) {
            --this.shake;
        }

        if (this.inGround) {
            int var18 = this.world.getBlockId(this.blockX, this.blockY, this.blockZ);
            int var19 = this.world.getBlockMeta(this.blockX, this.blockY, this.blockZ);
            if (var18 == this.inBlock && var19 == this.inData) {
                ++this.ticksInGround;
                if (this.ticksInGround == 1200) {
                    this.remove();
                }

            } else {
                this.inGround = false;
                this.xVelocity *= this.rand.nextFloat() * 0.2F;
                this.yVelocity *= this.rand.nextFloat() * 0.2F;
                this.zVelocity *= this.rand.nextFloat() * 0.2F;
                this.ticksInGround = 0;
                this.ticksFlying = 0;
            }
        } else {
            ++this.ticksFlying;
            Vec3d var17 = Vec3d.from(this.x, this.y, this.z);
            Vec3d var3 = Vec3d.from(this.x + this.xVelocity, this.y + this.yVelocity, this.z + this.zVelocity);
            HitResult var4 = ((ExWorld) this.world).rayTraceBlocks2(var17, var3, false, true, false);
            var17 = Vec3d.from(this.x, this.y, this.z);
            var3 = Vec3d.from(this.x + this.xVelocity, this.y + this.yVelocity, this.z + this.zVelocity);
            if (var4 != null) {
                var3 = Vec3d.from(var4.field_1988.x, var4.field_1988.y, var4.field_1988.z);
            }

            Entity var5 = null;
            List<Entity> var6 = this.world.getEntities((Entity) (Object) this, this.boundingBox.duplicateAndExpand(this.xVelocity, this.yVelocity, this.zVelocity).expand(1.0D, 1.0D, 1.0D));
            double var7 = 0.0D;

            float var11;
            for (Entity var10 : var6) {
                if (var10.method_1356() && (var10 != this.owner || this.ticksFlying >= 5)) {
                    var11 = 0.3F;
                    AxixAlignedBoundingBox var12 = var10.boundingBox.expand(var11, var11, var11);
                    HitResult var13 = var12.method_89(var17, var3);
                    if (var13 != null) {
                        double var14 = var17.distanceTo(var13.field_1988);
                        if (var14 < var7 || var7 == 0.0D) {
                            var5 = var10;
                            var7 = var14;
                        }
                    }
                }
            }

            if (var5 != null) {
                var4 = new HitResult(var5);
            }

            float var20;
            if (var4 != null) {
                if (var4.field_1989 != null) {
                    this.handleHitEntity(var4);
                } else {
                    this.blockX = var4.x;
                    this.blockY = var4.y;
                    this.blockZ = var4.z;
                    this.inBlock = this.world.getBlockId(this.blockX, this.blockY, this.blockZ);
                    this.inData = this.world.getBlockMeta(this.blockX, this.blockY, this.blockZ);
                    this.xVelocity = (float) (var4.field_1988.x - this.x);
                    this.yVelocity = (float) (var4.field_1988.y - this.y);
                    this.zVelocity = (float) (var4.field_1988.z - this.z);
                    var20 = MathHelper.sqrt(this.xVelocity * this.xVelocity + this.yVelocity * this.yVelocity + this.zVelocity * this.zVelocity);
                    this.x -= this.xVelocity / (double) var20 * (double) 0.05F;
                    this.y -= this.yVelocity / (double) var20 * (double) 0.05F;
                    this.z -= this.zVelocity / (double) var20 * (double) 0.05F;
                    this.world.playSound((Entity) (Object) this, "random.drr", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.shake = 7;
                }
            }

            this.x += this.xVelocity;
            this.y += this.yVelocity;
            this.z += this.zVelocity;
            var20 = MathHelper.sqrt(this.xVelocity * this.xVelocity + this.zVelocity * this.zVelocity);
            this.yaw = (float) (Math.atan2(this.xVelocity, this.zVelocity) * 180.0D / (double) ((float) Math.PI));

            this.pitch = (float) (Math.atan2(this.yVelocity, var20) * 180.0D / (double) ((float) Math.PI));
            while (this.pitch - this.prevPitch < -180.0F) {
                this.prevPitch -= 360.0F;
            }

            while (this.pitch - this.prevPitch >= 180.0F) {
                this.prevPitch += 360.0F;
            }

            while (this.yaw - this.prevYaw < -180.0F) {
                this.prevYaw -= 360.0F;
            }

            while (this.yaw - this.prevYaw >= 180.0F) {
                this.prevYaw += 360.0F;
            }

            this.pitch = this.prevPitch + (this.pitch - this.prevPitch) * 0.2F;
            this.yaw = this.prevYaw + (this.yaw - this.prevYaw) * 0.2F;
            float var21 = 0.99F;
            var11 = 0.03F;
            if (this.method_1334()) {
                for (int var22 = 0; var22 < 4; ++var22) {
                    float var23 = 0.25F;
                    this.world.addParticle("bubble", this.x - this.xVelocity * (double) var23, this.y - this.yVelocity * (double) var23, this.z - this.zVelocity * (double) var23, this.xVelocity, this.yVelocity, this.zVelocity);
                }

                var21 = 0.8F;
            }

            this.xVelocity *= var21;
            this.yVelocity *= var21;
            this.zVelocity *= var21;
            this.yVelocity -= var11;
            this.setPosition(this.x, this.y, this.z);
        }
    }

    public void handleHitEntity(HitResult var1) {
        if (var1.field_1989 instanceof LivingEntity && ((ExLivingEntity) var1.field_1989).protectedByShield(this.prevX, this.prevY, this.prevZ)) {
            this.world.playSound((Entity) (Object) this, "random.drr", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.remove();
        } else if (var1.field_1989.damage(this.owner, this.attackStrength)) {
            this.world.playSound((Entity) (Object) this, "random.drr", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.remove();
        } else {
            this.xVelocity *= -0.1F;
            this.yVelocity *= -0.1F;
            this.zVelocity *= -0.1F;
            this.yaw += 180.0F;
            this.prevYaw += 180.0F;
            this.ticksFlying = 0;
        }
    }
}
