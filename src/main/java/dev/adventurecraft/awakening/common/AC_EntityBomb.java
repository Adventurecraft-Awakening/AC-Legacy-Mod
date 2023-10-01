package dev.adventurecraft.awakening.common;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_EntityBomb extends ItemEntity {
	private static final double BOMB_DAMAGE = 20.0D;
	private static final double BOMB_RANGE = 5.0D;
	private static final double BOMB_DESTROY_RANGE = 3.0D;
	private static final int BOMB_FUSE = 45;
	private int fuse;
	private Entity parentEntity;

	public AC_EntityBomb(World var1) {
		super(var1);
		this.setSize(0.5F, 0.5F);
		this.stack = new ItemStack(AC_Items.bomb);
		this.fuse = BOMB_FUSE;
	}

	public AC_EntityBomb(World world, Entity entity) {
		this(world);
		this.parentEntity = entity;
		this.setRotation(entity.yaw, entity.pitch);
		this.xVelocity = 0.3D * -Math.sin((double)(this.yaw / 180.0F * 3.141593F)) * Math.cos((double)(this.pitch / 180.0F * 3.141593F));
		this.zVelocity = 0.3D * Math.cos((double)(this.yaw / 180.0F * 3.141593F)) * Math.cos((double)(this.pitch / 180.0F * 3.141593F));
		this.yVelocity = 0.3D * -Math.sin((double)(this.pitch / 180.0F * 3.141593F)) + (double)0.1F;
		this.setPosition(entity.x, entity.y, entity.z);
		this.prevX = this.x;
		this.prevY = this.y;
		this.prevZ = this.z;
	}

	public void tick() {
		super.tick();
		if(this.fuse == 45) {
			this.world.playSound(this, "random.fuse", 1.0F, 1.0F);
		}

		--this.fuse;
		double fuseRemaining = (double)this.fuse / 45.0D;
        // The particle effects animate as if the fuse was burning, drawing the fire and smoke lower!
		double flameSourceMod = 0.2D * fuseRemaining;

		if(this.fuse == 0) {
			explode(this, this.parentEntity, this.world, this.x, this.y, this.z);
		} else if(this.fuse % 2 == 0) {
			this.world.addParticle("smoke", this.x, this.y + 0.675D + flameSourceMod, this.z, 0.0D, 0.0D, 0.0D);
		} else {
			this.world.addParticle("flame", this.x, this.y + 0.675D + flameSourceMod, this.z, 0.0D, 0.0D, 0.0D);
		}

	}

	public static void explode(Entity exploder, Entity parent, World world, double x, double y, double z) {
		exploder.remove();
		world.playSound(x, y, z, "random.explode", 4.0F, 1.0F);
		List explodees = world.getEntities(exploder, AxixAlignedBoundingBox.createAndAddToList(Math.floor(x - 5.0D), Math.floor(y - 5.0D), Math.floor(z - 5.0D), Math.ceil(x + 5.0D), Math.ceil(y + 5.0D), Math.ceil(z + 5.0D)));

		int explodeeIndex;
		for(explodeeIndex = 0; explodeeIndex < explodees.size(); ++explodeeIndex) {
			Entity explodee = (Entity)explodees.get(explodeeIndex);
			double distanceFromExplosion = explodee.distanceTo(x, y, z);
			if(distanceFromExplosion < BOMB_RANGE) {
				distanceFromExplosion = (BOMB_RANGE - distanceFromExplosion) / BOMB_RANGE; // Percentage of how close the character is
				double xForce = explodee.x - x;
				double yForce = explodee.y - y;
				double zForce = explodee.z - z;
				explodee.accelerate(distanceFromExplosion * xForce, distanceFromExplosion * yForce, distanceFromExplosion * zForce);
				explodee.damage(parent, (int)Math.ceil(distanceFromExplosion * BOMB_DAMAGE));
			}
		}

        // He reused this variable.
		explodeeIndex = (int)x;
		int yPosition = (int)y;
		int zPosition = (int)z;

		int blockOffsetA; // Used for X
		int blockOffsetB; // User for Y
        int bombDestroyRange = (int) BOMB_DESTROY_RANGE;
        // Look for blocks in a 3x3x3 volume centered on the explosion's center block's origin corner, but not the outermost ones.
		for(int xOffset = -bombDestroyRange; xOffset <= bombDestroyRange; ++xOffset) {
			for(blockOffsetB = -bombDestroyRange; blockOffsetB <= 3; ++blockOffsetB) {
				for(blockOffsetA = -bombDestroyRange; blockOffsetA <= bombDestroyRange; ++blockOffsetA) {
					Double distanceSquared = Double.valueOf((double) xOffset * (double) xOffset + (double)(blockOffsetB * blockOffsetB) + (double)(blockOffsetA * blockOffsetA));
					if(distanceSquared.doubleValue() <= 9.0D) {
						int blockAtOffset = world.getBlockId(explodeeIndex + xOffset, yPosition + blockOffsetB, zPosition + blockOffsetA);
                        // Remove bombable tiles
						if(Block.BY_ID[blockAtOffset] instanceof AC_BlockBombable) {
							world.setBlock(explodeeIndex + xOffset, yPosition + blockOffsetB, zPosition + blockOffsetA, 0);
						}
					}
				}
			}
		}

		Random rng = new Random();
		rng.setSeed(world.getWorldTime());

        // He reused variables again smh
        // This is for the smoke coming out of the explosion.
		for(blockOffsetB = -3; blockOffsetB <= 3; ++blockOffsetB) { // Used for X
			for(blockOffsetA = -3; blockOffsetA <= 3; ++blockOffsetA) { // Used for Y
				for(int blockOffsetD = -3; blockOffsetD <= 3; ++blockOffsetD) {
					Double distanceSquared = Double.valueOf((double)blockOffsetB * (double)blockOffsetB + (double)(blockOffsetA * blockOffsetA) + (double)(blockOffsetD * blockOffsetD));
					if(rng.nextInt(3) == 0 && distanceSquared.doubleValue() <= 9.0D) {
						Double xDirection = Double.valueOf((double)blockOffsetB);
						Double yDirection = Double.valueOf((double)blockOffsetA);
						Double zDirection = Double.valueOf((double)blockOffsetD);
						Double launchPower = Double.valueOf(Math.sqrt(distanceSquared.doubleValue()) * (0.75D + 0.5D * rng.nextDouble()) * 1.5D / 3.0D);
						xDirection = Double.valueOf(xDirection.doubleValue() / launchPower.doubleValue());
						yDirection = Double.valueOf(yDirection.doubleValue() / launchPower.doubleValue());
						zDirection = Double.valueOf(zDirection.doubleValue() / launchPower.doubleValue());
						world.addParticle("explode", x, y, z, xDirection.doubleValue(), yDirection.doubleValue(), zDirection.doubleValue());
						world.addParticle("smoke", x, y, z, xDirection.doubleValue(), yDirection.doubleValue(), zDirection.doubleValue());
					}
				}
			}
		}

	}

	public boolean damage(Entity var1, int var2) {
		if(!this.removed) {
			this.setAttacked();
			explode(this, this.parentEntity, this.world, this.x, this.y, this.z);
		}

		return false;
	}

	public void writeAdditional(CompoundTag var1) {
		super.writeAdditional(var1);
		var1.put("Fuse", (byte)this.fuse);
	}

	public void readAdditional(CompoundTag var1) {
		super.readAdditional(var1);
		this.fuse = var1.getByte("Fuse");
	}

	public void onPlayerCollision(PlayerEntity var1) {
	}
}
