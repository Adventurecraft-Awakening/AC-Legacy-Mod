package dev.adventurecraft.awakening.script;

public class ScriptVec3 {
	public double x;
	public double y;
	public double z;

	public ScriptVec3(double var1, double var3, double var5) {
		this.x = var1;
		this.y = var3;
		this.z = var5;
	}

	public ScriptVec3(float var1, float var2, float var3) {
		this.x = var1;
		this.y = var2;
		this.z = var3;
	}

	public ScriptVec3 add(ScriptVec3 var1) {
		this.x += var1.x;
		this.y += var1.y;
		this.z += var1.z;
		return this;
	}

	public ScriptVec3 subtract(ScriptVec3 var1) {
		this.x -= var1.x;
		this.y -= var1.y;
		this.z -= var1.z;
		return this;
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public double distance(ScriptVec3 var1) {
		double var2 = this.x - var1.x;
		double var4 = this.y - var1.y;
		double var6 = this.z - var1.z;
		return Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
	}

	public ScriptVec3 scale(double var1) {
		this.x *= var1;
		this.y *= var1;
		this.z *= var1;
		return this;
	}
}
