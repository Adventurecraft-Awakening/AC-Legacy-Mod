package dev.adventurecraft.awakening.mixin.entity.projectile;

import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(Arrow.class)
public abstract class MixinArrowEntity extends MixinEntity implements ExArrowEntity {

    @Shadow
    public  int xTile;
    @Shadow
    public  int yTile;
    @Shadow
    public  int zTile;
    @Shadow
    private boolean inGround;
    @Shadow
    public int shakeTime;
    @Shadow
    public int inTile;
    @Shadow
    private int life;
    @Shadow
    private int inData;
    @Shadow
    public int flightTime;
    @Shadow
    public LivingEntity owner;

    private int attackStrength = 2;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Overwrite
    public void tick() {
        super.tick();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float var1 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
            this.yRotO = this.yRot = (float) (Math.atan2(this.xd, this.zd) * 180.0D / (double) ((float) Math.PI));
            this.xRotO = this.xRot = (float) (Math.atan2(this.yd, var1) * 180.0D / (double) ((float) Math.PI));
        }

        int var16 = this.level.getTile(this.xTile, this.yTile, this.zTile);
        if (var16 > 0 && var16 != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(var16)) {
            Tile.tiles[var16].updateShape(this.level, this.xTile, this.yTile, this.zTile);
            AABB var2 = Tile.tiles[var16].getAABB(this.level, this.xTile, this.yTile, this.zTile);
            if (var2 != null && var2.intersects(Vec3.newTemp(this.x, this.y, this.z))) {
                this.inGround = true;
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.inGround) {
            int var18 = this.level.getTile(this.xTile, this.yTile, this.zTile);
            int var19 = this.level.getData(this.xTile, this.yTile, this.zTile);
            if (var18 == this.inTile && var19 == this.inData) {
                ++this.life;
                if (this.life == 1200) {
                    this.remove();
                }

            } else {
                this.inGround = false;
                this.xd *= this.random.nextFloat() * 0.2F;
                this.yd *= this.random.nextFloat() * 0.2F;
                this.zd *= this.random.nextFloat() * 0.2F;
                this.life = 0;
                this.flightTime = 0;
            }
        } else {
            ++this.flightTime;
            Vec3 var17 = Vec3.newTemp(this.x, this.y, this.z);
            Vec3 var3 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
            HitResult var4 = ((ExWorld) this.level).rayTraceBlocks2(var17, var3, false, true, false);
            var17 = Vec3.newTemp(this.x, this.y, this.z);
            var3 = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
            if (var4 != null) {
                var3 = Vec3.newTemp(var4.pos.x, var4.pos.y, var4.pos.z);
            }

            Entity var5 = null;
            List<Entity> var6 = this.level.getEntities((Entity) (Object) this, this.bb.expand(this.xd, this.yd, this.zd).inflate(1.0D, 1.0D, 1.0D));
            double var7 = 0.0D;

            float var11;
            for (Entity var10 : var6) {
                if (var10.isPickable() && (var10 != this.owner || this.flightTime >= 5)) {
                    var11 = 0.3F;
                    AABB var12 = var10.bb.inflate(var11, var11, var11);
                    HitResult var13 = var12.clip(var17, var3);
                    if (var13 != null) {
                        double var14 = var17.distanceTo(var13.pos);
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
                if (var4.entity != null) {
                    this.handleHitEntity(var4);
                } else {
                    this.xTile = var4.x;
                    this.yTile = var4.y;
                    this.zTile = var4.z;
                    this.inTile = this.level.getTile(this.xTile, this.yTile, this.zTile);
                    this.inData = this.level.getData(this.xTile, this.yTile, this.zTile);
                    this.xd = (float) (var4.pos.x - this.x);
                    this.yd = (float) (var4.pos.y - this.y);
                    this.zd = (float) (var4.pos.z - this.z);
                    var20 = Mth.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
                    this.x -= this.xd / (double) var20 * (double) 0.05F;
                    this.y -= this.yd / (double) var20 * (double) 0.05F;
                    this.z -= this.zd / (double) var20 * (double) 0.05F;
                    this.level.playSound((Entity) (Object) this, "random.drr", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.shakeTime = 7;
                }
            }

            this.x += this.xd;
            this.y += this.yd;
            this.z += this.zd;
            var20 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
            this.yRot = (float) (Math.atan2(this.xd, this.zd) * 180.0D / (double) ((float) Math.PI));

            this.xRot = (float) (Math.atan2(this.yd, var20) * 180.0D / (double) ((float) Math.PI));
            while (this.xRot - this.xRotO < -180.0F) {
                this.xRotO -= 360.0F;
            }

            while (this.xRot - this.xRotO >= 180.0F) {
                this.xRotO += 360.0F;
            }

            while (this.yRot - this.yRotO < -180.0F) {
                this.yRotO -= 360.0F;
            }

            while (this.yRot - this.yRotO >= 180.0F) {
                this.yRotO += 360.0F;
            }

            this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
            this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;
            float var21 = 0.99F;
            var11 = 0.03F;
            if (this.isInWater()) {
                for (int var22 = 0; var22 < 4; ++var22) {
                    float var23 = 0.25F;
                    this.level.addParticle("bubble", this.x - this.xd * (double) var23, this.y - this.yd * (double) var23, this.z - this.zd * (double) var23, this.xd, this.yd, this.zd);
                }

                var21 = 0.8F;
            }

            this.xd *= var21;
            this.yd *= var21;
            this.zd *= var21;
            this.yd -= var11;
            this.setPos(this.x, this.y, this.z);
        }
    }

    public void handleHitEntity(HitResult var1) {
        if (var1.entity instanceof LivingEntity && ((ExLivingEntity) var1.entity).protectedByShield(this.xo, this.yo, this.zo)) {
            this.level.playSound((Entity) (Object) this, "random.drr", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.remove();
        } else if (var1.entity.hurt(this.owner, this.attackStrength)) {
            this.level.playSound((Entity) (Object) this, "random.drr", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.remove();
        } else {
            this.xd *= -0.1F;
            this.yd *= -0.1F;
            this.zd *= -0.1F;
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            this.flightTime = 0;
        }
    }

    @Override
    public int getAttackStrength() {
        return this.attackStrength;
    }

    @Override
    public void setAttackStrength(int value) {
        this.attackStrength = value;
    }
}
