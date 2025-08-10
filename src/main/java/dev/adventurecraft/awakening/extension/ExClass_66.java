package dev.adventurecraft.awakening.extension;

import dev.adventurecraft.awakening.client.renderer.ChunkMesh;

import javax.annotation.Nullable;
import java.util.List;

public interface ExClass_66 {

    void setVisibleFromPosition(double x, double y, double z, boolean value);

    boolean isVisibleFromPosition();

    void isVisibleFromPosition(boolean value);

    double visibleFromX();

    void setVisibleFromX(double x);

    double visibleFromY();

    void setVisibleFromY(double y);

    double visibleFromZ();

    void setVisibleFromZ(double z);

    boolean isInFrustrumFully();

    @Nullable
    List<ChunkMesh> getRenderList(int layer);
}
