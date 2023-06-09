package dev.adventurecraft.awakening.extension.entity.player;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;

public interface ExPlayerEntity extends ExLivingEntity {

    boolean isUsingUmbrella();

    void swingOffhandItem();

    float getSwingOffhandProgress(float var1);

    int getHeartPiecesCount();

    void setHeartPiecesCount(int value);

    boolean areSwappedItems();

    void setSwappedItems(boolean value);

    String getCloakTexture();

    void setCloakTexture(String value);
}
