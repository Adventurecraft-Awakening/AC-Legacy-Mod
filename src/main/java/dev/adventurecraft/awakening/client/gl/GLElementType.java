package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL11;

public enum GLElementType {

    BYTE(GL11.GL_UNSIGNED_BYTE, 1),
    SHORT(GL11.GL_UNSIGNED_SHORT, 2),
    INT(GL11.GL_UNSIGNED_INT, 4);

    public final int id;
    public final int size;

    GLElementType(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public long minIndex() {
        return 0;
    }

    public long maxIndex() {
        return (-1L) >>> (64 - this.size * 8);
    }

    public static GLElementType fromCount(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("negative count is invalid");
        }

        if (Long.compareUnsigned(count, SHORT.maxIndex()) <= 0) {
            return SHORT;
        }
        if (Long.compareUnsigned(count, INT.maxIndex()) <= 0) {
            return INT;
        }
        throw new IllegalArgumentException("size limit exceeded");
    }
}