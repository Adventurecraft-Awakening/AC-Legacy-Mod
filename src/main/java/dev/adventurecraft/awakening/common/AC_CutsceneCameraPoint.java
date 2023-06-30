package dev.adventurecraft.awakening.common;

public class AC_CutsceneCameraPoint {

    public float time;
    public float posX;
    public float posY;
    public float posZ;
    public float rotYaw;
    public float rotPitch;
    public AC_CutsceneCameraBlendType blendType;
    public int cameraID;
    public static int startCameraID = 0;

    public AC_CutsceneCameraPoint(float time, float x, float y, float z, float yaw, float pitch, AC_CutsceneCameraBlendType type) {
        this.time = time;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.rotYaw = yaw;
        this.rotPitch = pitch;
        this.blendType = type;
        this.cameraID = startCameraID++;
    }
}
