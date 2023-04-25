package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class AC_CutsceneCamera {
    public long startTime;
    public AC_CutsceneCameraPoint curPoint = new AC_CutsceneCameraPoint(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0);
    public AC_CutsceneCameraPoint prevPrevPoint;
    public AC_CutsceneCameraPoint prevPoint;
    public List<AC_CutsceneCameraPoint> cameraPoints = new ArrayList<>();
    public List<Vec3d> lineVecs = new ArrayList<>();
    public int startType = 2;

    public void addCameraPoint(float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
        int var8 = 0;

        for (AC_CutsceneCameraPoint var10 : this.cameraPoints) {
            if (var1 < var10.time) {
                break;
            }
            ++var8;
        }

        this.cameraPoints.add(var8, new AC_CutsceneCameraPoint(var1, var2, var3, var4, var5, var6, var7));
        this.fixYawPitch(0.0F, 0.0F);
    }

    public void loadCameraEntities() {

        for (Entity var2 : (List<Entity>) Minecraft.instance.world.entities) {
            if (var2 instanceof AC_EntityCamera) {
                var2.remove();
            }
        }

        for (AC_CutsceneCameraPoint var11 : this.cameraPoints) {
            AC_EntityCamera var3 = new AC_EntityCamera(Minecraft.instance.world, var11.time, var11.cameraBlendType, var11.cameraID);
            var3.method_1338(var11.posX, var11.posY, var11.posZ, var11.rotYaw, var11.rotPitch);
            Minecraft.instance.world.spawnEntity(var3);
        }

        AC_CutsceneCamera var10 = new AC_CutsceneCamera();

        for (AC_CutsceneCameraPoint var13 : this.cameraPoints) {
            var10.addCameraPoint(var13.time, var13.posX, var13.posY, var13.posZ, var13.rotYaw, var13.rotPitch, var13.cameraBlendType);
        }

        AC_CutsceneCameraPoint var11 = null;
        this.lineVecs.clear();

        for (AC_CutsceneCameraPoint var4 : this.cameraPoints) {
            if (var11 != null) {
                for (int var6 = 0; var6 < 25; ++var6) {
                    float var5 = (float) (var6 + 1) / 25.0F;
                    float var7 = this.lerp(var5, var11.time, var4.time);
                    AC_CutsceneCameraPoint var8 = var10.getPoint(var7);
                    Vec3d var9 = Vec3d.create(var8.posX, var8.posY, var8.posZ);
                    this.lineVecs.add(var9);
                }
            } else {
                this.lineVecs.add(Vec3d.create(var4.posX, var4.posY, var4.posZ));
            }
            var11 = var4;
        }
    }

    public void drawLines(LivingEntity var1, float var2) {
        double var3 = var1.prevRenderX + (var1.x - var1.prevRenderX) * (double) var2;
        double var5 = var1.prevRenderY + (var1.y - var1.prevRenderY) * (double) var2;
        double var7 = var1.prevRenderZ + (var1.z - var1.prevRenderZ) * (double) var2;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 0.2F, 0.0F, 1.0F);
        GL11.glLineWidth(5.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator var9 = Tessellator.INSTANCE;
        var9.start(3);

        for (Vec3d var11 : this.lineVecs) {
            var9.addVertex(var11.x - var3, var11.y - var5, var11.z - var7);
        }

        var9.tessellate();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void fixYawPitch(float var1, float var2) {
        float var3 = 0.0F;
        float var4 = 0.0F;
        AC_CutsceneCameraPoint var5 = new AC_CutsceneCameraPoint(0.0F, 0.0F, 0.0F, 0.0F, var1, var2, 0);

        for (AC_CutsceneCameraPoint var7 : this.cameraPoints) {
            if (var5 != null) {
                var7.rotYaw += var3;
                boolean var9 = true;

                float var8;
                do {
                    var8 = var7.rotYaw - var5.rotYaw;
                    if (var8 > 180.0F) {
                        var3 -= 360.0F;
                        var7.rotYaw -= 360.0F;
                    } else if (var8 < -180.0F) {
                        var3 += 360.0F;
                        var7.rotYaw += 360.0F;
                    } else {
                        var9 = false;
                    }
                } while (var9);

                var7.rotPitch += var4;
                var9 = true;

                do {
                    var8 = var7.rotPitch - var5.rotPitch;
                    if (var8 > 180.0F) {
                        var4 -= 360.0F;
                        var7.rotPitch -= 360.0F;
                    } else if (var8 < -180.0F) {
                        var4 += 360.0F;
                        var7.rotPitch += 360.0F;
                    } else {
                        var9 = false;
                    }

                } while (var9);
            }
            var5 = var7;
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
        this.startTime = Minecraft.instance.world.getWorldTime();
    }

    private float cubicInterpolation(float var1, float var2, float var3, float var4, float var5) {
        float var6 = var1 * var1;
        float var7 = -0.5F * var2 + 1.5F * var3 - 1.5F * var4 + 0.5F * var5;
        float var8 = var2 - 2.5F * var3 + 2.0F * var4 - 0.5F * var5;
        float var9 = -0.5F * var2 + 0.5F * var4;
        return var7 * var1 * var6 + var8 * var6 + var9 * var1 + var3;
    }

    private float lerp(float var1, float var2, float var3) {
        return (1.0F - var1) * var2 + var1 * var3;
    }

    public boolean isEmpty() {
        return this.cameraPoints.isEmpty();
    }

    public float getLastTime() {
        return this.cameraPoints.get(this.cameraPoints.size() - 1).time;
    }

    public AC_CutsceneCameraPoint getCurrentPoint(float var1) {
        float var2 = ((float) (Minecraft.instance.world.getWorldTime() - this.startTime) + var1) / 20.0F;
        return this.getPoint(var2);
    }

    public AC_CutsceneCameraPoint getPoint(float var1) {
        AC_CutsceneCameraPoint var7;
        if (this.prevPoint == null) {
            if (this.cameraPoints.isEmpty()) {
                return this.curPoint;
            }

            if (this.startType != 0) {
                AbstractClientPlayerEntity var2 = Minecraft.instance.player;
                this.prevPoint = new AC_CutsceneCameraPoint(0.0F, (float) var2.x, (float) var2.y, (float) var2.z, var2.yaw, var2.pitch, this.startType);
                this.fixYawPitch(var2.yaw, var2.pitch);
            } else {
                var7 = this.cameraPoints.get(0);
                this.prevPoint = new AC_CutsceneCameraPoint(0.0F, var7.posX, var7.posY, var7.posZ, var7.rotYaw, var7.rotPitch, this.startType);
            }
        }

        if (this.prevPoint.time <= var1 && !this.cameraPoints.isEmpty()) {
            var7 = this.cameraPoints.get(0);

            float var4;
            float var5;
            while (var7 != null && var7.time < var1 && !this.cameraPoints.isEmpty()) {
                this.prevPrevPoint = this.prevPoint;
                this.prevPoint = this.cameraPoints.remove(0);
                var7 = null;
                if (!this.cameraPoints.isEmpty()) {
                    var7 = this.cameraPoints.get(0);
                    if (this.prevPrevPoint != null) {
                        float var3 = var7.time - this.prevPoint.time;
                        var4 = this.prevPoint.time - this.prevPrevPoint.time;
                        if (var4 > 0.0F) {
                            var5 = var3 / var4;
                            this.prevPrevPoint = new AC_CutsceneCameraPoint(0.0F, this.prevPoint.posX - var5 * (this.prevPoint.posX - this.prevPrevPoint.posX), this.prevPoint.posY - var5 * (this.prevPoint.posY - this.prevPrevPoint.posY), this.prevPoint.posZ - var5 * (this.prevPoint.posZ - this.prevPrevPoint.posZ), this.prevPoint.rotYaw - var5 * (this.prevPoint.rotYaw - this.prevPrevPoint.rotYaw), this.prevPoint.rotPitch - var5 * (this.prevPoint.rotPitch - this.prevPrevPoint.rotPitch), 0);
                        } else {
                            this.prevPrevPoint = new AC_CutsceneCameraPoint(0.0F, this.prevPoint.posX, this.prevPoint.posY, this.prevPoint.posZ, this.prevPoint.rotYaw, this.prevPoint.rotPitch, 0);
                        }
                    }
                }
            }

            if (var7 == null) {
                return this.prevPoint;
            } else {
                if (this.prevPrevPoint == null) {
                    this.prevPrevPoint = new AC_CutsceneCameraPoint(0.0F, 2.0F * this.prevPoint.posX - var7.posX, 2.0F * this.prevPoint.posY - var7.posY, 2.0F * this.prevPoint.posZ - var7.posZ, 2.0F * this.prevPoint.rotYaw - var7.rotYaw, 2.0F * this.prevPoint.rotPitch - var7.rotPitch, 0);
                }

                AC_CutsceneCameraPoint var8;
                if (this.cameraPoints.size() > 1) {
                    var8 = this.cameraPoints.get(1);
                    var4 = var7.time - this.prevPoint.time;
                    var5 = var8.time - var7.time;
                    if (var5 > 0.0F) {
                        float var6 = var4 / var5;
                        var8 = new AC_CutsceneCameraPoint(0.0F, var7.posX + var6 * (var8.posX - var7.posX), var7.posY + var6 * (var8.posY - var7.posY), var7.posZ + var6 * (var8.posZ - var7.posZ), var7.rotYaw + var6 * (var8.rotYaw - var7.rotYaw), var7.rotPitch + var6 * (var8.rotPitch - var7.rotPitch), 0);
                    } else {
                        var8 = new AC_CutsceneCameraPoint(0.0F, var7.posX, var7.posY, var7.posZ, var7.rotYaw, var7.rotPitch, 0);
                    }
                } else {
                    var8 = new AC_CutsceneCameraPoint(0.0F, 2.0F * var7.posX - this.prevPoint.posX, 2.0F * var7.posY - this.prevPoint.posY, 2.0F * var7.posZ - this.prevPoint.posZ, 2.0F * var7.rotYaw - this.prevPoint.rotYaw, 2.0F * var7.rotPitch - this.prevPoint.rotPitch, 0);
                }

                var4 = (var1 - this.prevPoint.time) / (var7.time - this.prevPoint.time);
                this.curPoint.time = var1;
                switch (this.prevPoint.cameraBlendType) {
                    case 1:
                        this.curPoint.posX = this.lerp(var4, this.prevPoint.posX, var7.posX);
                        this.curPoint.posY = this.lerp(var4, this.prevPoint.posY, var7.posY);
                        this.curPoint.posZ = this.lerp(var4, this.prevPoint.posZ, var7.posZ);
                        this.curPoint.rotYaw = this.lerp(var4, this.prevPoint.rotYaw, var7.rotYaw);
                        this.curPoint.rotPitch = this.lerp(var4, this.prevPoint.rotPitch, var7.rotPitch);
                        break;

                    case 2:
                        this.curPoint.posX = this.cubicInterpolation(var4, this.prevPrevPoint.posX, this.prevPoint.posX, var7.posX, var8.posX);
                        this.curPoint.posY = this.cubicInterpolation(var4, this.prevPrevPoint.posY, this.prevPoint.posY, var7.posY, var8.posY);
                        this.curPoint.posZ = this.cubicInterpolation(var4, this.prevPrevPoint.posZ, this.prevPoint.posZ, var7.posZ, var8.posZ);
                        this.curPoint.rotYaw = this.cubicInterpolation(var4, this.prevPrevPoint.rotYaw, this.prevPoint.rotYaw, var7.rotYaw, var8.rotYaw);
                        this.curPoint.rotPitch = this.cubicInterpolation(var4, this.prevPrevPoint.rotPitch, this.prevPoint.rotPitch, var7.rotPitch, var8.rotPitch);
                        break;

                    default:
                        this.curPoint.posX = this.prevPoint.posX;
                        this.curPoint.posY = this.prevPoint.posY;
                        this.curPoint.posZ = this.prevPoint.posZ;
                        this.curPoint.rotYaw = this.prevPoint.rotYaw;
                        this.curPoint.rotPitch = this.prevPoint.rotPitch;
                        break;
                }

                return this.curPoint;
            }
        } else {
            return this.prevPoint;
        }
    }

    public void deletePoint(int var1) {
        AC_CutsceneCameraPoint var2 = null;

        for (AC_CutsceneCameraPoint var4 : this.cameraPoints) {
            if (var4.cameraID == var1) {
                var2 = var4;
                break;
            }
        }

        if (var2 != null) {
            this.cameraPoints.remove(var2);
        }
    }

    public void setPointType(int var1, int var2) {
        Iterator<AC_CutsceneCameraPoint> var3 = this.cameraPoints.iterator();

        AC_CutsceneCameraPoint var4;
        do {
            if (!var3.hasNext()) {
                return;
            }

            var4 = var3.next();
        } while (var4.cameraID != var1);

        var4.cameraBlendType = var2;
        this.loadCameraEntities();
    }

    public void setTime(int var1, float var2) {
        Iterator<AC_CutsceneCameraPoint> var3 = this.cameraPoints.iterator();


        AC_CutsceneCameraPoint var4;
        do {
            if (!var3.hasNext()) {
                return;
            }

            var4 = var3.next();
        } while (var4.cameraID != var1);

        var4.time = var2;
        this.loadCameraEntities();
    }
}
