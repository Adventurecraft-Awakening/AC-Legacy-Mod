package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockLockedDoor extends Block {
	int doorKeyToUse;

	protected AC_BlockLockedDoor(int var1, int var2, int var3) {
		super(var1, Material.METAL);
		this.texture = var2;
		this.doorKeyToUse = var3;
	}

	public boolean isFullOpaque() {
		return false;
	}

	public HitResult method_1564(World var1, int var2, int var3, int var4, Vec3d var5, Vec3d var6) {
		int var7 = var1.getBlockMeta(var2, var3, var4);
		return !AC_DebugMode.active && var7 == 1 ? null : super.method_1564(var1, var2, var3, var4, var5, var6);
	}

	public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4);
		return !AC_DebugMode.active && var5 != 1 ? super.getCollisionShape(var1, var2, var3, var4) : null;
	}

	public boolean shouldRender(BlockView var1, int var2, int var3, int var4) {
		int var5 = var1.getBlockMeta(var2, var3, var4);
		return AC_DebugMode.active || var5 == 0;
	}

	public int getTextureForSide(BlockView var1, int var2, int var3, int var4, int var5) {
		if(var5 != 0 && var5 != 1) {
			int var6;
			for(var6 = 1; var1.getBlockId(var2, var3 + var6, var4) == this.id; ++var6) {
			}

			int var7;
			for(var7 = 1; var1.getBlockId(var2, var3 - var7, var4) == this.id; ++var7) {
				++var6;
			}

			int var8 = this.texture;
			if(var6 > 2) {
				if(var6 / 2 == var7 - 1) {
					int var9 = 1;

					for(var7 = 1; var1.getBlockId(var2 + var9, var3, var4) == this.id; ++var9) {
					}

					while(var1.getBlockId(var2 - var7, var3, var4) == this.id) {
						++var9;
						++var7;
					}

					if(var9 == 1) {
						while(var1.getBlockId(var2, var3, var4 + var9) == this.id) {
							++var9;
						}

						while(var1.getBlockId(var2, var3, var4 - var7) == this.id) {
							++var9;
							++var7;
						}
					}

					if(var9 / 2 == var7 - 1) {
						++var8;
					}
				}
			} else {
				var8 += 16;
				if(var1.getBlockId(var2, var3 - 1, var4) != this.id) {
					var8 += 16;
				}

				if(var5 == 2) {
					if(var1.getBlockId(var2 + 1, var3, var4) == this.id) {
						++var8;
					}
				} else if(var5 == 3) {
					if(var1.getBlockId(var2 - 1, var3, var4) == this.id) {
						++var8;
					}
				} else if(var5 == 4) {
					if(var1.getBlockId(var2, var3, var4 - 1) == this.id) {
						++var8;
					}
				} else if(var5 == 5 && var1.getBlockId(var2, var3, var4 + 1) == this.id) {
					++var8;
				}
			}

			return var8;
		} else {
			return this.texture;
		}
	}

	public void activate(World var1, int var2, int var3, int var4, PlayerEntity var5) {
		if(var5.inventory.removeItem(this.doorKeyToUse)) {
			var1.playSound((double)var2 + 0.5D, (double)var3 + 0.5D, (double)var4 + 0.5D, "random.door_open", 1.0F, var1.rand.nextFloat() * 0.1F + 0.9F);

			int var6;
			int var7;
			for(var6 = 0; var1.getBlockId(var2, var3 + var6, var4) == this.id; ++var6) {
				for(var7 = 0; var1.getBlockId(var2 + var7, var3 + var6, var4) == this.id; ++var7) {
					var1.setBlockMeta(var2 + var7, var3 + var6, var4, 1);
					var1.notifyListeners(var2 + var7, var3 + var6, var4);
				}

				for(var7 = 1; var1.getBlockId(var2 - var7, var3 + var6, var4) == this.id; ++var7) {
					var1.setBlockMeta(var2 - var7, var3 + var6, var4, 1);
					var1.notifyListeners(var2 - var7, var3 + var6, var4);
				}

				for(var7 = 1; var1.getBlockId(var2, var3 + var6, var4 + var7) == this.id; ++var7) {
					var1.setBlockMeta(var2, var3 + var6, var4 + var7, 1);
					var1.notifyListeners(var2, var3 + var6, var4 + var7);
				}

				for(var7 = 1; var1.getBlockId(var2, var3 + var6, var4 - var7) == this.id; ++var7) {
					var1.setBlockMeta(var2, var3 + var6, var4 - var7, 1);
					var1.notifyListeners(var2, var3 + var6, var4 - var7);
				}
			}

			for(var6 = -1; var1.getBlockId(var2, var3 + var6, var4) == this.id; --var6) {
				for(var7 = 0; var1.getBlockId(var2 + var7, var3 + var6, var4) == this.id; ++var7) {
					var1.setBlockMeta(var2 + var7, var3 + var6, var4, 1);
					var1.notifyListeners(var2 + var7, var3 + var6, var4);
				}

				for(var7 = 1; var1.getBlockId(var2 - var7, var3 + var6, var4) == this.id; ++var7) {
					var1.setBlockMeta(var2 - var7, var3 + var6, var4, 1);
					var1.notifyListeners(var2 - var7, var3 + var6, var4);
				}

				for(var7 = 1; var1.getBlockId(var2, var3 + var6, var4 + var7) == this.id; ++var7) {
					var1.setBlockMeta(var2, var3 + var6, var4 + var7, 1);
					var1.notifyListeners(var2, var3 + var6, var4 + var7);
				}

				for(var7 = 1; var1.getBlockId(var2, var3 + var6, var4 - var7) == this.id; ++var7) {
					var1.setBlockMeta(var2, var3 + var6, var4 - var7, 1);
					var1.notifyListeners(var2, var3 + var6, var4 - var7);
				}
			}
		}

	}

	public void reset(World var1, int var2, int var3, int var4, boolean var5) {
		if(!var5) {
			var1.setBlockMeta(var2, var3, var4, 0);
		}

	}

	public int alwaysUseClick(World var1, int var2, int var3, int var4) {
		return 0;
	}
}
