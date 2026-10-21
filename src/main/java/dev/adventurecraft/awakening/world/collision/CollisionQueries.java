package dev.adventurecraft.awakening.world.collision;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collision presence queries. Movement still uses Level.getCubes' complete ordered list.
 * A block producer always finishes before a presence query can stop, including custom
 * producers which append several boxes. Producers after the first hit are not called.
 */
public final class CollisionQueries<E> {

    // Only for legacy call sites that immediately consume List.size()/isEmpty().
    private static final List<AABB> PRESENT = Collections.singletonList(null);

    private final Source<E> source;
    private final ArrayList<Query<E>> queries = new ArrayList<>();
    private int depth;
    private long queryCount;
    private long fallbackCount;
    private long visitedColumns;
    private long visitedBlocks;
    private long blockShapeCalls;
    private long entityCandidates;
    private long entityShapeCalls;

    public CollisionQueries(Source<E> source) {
        this.source = source;
    }

    public boolean hasCollision(E entity, AABB area) {
        int index = this.depth++;
        if (index == this.queries.size()) {
            this.queries.add(new Query<>());
        }
        Query<E> query = this.queries.get(index);
        this.queryCount++;
        try {
            int minX = Mth.floor(area.x0);
            int maxX = Mth.floor(area.x1 + 1.0);
            int minY = Mth.floor(area.y0);
            int maxY = Mth.floor(area.y1 + 1.0);
            int minZ = Mth.floor(area.z0);
            int maxZ = Mth.floor(area.z1 + 1.0);
            // Preserve the extra row below the query (fences), X/Z/Y order and
            // the original loaded-column test. Shapes can extend outside a cell.
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    this.visitedColumns++;
                    if (!this.source.hasChunkAt(x, 64, z)) {
                        continue;
                    }
                    for (int y = minY - 1; y < maxY; y++) {
                        this.visitedBlocks++;
                        if (this.source.addBlockCollisions(entity, x, y, z, area, query.boxes)) {
                            this.blockShapeCalls++;
                        }
                        if (!query.boxes.isEmpty()) {
                            return true;
                        }
                    }
                }
            }

            // Level.getEntities uses shared scratch. Own the selected references
            // before entity producers can perform another collision query.
            List<E> selected = this.source.getEntities(entity, area.inflate(0.25, 0.25, 0.25));
            for (int i = 0; i < selected.size(); i++) {
                query.entities.add(selected.get(i));
            }
            for (int i = 0; i < query.entities.size(); i++) {
                E other = query.entities.get(i);
                this.entityCandidates++;
                this.entityShapeCalls++;
                AABB box = this.source.getCollideBox(other);
                if (box != null && box.intersects(area)) {
                    return true;
                }
                this.entityShapeCalls++;
                box = this.source.getCollideAgainstBox(entity, other);
                if (box != null && box.intersects(area)) {
                    return true;
                }
            }
            return false;
        }
        finally {
            query.release();
            this.depth--;
            // Rare deep reentrancy must not leave an unbounded scratch stack.
            if (this.depth == 0) {
                while (this.queries.size() > 4) {
                    this.queries.removeLast();
                }
            }
        }
    }

    public void recordFallback() {
        this.queryCount++;
        this.fallbackCount++;
    }

    public Statistics statistics() {
        return new Statistics(this.queryCount, this.fallbackCount, this.visitedColumns, this.visitedBlocks,
            this.blockShapeCalls, this.entityCandidates, this.entityShapeCalls);
    }

    public static List<AABB> presenceList(boolean present) {
        return present ? PRESENT : Collections.emptyList();
    }

    public interface Source<E> {
        boolean hasChunkAt(int x, int y, int z);

        /** Returns whether a non-null, permitted block producer was dispatched. */
        boolean addBlockCollisions(E entity, int x, int y, int z, AABB area, ArrayList<AABB> output);

        List<E> getEntities(E entity, AABB area);

        AABB getCollideBox(E entity);

        AABB getCollideAgainstBox(E entity, E other);
    }

    public record Statistics(long queries, long fallbacks, long visitedColumns, long visitedBlocks,
                             long blockShapeCalls, long entityCandidates, long entityShapeCalls) {
    }

    private static final class Query<E> {
        private ArrayList<AABB> boxes = new ArrayList<>();
        private ArrayList<E> entities = new ArrayList<>();

        private void release() {
            if (this.boxes.size() > 64) {
                this.boxes = new ArrayList<>();
            }
            else {
                this.boxes.clear();
            }
            if (this.entities.size() > 256) {
                this.entities = new ArrayList<>();
            }
            else {
                this.entities.clear();
            }
        }
    }
}
