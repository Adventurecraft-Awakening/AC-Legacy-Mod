package dev.adventurecraft.awakening.extension.client.options;

import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.opengl.GL11;

public interface ExGameOptions {

    boolean ofFogFancy();

    float ofFogStart();

    int ofMipmapLevel();

    boolean ofMipmapLinear();

    int getMipmapType();

    boolean ofLoadFar();

    int ofPreloadedChunks();

    boolean ofOcclusionFancy();

    boolean isOcclusionEnabled();

    boolean isOcclusionFancy();

    boolean ofSmoothFps();

    boolean ofSmoothInput();

    float ofBrightness();

    float ofAoLevel();

    int ofAaLevel();

    int ofAfLevel();

    int ofClouds();

    boolean isCloudsOff();

    boolean isCloudsFancy();

    float ofCloudsHeight();

    int ofLeaves();

    boolean isLeavesFancy();

    int ofGrass();

    boolean isGrassFancy();

    int ofRain();

    boolean isRainOff();

    boolean isRainFancy();

    int ofWater();

    boolean isWaterFancy();

    ConnectedGrassOption ofConnectedGrass();

    int ofAutoSaveTicks();

    boolean ofFastDebugInfo();

    boolean ofWeather();

    boolean ofSky();

    boolean ofStars();

    int ofChunkUpdates();

    boolean ofChunkUpdatesDynamic();

    boolean ofFarView();

    int ofTime();

    boolean isTimeDayOnly();

    boolean isTimeNightOnly();

    boolean ofClearWater();

    int ofAnimatedWater();

    int ofAnimatedLava();

    boolean ofAnimatedFire();

    boolean ofAnimatedPortal();

    boolean ofAnimatedRedstone();

    boolean ofAnimatedExplosion();

    boolean ofAnimatedFlame();

    boolean ofAnimatedSmoke();

    KeyBinding ofKeyBindZoom();

    boolean isGrass3d();

    boolean isAutoFarClip();
}