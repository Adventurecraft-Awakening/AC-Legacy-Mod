package dev.adventurecraft.awakening.extension.client;

import dev.adventurecraft.awakening.common.AC_CutsceneCamera;
import dev.adventurecraft.awakening.common.gui.AC_GuiStore;
import dev.adventurecraft.awakening.common.AC_MapList;
import java.net.URL;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface ExMinecraft {

    Level getWorld(String saveName, long seed, String mapName);

    void startWorld(String worldName, String saveName, long seed, String mapName);

    String getMapUsed(String worldName);

    void saveMapUsed(String worldName, String mapName);

    void loadSoundFromDir(String id, URL url);

    double getFrameTime();

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
