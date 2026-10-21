package dev.adventurecraft.awakening.extension;

import dev.adventurecraft.awakening.client.render.chunk.ChunkBuilder;
import dev.adventurecraft.awakening.client.render.chunk.ChunkMesh;
import net.minecraft.client.renderer.Tesselator;

import javax.annotation.Nullable;
import java.util.List;

public interface ExClass_66 {

    void ac$renderQueryBox(Tesselator ts, double x, double y, double z);

    void ac$rebuild(ChunkBuilder builder);

    void ac$readWorldData(ChunkBuilder builder);

    void ac$generateMesh(ChunkBuilder builder);

    void ac$submitMesh(ChunkBuilder builder);

    boolean ac$isMeshCurrent(ChunkBuilder builder);

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
