package dev.adventurecraft.awakening.common;

public class AC_CutsceneCameraPoint {
	public float time;
	public float posX;
	public float posY;
	public float posZ;
	public float rotYaw;
	public float rotPitch;
	public int cameraBlendType;
	public int cameraID;
	public static int startCameraID = 0;
	public static final int NONE = 0;
	public static final int LINEAR = 1;
	public static final int QUADRATIC = 2;

	public AC_CutsceneCameraPoint(float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
		this.time = var1;
		this.posX = var2;
		this.posY = var3;
		this.posZ = var4;
		this.rotYaw = var5;
		this.rotPitch = var6;
		this.cameraBlendType = var7;
		this.cameraID = startCameraID++;
	}
}
