package dev.adventurecraft.awakening.common;

public class AC_BlockChain extends AC_BlockRope {

	protected AC_BlockChain(int var1, int var2) {
		super(var1, var2);
	}

    @Override
	public int getTextureForSide(int var1, int var2) {
		return this.texture + var1 % 2 + var2 / 3 * 2;
	}
}
