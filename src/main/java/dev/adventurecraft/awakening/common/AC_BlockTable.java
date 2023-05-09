package dev.adventurecraft.awakening.common;

public class AC_BlockTable extends AC_BlockSolid {

	protected AC_BlockTable(int var1, int var2) {
		super(var1, var2);
		this.setBoundingBox(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

    @Override
	public int getTextureForSide(int var1, int var2) {
		return var1 <= 1 ? this.texture + var2 : this.texture + 16 + var2;
	}

    @Override
	public boolean isFullOpaque() {
		return false;
	}

    @Override
	public int getRenderType() {
		return 33;
	}
}
