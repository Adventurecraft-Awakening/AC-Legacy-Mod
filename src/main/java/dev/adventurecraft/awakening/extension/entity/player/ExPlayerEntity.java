package dev.adventurecraft.awakening.extension.entity.player;

import dev.adventurecraft.awakening.entity.player.GameMode;
import dev.adventurecraft.awakening.extension.entity.ExMob;

public interface ExPlayerEntity extends ExMob {

    boolean isUsingUmbrella();

    void swingOffhandItem();

    float getSwingOffhandProgress(float var1);

    int getHeartPiecesCount();

    void setHeartPiecesCount(int value);

    boolean areSwappedItems();

    void setSwappedItems(boolean value);

    String getCloakTexture();

    void setCloakTexture(String value);

    GameMode getGameMode();
}
