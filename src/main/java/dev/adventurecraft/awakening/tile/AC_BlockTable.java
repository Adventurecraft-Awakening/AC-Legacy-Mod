package dev.adventurecraft.awakening.tile;

public class AC_BlockTable extends AC_BlockSolid {

	protected AC_BlockTable(int var1, int var2) {
		super(var1, var2);
		this.setShape(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

    @Override
	public int getTexture(int var1, int var2) {
		return var1 <= 1 ? this.tex + var2 : this.tex + 16 + var2;
	}

    @Override
	public boolean isSolidRender() {
		return false;
	}

    @Override
	public int getRenderShape() {
		return 33;
	}
}
