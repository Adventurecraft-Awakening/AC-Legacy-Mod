package dev.adventurecraft.awakening.common;

public class AC_BlockChain extends AC_BlockRope {

	protected AC_BlockChain(int var1, int var2) {
		super(var1, var2);
	}

    @Override
	public int getTexture(int var1, int var2) {
		return this.tex + var1 % 2 + var2 / 3 * 2;
	}
}
