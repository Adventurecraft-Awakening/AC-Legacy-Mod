package dev.adventurecraft.awakening.extension.client.options;

import dev.adventurecraft.awakening.client.gl.GLMipMode;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import net.minecraft.client.KeyMapping;

public interface ExGameOptions {

    /**
     * Account for chunks trying to access neighbors, be it during simulation or rendering.
     */
    int CHUNK_DISTANCE_BORDER = 2;

    boolean ofFogFancy();

    float ofFogStart();

    int ofMipmapLevel();

    GLMipMode ofMipmapMode();

    boolean ofLoadFar();

    int ofPreloadedChunks();

    int ofChunkRenderDistance();

    int ofChunkSimulationDistance();

    int ofChunkLoadDistance();

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

    KeyMapping ofKeyBindZoom();

    boolean isGrass3d();

    boolean isAutoFarClip();

    int getChatMessageBufferLimit();

    int getParticleLimit();

    boolean getAllowJavaInScript();

    float getChatWidth();
}