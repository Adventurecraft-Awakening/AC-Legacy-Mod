package dev.adventurecraft.awakening.extension.client;

import dev.adventurecraft.awakening.common.AC_CutsceneCamera;
import dev.adventurecraft.awakening.common.AC_GuiStore;
import dev.adventurecraft.awakening.common.AC_MapList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public interface ExMinecraft {

    World getWorld(String var1, long var2, String var4);

    void startWorld(String var1, String var2, long var3, String var5);

    String getMapUsed(String var1);

    void saveMapUsed(String var1, String var2);

    long getAvgFrameTime();

    void updateStoreGUI();

    boolean isCameraActive();

    void setCameraActive(boolean value);

    boolean isCameraPause();

    void setCameraPause(boolean value);

    AC_CutsceneCamera getCutsceneCamera();

    void setCutsceneCamera(AC_CutsceneCamera value);

    AC_CutsceneCamera getActiveCutsceneCamera();

    void setActiveCutsceneCamera(AC_CutsceneCamera value);

    LivingEntity getCutsceneCameraEntity();

    AC_GuiStore getStoreGUI();

    AC_MapList getMapList();
}
