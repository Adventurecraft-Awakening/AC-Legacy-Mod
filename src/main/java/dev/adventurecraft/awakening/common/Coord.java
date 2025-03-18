package dev.adventurecraft.awakening.common;

public class Coord {
	public int x;
	public int y;
	public int z;

	public Coord() {
		this.set(0, 0, 0);
	}

	public Coord(int x, int y, int z) {
		this.set(x, y, z);
	}

	public void set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public boolean equals(Object obj) {
        if (obj instanceof Coord c) {
            return this.x == c.x && this.y == c.y && this.z == c.z;
        }
        return false;
    }

	public void min(int x, int y, int z) {
		this.x = Math.min(this.x, x);
		this.y = Math.min(this.y, y);
		this.z = Math.min(this.z, z);
	}

	public void max(int x, int y, int z) {
		this.x = Math.max(this.x, x);
		this.y = Math.max(this.y, y);
		this.z = Math.max(this.z, z);
	}
}
