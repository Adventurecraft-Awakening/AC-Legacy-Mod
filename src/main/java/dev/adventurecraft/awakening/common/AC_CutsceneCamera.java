package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.adventurecraft.awakening.entity.AC_EntityCamera;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class AC_CutsceneCamera {

    private final Level level;

    public long startTime;
    public AC_CutsceneCameraPoint curPoint = new AC_CutsceneCameraPoint(
        0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, AC_CutsceneCameraBlendType.NONE);
    public AC_CutsceneCameraPoint prevPrevPoint;
    public AC_CutsceneCameraPoint prevPoint;

    public ArrayList<AC_CutsceneCameraPoint> cameraPoints = new ArrayList<>();
    public ArrayList<Vec3> linePoints = new ArrayList<>();
    public AC_CutsceneCameraBlendType startType = AC_CutsceneCameraBlendType.QUADRATIC;

    public AC_CutsceneCamera(Level level) {
        this.level = Objects.requireNonNull(level);
    }

    public void addCameraPoint(
        float time, float x, float y, float z, float yaw, float pitch, AC_CutsceneCameraBlendType type) {
        int pointCount = 0;

        for (AC_CutsceneCameraPoint var10 : this.cameraPoints) {
            if (time < var10.time) {
                break;
            }
            ++pointCount;
        }

        this.cameraPoints.add(pointCount, new AC_CutsceneCameraPoint(time, x, y, z, yaw, pitch, type));
        this.fixYawPitch(0.0F, 0.0F);
    }

    public void loadCameraEntities() {
        for (Entity entity : (List<Entity>) this.level.entities) {
            if (entity instanceof AC_EntityCamera) {
                entity.remove();
            }
        }

        for (AC_CutsceneCameraPoint point : this.cameraPoints) {
            var camera = new AC_EntityCamera(this.level, point.time, point.blendType, point.cameraID);
            camera.absMoveTo(point.posX, point.posY, point.posZ, point.rotYaw, point.rotPitch);
            this.level.addEntity(camera);
        }

        var camera = new AC_CutsceneCamera(this.level);

        for (AC_CutsceneCameraPoint point : this.cameraPoints) {
            camera.addCameraPoint(
                point.time, point.posX, point.posY, point.posZ, point.rotYaw, point.rotPitch, point.blendType);
        }

        AC_CutsceneCameraPoint prevPoint = null;
        this.linePoints.clear();

        for (AC_CutsceneCameraPoint point : this.cameraPoints) {
            if (prevPoint != null) {
                for (int i = 0; i < 25; ++i) {
                    float currTime = (float) (i + 1) / 25.0F;
                    float nextTime = MathF.lerp(currTime, prevPoint.time, point.time);
                    AC_CutsceneCameraPoint nextPoint = camera.getPoint(nextTime);
                    Vec3 linePoint = Vec3.create(nextPoint.posX, nextPoint.posY, nextPoint.posZ);
                    this.linePoints.add(linePoint);
                }
            } else {
                this.linePoints.add(Vec3.create(point.posX, point.posY, point.posZ));
            }
            prevPoint = point;
        }
    }

    public void drawLines(Mob entity, float time) {
        double prX = entity.xOld + (entity.x - entity.xOld) * (double) time;
        double prY = entity.yOld + (entity.y - entity.yOld) * (double) time;
        double prZ = entity.zOld + (entity.z - entity.zOld) * (double) time;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 0.2F, 0.0F, 1.0F);
        GL11.glLineWidth(5.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tesselator ts = Tesselator.instance;
        ts.begin(GL11.GL_LINE_STRIP);

        for (Vec3 linePoint : this.linePoints) {
            ts.vertex(linePoint.x - prX, linePoint.y - prY, linePoint.z - prZ);
        }

        ts.end();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void fixYawPitch(float yaw, float pitch) {
        float yawSum = 0.0F;
        float pitchSum = 0.0F;
        var prevPoint = new AC_CutsceneCameraPoint(
            0.0F, 0.0F, 0.0F, 0.0F, yaw, pitch, AC_CutsceneCameraBlendType.NONE);

        for (AC_CutsceneCameraPoint point : this.cameraPoints) {
            if (prevPoint != null) {
                point.rotYaw += yawSum;
                boolean unfixed = true;

                do {
                    float yawDiff = point.rotYaw - prevPoint.rotYaw;
                    if (yawDiff > 180.0F) {
                        yawSum -= 360.0F;
                        point.rotYaw -= 360.0F;
                    } else if (yawDiff < -180.0F) {
                        yawSum += 360.0F;
                        point.rotYaw += 360.0F;
                    } else {
                        unfixed = false;
                    }
                } while (unfixed);

                point.rotPitch += pitchSum;
                unfixed = true;

                do {
                    float pitchDiff = point.rotPitch - prevPoint.rotPitch;
                    if (pitchDiff > 180.0F) {
                        pitchSum -= 360.0F;
                        point.rotPitch -= 360.0F;
                    } else if (pitchDiff < -180.0F) {
                        pitchSum += 360.0F;
                        point.rotPitch += 360.0F;
                    } else {
                        unfixed = false;
                    }
                } while (unfixed);
            }
            prevPoint = point;
        }
    }

    public void clearPoints() {
        this.prevPrevPoint = null;
        this.prevPoint = null;
        this.cameraPoints.clear();
    }

    public void startCamera() {
        this.prevPrevPoint = null;
        this.prevPoint = null;
        this.startTime = this.level.getTime();
    }

    public boolean isEmpty() {
        return this.cameraPoints.isEmpty();
    }

    public float getLastTime() {
        return this.cameraPoints.get(this.cameraPoints.size() - 1).time;
    }

    public AC_CutsceneCameraPoint getCurrentPoint(float time) {
        float normalizedTime = ((float) (this.level.getTime() - this.startTime) + time) / 20.0F;
        return this.getPoint(normalizedTime);
    }

    public AC_CutsceneCameraPoint getPoint(float time) {
        if (this.prevPoint == null) {
            if (this.cameraPoints.isEmpty()) {
                return this.curPoint;
            }

            if (this.startType != AC_CutsceneCameraBlendType.NONE) {
                LocalPlayer entity = Minecraft.instance.player;
                this.prevPoint = new AC_CutsceneCameraPoint(
                    0.0F, (float) entity.x, (float) entity.y, (float) entity.z, entity.yRot, entity.xRot, this.startType);
                this.fixYawPitch(entity.yRot, entity.xRot);
            } else {
                AC_CutsceneCameraPoint point = this.cameraPoints.get(0);
                this.prevPoint = new AC_CutsceneCameraPoint(
                    0.0F, point.posX, point.posY, point.posZ, point.rotYaw, point.rotPitch, this.startType);
            }
        }

        if (!(this.prevPoint.time <= time) || this.cameraPoints.isEmpty()) {
            return this.prevPoint;
        }

        AC_CutsceneCameraPoint point = this.cameraPoints.get(0);

        while (point != null && point.time < time && !this.cameraPoints.isEmpty()) {
            this.prevPrevPoint = this.prevPoint;
            this.prevPoint = this.cameraPoints.remove(0);
            point = null;
            if (this.cameraPoints.isEmpty()) {
                continue;
            }
            point = this.cameraPoints.get(0);
            if (this.prevPrevPoint == null) {
                continue;
            }

            float timeSincePrev = point.time - this.prevPoint.time;
            float prevTimeSincePrev = this.prevPoint.time - this.prevPrevPoint.time;
            if (prevTimeSincePrev > 0.0F) {
                float factor = timeSincePrev / prevTimeSincePrev;
                this.prevPrevPoint = new AC_CutsceneCameraPoint(
                    0.0F,
                    this.prevPoint.posX - factor * (this.prevPoint.posX - this.prevPrevPoint.posX),
                    this.prevPoint.posY - factor * (this.prevPoint.posY - this.prevPrevPoint.posY),
                    this.prevPoint.posZ - factor * (this.prevPoint.posZ - this.prevPrevPoint.posZ),
                    this.prevPoint.rotYaw - factor * (this.prevPoint.rotYaw - this.prevPrevPoint.rotYaw),
                    this.prevPoint.rotPitch - factor * (this.prevPoint.rotPitch - this.prevPrevPoint.rotPitch),
                    AC_CutsceneCameraBlendType.NONE);
            } else {
                this.prevPrevPoint = new AC_CutsceneCameraPoint(
                    0.0F,
                    this.prevPoint.posX,
                    this.prevPoint.posY,
                    this.prevPoint.posZ,
                    this.prevPoint.rotYaw,
                    this.prevPoint.rotPitch,
                    AC_CutsceneCameraBlendType.NONE);
            }
        }

        if (point == null) {
            return this.prevPoint;
        }

        if (this.prevPrevPoint == null) {
            this.prevPrevPoint = new AC_CutsceneCameraPoint(
                0.0F,
                2.0F * this.prevPoint.posX - point.posX,
                2.0F * this.prevPoint.posY - point.posY,
                2.0F * this.prevPoint.posZ - point.posZ,
                2.0F * this.prevPoint.rotYaw - point.rotYaw,
                2.0F * this.prevPoint.rotPitch - point.rotPitch,
                AC_CutsceneCameraBlendType.NONE);
        }

        AC_CutsceneCameraPoint nextPoint;
        if (this.cameraPoints.size() > 1) {
            nextPoint = this.cameraPoints.get(1);
            float timeSincePrev = point.time - this.prevPoint.time;
            float timeToNext = nextPoint.time - point.time;
            if (timeToNext > 0.0F) {
                float factor = timeSincePrev / timeToNext;
                nextPoint = new AC_CutsceneCameraPoint(
                    0.0F,
                    point.posX + factor * (nextPoint.posX - point.posX),
                    point.posY + factor * (nextPoint.posY - point.posY),
                    point.posZ + factor * (nextPoint.posZ - point.posZ),
                    point.rotYaw + factor * (nextPoint.rotYaw - point.rotYaw),
                    point.rotPitch + factor * (nextPoint.rotPitch - point.rotPitch),
                    AC_CutsceneCameraBlendType.NONE);
            } else {
                nextPoint = new AC_CutsceneCameraPoint(
                    0.0F, point.posX, point.posY, point.posZ, point.rotYaw, point.rotPitch, AC_CutsceneCameraBlendType.NONE);
            }
        } else {
            nextPoint = new AC_CutsceneCameraPoint(
                0.0F,
                2.0F * point.posX - this.prevPoint.posX,
                2.0F * point.posY - this.prevPoint.posY,
                2.0F * point.posZ - this.prevPoint.posZ,
                2.0F * point.rotYaw - this.prevPoint.rotYaw,
                2.0F * point.rotPitch - this.prevPoint.rotPitch,
                AC_CutsceneCameraBlendType.NONE);
        }

        float amount = (time - this.prevPoint.time) / (point.time - this.prevPoint.time);
        this.curPoint.time = time;

        switch (this.prevPoint.blendType) {
            case LINEAR:
                this.curPoint.posX = MathF.lerp(amount, this.prevPoint.posX, point.posX);
                this.curPoint.posY = MathF.lerp(amount, this.prevPoint.posY, point.posY);
                this.curPoint.posZ = MathF.lerp(amount, this.prevPoint.posZ, point.posZ);
                this.curPoint.rotYaw = MathF.lerp(amount, this.prevPoint.rotYaw, point.rotYaw);
                this.curPoint.rotPitch = MathF.lerp(amount, this.prevPoint.rotPitch, point.rotPitch);
                break;

            case QUADRATIC:
                //noinspection SuspiciousNameCombination
                this.curPoint.posX = MathF.cubicInterpolation(amount, this.prevPrevPoint.posX, this.prevPoint.posX, point.posX, nextPoint.posX);
                this.curPoint.posY = MathF.cubicInterpolation(amount, this.prevPrevPoint.posY, this.prevPoint.posY, point.posY, nextPoint.posY);
                this.curPoint.posZ = MathF.cubicInterpolation(amount, this.prevPrevPoint.posZ, this.prevPoint.posZ, point.posZ, nextPoint.posZ);
                this.curPoint.rotYaw = MathF.cubicInterpolation(amount, this.prevPrevPoint.rotYaw, this.prevPoint.rotYaw, point.rotYaw, nextPoint.rotYaw);
                this.curPoint.rotPitch = MathF.cubicInterpolation(amount, this.prevPrevPoint.rotPitch, this.prevPoint.rotPitch, point.rotPitch, nextPoint.rotPitch);
                break;

            case NONE:
                this.curPoint.posX = this.prevPoint.posX;
                this.curPoint.posY = this.prevPoint.posY;
                this.curPoint.posZ = this.prevPoint.posZ;
                this.curPoint.rotYaw = this.prevPoint.rotYaw;
                this.curPoint.rotPitch = this.prevPoint.rotPitch;
                break;
        }

        return this.curPoint;
    }

    public void deletePoint(int id) {
        ArrayList<AC_CutsceneCameraPoint> points = this.cameraPoints;
        for (int i = 0; i < points.size(); i++) {
            AC_CutsceneCameraPoint point = points.get(i);
            if (point.cameraID == id) {
                this.cameraPoints.remove(i);
                break;
            }
        }
    }

    public void setPointType(int id, AC_CutsceneCameraBlendType type) {
        for (AC_CutsceneCameraPoint point : this.cameraPoints) {
            if (point.cameraID == id) {
                point.blendType = type;
                this.loadCameraEntities();
                break;
            }
        }
    }

    public void setPointTime(int id, float time) {
        for (AC_CutsceneCameraPoint point : this.cameraPoints) {
            if (point.cameraID == id) {
                point.time = time;
                this.loadCameraEntities();
                break;
            }
        }
    }
}
