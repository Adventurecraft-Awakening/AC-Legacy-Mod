package dev.adventurecraft.awakening.extension.item;

public interface ExItemStack {

    boolean getReloading();

    void setReloading(boolean value);

    boolean getJustReloaded();

    void setJustReloaded(boolean value);

    int getTimeLeft();

    void setTimeLeft(int value);
}
