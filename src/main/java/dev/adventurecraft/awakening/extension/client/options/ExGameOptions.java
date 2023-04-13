package dev.adventurecraft.awakening.extension.client.options;

public interface ExGameOptions {

    boolean ofFogFancy();

    float ofFogStart();

    int ofMipmapLevel();

    boolean ofMipmapLinear();

    boolean ofLoadFar();

    int ofPreloadedChunks();

    boolean ofOcclusionFancy();

    boolean ofSmoothFps();

    boolean ofSmoothInput();

    float ofBrightness();

    float ofAoLevel();

    int ofAaLevel();

    int ofAfLevel();

    int ofClouds();

    float ofCloudsHeight();

    int ofTrees();

    int ofGrass();

    int ofRain();

    int ofWater();

    int ofBetterGrass();

    int ofAutoSaveTicks();

    boolean ofFastDebugInfo();

    boolean ofWeather();

    boolean ofSky();

    boolean ofStars();

    int ofChunkUpdates();

    boolean ofChunkUpdatesDynamic();

    boolean ofFarView();

    int ofTime();

    boolean ofClearWater();

    int ofAnimatedWater();

    int ofAnimatedLava();

    boolean ofAnimatedFire();

    boolean ofAnimatedPortal();

    boolean ofAnimatedRedstone();

    boolean ofAnimatedExplosion();

    boolean ofAnimatedFlame();

    boolean ofAnimatedSmoke();
}