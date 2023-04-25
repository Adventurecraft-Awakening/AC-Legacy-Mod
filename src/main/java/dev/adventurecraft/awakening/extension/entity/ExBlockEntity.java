package dev.adventurecraft.awakening.extension.entity;

public interface ExBlockEntity {

    String getClassName();

    boolean isKilledFromSaving();

    void setKilledFromSaving(boolean value);
}
