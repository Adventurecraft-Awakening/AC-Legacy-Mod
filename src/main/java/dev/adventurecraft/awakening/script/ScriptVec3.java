package dev.adventurecraft.awakening.script;

public class ScriptVec3 {
    
	public double x;
	public double y;
	public double z;

	public ScriptVec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public ScriptVec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public ScriptVec3 add(ScriptVec3 vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public ScriptVec3 subtract(ScriptVec3 vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}

    public ScriptVec3 scale(double value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        return this;
    }

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public double distance(ScriptVec3 vec) {
		double dX = this.x - vec.x;
		double dY = this.y - vec.y;
		double dZ = this.z - vec.z;
		return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
	}

    public double getR() {
        return this.x;
    }

    public void setR(double value) {
        this.x = value;
    }

    public double getG() {
        return this.y;
    }

    public void setG(double value) {
        this.y = value;
    }

    public double getB() {
        return this.z;
    }

    public void setB(double value) {
        this.z = value;
    }
}
