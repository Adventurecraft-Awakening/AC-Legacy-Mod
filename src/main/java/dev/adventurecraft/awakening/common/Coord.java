package dev.adventurecraft.awakening.common;

class Coord {
	public int x;
	public int y;
	public int z;

	public Coord() {
		this.set(0, 0, 0);
	}

	public Coord(int var1, int var2, int var3) {
		this.set(var1, var2, var3);
	}

	public void set(int var1, int var2, int var3) {
		this.x = var1;
		this.y = var2;
		this.z = var3;
	}

	public boolean equals(Object var1) {
		if(!(var1 instanceof Coord)) {
			return false;
		} else {
			Coord var2 = (Coord)var1;
			return this.x == var2.x && this.y == var2.y && this.z == var2.z;
		}
	}

	public void min(int var1, int var2, int var3) {
		this.x = Math.min(this.x, var1);
		this.y = Math.min(this.y, var2);
		this.z = Math.min(this.z, var3);
	}

	public void max(int var1, int var2, int var3) {
		this.x = Math.max(this.x, var1);
		this.y = Math.max(this.y, var2);
		this.z = Math.max(this.z, var3);
	}
}
