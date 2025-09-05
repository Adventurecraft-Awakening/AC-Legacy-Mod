package dev.adventurecraft.awakening.mixin.entity.projectile;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.world.RayFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(Arrow.class)
public abstract class MixinArrowEntity extends MixinEntity implements ExArrowEntity {

    @Shadow public int xTile;
    @Shadow public int yTile;
    @Shadow public int zTile;
    @Shadow private boolean inGround;
    @Shadow public int shakeTime;
    @Shadow public int inTile;
    @Shadow private int life;
    @Shadow private int inData;
    @Shadow public int flightTime;
    @Shadow public Mob owner;

    @Unique private int attackStrength = 2;

    @Inject(
        method = "defineSynchedData",
        at = @At("TAIL")
    )
    private void init(CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Overwrite
    public void tick() {
        super.tick();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float len = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
            this.yRotO = this.yRot = MathF.toDegrees((float) Math.atan2(this.xd, this.zd));
            this.xRotO = this.xRot = MathF.toDegrees((float) Math.atan2(this.yd, len));
        }

        int tileId = this.level.getTile(this.xTile, this.yTile, this.zTile);
        if (tileId > 0 && tileId != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(tileId)) {
            var tile = Tile.tiles[tileId];
            tile.updateShape(this.level, this.xTile, this.yTile, this.zTile);
            AABB aabb = tile.getAABB(this.level, this.xTile, this.yTile, this.zTile);
            if (aabb != null && aabb.intersects(Vec3.newTemp(this.x, this.y, this.z))) {
                this.inGround = true;
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.inGround) {
            tileId = this.level.getTile(this.xTile, this.yTile, this.zTile);
            int data = this.level.getData(this.xTile, this.yTile, this.zTile);
            if (tileId == this.inTile && data == this.inData) {
                ++this.life;
                if (this.life == 1200) {
                    this.remove();
                }

            }
            else {
                this.inGround = false;
                this.xd *= this.random.nextFloat() * 0.2F;
                this.yd *= this.random.nextFloat() * 0.2F;
                this.zd *= this.random.nextFloat() * 0.2F;
                this.life = 0;
                this.flightTime = 0;
            }
        }
        else {
            ++this.flightTime;
            Vec3 start = Vec3.newTemp(this.x, this.y, this.z);
            Vec3 end = Vec3.newTemp(this.x + this.xd, this.y + this.yd, this.z + this.zd);
            HitResult hit = ((ExWorld) this.level).rayTraceBlocks2(start, end, RayFlags.SHAPE);

            start.set(this.x, this.y, this.z);
            if (hit == null) {
                end.set(this.x + this.xd, this.y + this.yd, this.z + this.zd);
            }
            else {
                end.set(hit.pos.x, hit.pos.y, hit.pos.z);
            }

            List<Entity> entities = this.level.getEntities(
                (Entity) (Object) this,
                this.bb.expand(this.xd, this.yd, this.zd).inflate(1.0D, 1.0D, 1.0D)
            );

            Entity hitEntity = null;
            double minDist = 0.0D;
            for (Entity entity : entities) {
                if (entity.isPickable() && (entity != this.owner || this.flightTime >= 5)) {
                    float range = 0.3F;
                    AABB ebb = entity.bb.inflate(range, range, range);
                    HitResult entityHit = ebb.clip(start, end);
                    if (entityHit != null) {
                        double dist = start.distanceTo(entityHit.pos);
                        if (dist < minDist || minDist == 0.0D) {
                            hitEntity = entity;
                            minDist = dist;
                        }
                    }
                }
            }

            if (hitEntity != null) {
                hit = new HitResult(hitEntity);
            }

            if (hit != null) {
                if (hit.entity != null) {
                    this.handleHitEntity(hit);
                }
                else {
                    this.xTile = hit.x;
                    this.yTile = hit.y;
                    this.zTile = hit.z;
                    this.inTile = this.level.getTile(this.xTile, this.yTile, this.zTile);
                    this.inData = this.level.getData(this.xTile, this.yTile, this.zTile);
                    this.xd = (float) (hit.pos.x - this.x);
                    this.yd = (float) (hit.pos.y - this.y);
                    this.zd = (float) (hit.pos.z - this.z);
                    double len = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
                    this.x -= this.xd / len * 0.05;
                    this.y -= this.yd / len * 0.05;
                    this.z -= this.zd / len * 0.05;
                    this.playHitSound();
                    this.inGround = true;
                    this.shakeTime = 7;
                }
            }

            this.x += this.xd;
            this.y += this.yd;
            this.z += this.zd;
            float lenXZ = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
            this.yRot = MathF.toDegrees((float) Math.atan2(this.xd, this.zd));
            this.xRot = MathF.toDegrees((float) Math.atan2(this.yd, lenXZ));

            this.xRotO = MathF.normalizeAngleDelta(this.xRotO, this.xRot);
            this.yRotO = MathF.normalizeAngleDelta(this.yRotO, this.yRot);

            this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
            this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;

            float speed = 0.99F;
            if (this.isInWater()) {
                this.spawnBubbles();
                speed = 0.8F;
            }

            this.xd *= speed;
            this.yd *= speed;
            this.zd *= speed;
            this.yd -= 0.03F;
            this.setPos(this.x, this.y, this.z);
        }
    }

    @Unique
    private void spawnBubbles() {
        double len = 0.25;
        double x = this.x - this.xd * len;
        double y = this.y - this.yd * len;
        double z = this.z - this.zd * len;

        for (int i = 0; i < 4; ++i) {
            this.level.addParticle("bubble", x, y, z, this.xd, this.yd, this.zd);
        }
    }

    @Unique
    private void handleHitEntity(HitResult hit) {
        if (hit.entity instanceof Mob mob && ((ExMob) mob).protectedByShield(this.xo, this.yo, this.zo)) {
            this.consumeArrow();
        }
        else if (hit.entity.hurt(this.owner, this.attackStrength)) {
            this.consumeArrow();
        }
        else {
            this.xd *= -0.1F;
            this.yd *= -0.1F;
            this.zd *= -0.1F;
            this.yRot += 180.0F;
            this.yRotO += 180.0F;
            this.flightTime = 0;
        }
    }

    @Unique
    private void playHitSound() {
        float pitch = 1.2F / (this.random.nextFloat() * 0.2F + 0.9F);
        this.level.playSound((Entity) (Object) this, "random.drr", 1.0F, pitch);
    }

    @Unique
    private void consumeArrow() {
        this.playHitSound();
        this.remove();
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
