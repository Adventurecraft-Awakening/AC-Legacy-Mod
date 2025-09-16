package dev.adventurecraft.awakening.extension;

import dev.adventurecraft.awakening.client.renderer.ChunkMesh;
import net.minecraft.client.renderer.Tesselator;

import javax.annotation.Nullable;
import java.util.List;

public interface ExClass_66 {

    void ac$renderQueryBox(Tesselator ts, double x, double y, double z);

    void setVisibleFromPosition(double x, double y, double z, boolean value);

    boolean isVisibleFromPosition();

    void isVisibleFromPosition(boolean value);

    double visibleFromX();

    double visibleFromY();

    double visibleFromZ();

    boolean isInFrustrumFully();

    @Nullable
    List<ChunkMesh> getRenderList(int layer);
}
