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
		this.fuse = 45;
	}

	public AC_EntityBomb(World var1, Entity var2) {
		this(var1);
		this.parentEntity = var2;
		this.setRotation(var2.yaw, var2.pitch);
		this.xVelocity = 0.3D * -Math.sin((double)(this.yaw / 180.0F * 3.141593F)) * Math.cos((double)(this.pitch / 180.0F * 3.141593F));
		this.zVelocity = 0.3D * Math.cos((double)(this.yaw / 180.0F * 3.141593F)) * Math.cos((double)(this.pitch / 180.0F * 3.141593F));
		this.yVelocity = 0.3D * -Math.sin((double)(this.pitch / 180.0F * 3.141593F)) + (double)0.1F;
		this.setPosition(var2.x, var2.y, var2.z);
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
		double var1 = (double)this.fuse / 45.0D;
		double var3 = 0.2D * var1;
		if(this.fuse == 0) {
			explode(this, this.parentEntity, this.world, this.x, this.y, this.z);
		} else if(this.fuse % 2 == 0) {
			this.world.addParticle("smoke", this.x, this.y + 0.675D + var3, this.z, 0.0D, 0.0D, 0.0D);
		} else {
			this.world.addParticle("flame", this.x, this.y + 0.675D + var3, this.z, 0.0D, 0.0D, 0.0D);
		}

	}

	public static void explode(Entity var0, Entity var1, World var2, double var3, double var5, double var7) {
		var0.remove();
		var2.playSound(var3, var5, var7, "random.explode", 4.0F, 1.0F);
		List var9 = var2.getEntities(var0, AxixAlignedBoundingBox.createAndAddToList(Math.floor(var3 - 5.0D), Math.floor(var5 - 5.0D), Math.floor(var7 - 5.0D), Math.ceil(var3 + 5.0D), Math.ceil(var5 + 5.0D), Math.ceil(var7 + 5.0D)));

		int var10;
		for(var10 = 0; var10 < var9.size(); ++var10) {
			Entity var11 = (Entity)var9.get(var10);
			double var12 = var11.distanceTo(var3, var5, var7);
			if(var12 < 5.0D) {
				var12 = (5.0D - var12) / 5.0D;
				double var14 = var11.x - var3;
				double var16 = var11.y - var5;
				double var18 = var11.z - var7;
				var11.accelerate(var12 * var14, var12 * var16, var12 * var18);
				var11.damage(var1, (int)Math.ceil(var12 * 20.0D));
			}
		}

		var10 = (int)var3;
		int var22 = (int)var5;
		int var23 = (int)var7;

		int var15;
		int var25;
		for(int var13 = -3; var13 <= 3; ++var13) {
			for(var25 = -3; var25 <= 3; ++var25) {
				for(var15 = -3; var15 <= 3; ++var15) {
					Double var26 = Double.valueOf((double)var13 * (double)var13 + (double)(var25 * var25) + (double)(var15 * var15));
					if(var26.doubleValue() <= 9.0D) {
						int var17 = var2.getBlockId(var10 + var13, var22 + var25, var23 + var15);
						if(Block.BY_ID[var17] instanceof AC_BlockBombable) {
							var2.setBlock(var10 + var13, var22 + var25, var23 + var15, 0);
						}
					}
				}
			}
		}

		Random var24 = new Random();
		var24.setSeed(var2.getWorldTime());

		for(var25 = -3; var25 <= 3; ++var25) {
			for(var15 = -3; var15 <= 3; ++var15) {
				for(int var27 = -3; var27 <= 3; ++var27) {
					Double var28 = Double.valueOf((double)var25 * (double)var25 + (double)(var15 * var15) + (double)(var27 * var27));
					if(var24.nextInt(3) == 0 && var28.doubleValue() <= 9.0D) {
						Double var29 = Double.valueOf((double)var25);
						Double var19 = Double.valueOf((double)var15);
						Double var20 = Double.valueOf((double)var27);
						Double var21 = Double.valueOf(Math.sqrt(var28.doubleValue()) * (0.75D + 0.5D * var24.nextDouble()) * 1.5D / 3.0D);
						var29 = Double.valueOf(var29.doubleValue() / var21.doubleValue());
						var19 = Double.valueOf(var19.doubleValue() / var21.doubleValue());
						var20 = Double.valueOf(var20.doubleValue() / var21.doubleValue());
						var2.addParticle("explode", var3, var5, var7, var29.doubleValue(), var19.doubleValue(), var20.doubleValue());
						var2.addParticle("smoke", var3, var5, var7, var29.doubleValue(), var19.doubleValue(), var20.doubleValue());
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
