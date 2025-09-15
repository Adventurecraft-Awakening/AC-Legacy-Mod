package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public final class GLDevice {

    private final DeviceInfo info = new DeviceInfo();

    private GLBuffer shortElementCache;
    private GLBuffer intElementCache;

    public GLDevice(ContextCapabilities caps) {
        if (!caps.OpenGL31) {
            throw new UnsupportedOperationException("Unsupported OpenGL version.");
        }
    }

    public DeviceInfo getDeviceInfo() {
        return this.info;
    }

    public GLBuffer newBuffer(long sizeInBytes) {
        var buffer = new GLBuffer(sizeInBytes);
        this.info.bufferAllocatedBytes += sizeInBytes;
        this.info.bufferCount += 1;
        return buffer;
    }

    public void delete(GLBuffer buffer) {
        int handle = buffer.takeHandle();
        GL15.glDeleteBuffers(handle);
        this.info.bufferAllocatedBytes -= buffer.sizeInBytes();
        this.info.bufferCount -= 1;
    }

    public void bind(GLBufferTarget target, GLBuffer buffer) {
        GL15.glBindBuffer(target.id, buffer.handle());
    }

    public void unbind(GLBufferTarget target) {
        GL15.glBindBuffer(target.id, 0);
    }

    public void alloc(GLBufferTarget target, long size, GLBufferUsage usage) {
        GL15.glBufferData(target.id, size, usage.id);
    }

    public void uploadData(GLBufferTarget target, long offset, ByteBuffer data) {
        GL15.glBufferSubData(target.id, offset, data);
    }

    public void copyBuffer(GLBufferTarget src, long srcOffset, GLBufferTarget dst, long dstOffset, long size) {
        GL31.glCopyBufferSubData(src.id, dst.id, srcOffset, dstOffset, size);
    }

    public long bindQuadElements(GLBufferTarget target, GLElementType type, long quadCount) {
        if (type == GLElementType.SHORT) {
            this.shortElementCache = this.computeElementBuffer(target, type, this.shortElementCache, quadCount);
        }
        else if (type == GLElementType.INT) {
            this.intElementCache = this.computeElementBuffer(target, type, this.intElementCache, quadCount);
        }
        else {
            throw new IllegalArgumentException("unsupported index type");
        }
        return byteSizeForQuadElements(type, quadCount);
    }

    private GLBuffer computeElementBuffer(
        GLBufferTarget target,
        GLElementType indexType,
        GLBuffer buffer,
        long minQuadCount
    ) {
        long oldCount = buffer != null ? (buffer.sizeInBytes() / indexType.size) : 0;
        if (buffer != null && oldCount >= minQuadCount) {
            bind(target, buffer);
            return buffer;
        }

        long newCount = switch (indexType) {
            case BYTE, SHORT -> indexType.maxIndex();
            case INT -> {
                long multiple = GLElementType.SHORT.maxIndex();
                yield Math.ceilDiv(minQuadCount, multiple) * multiple;
            }
        };
        if (newCount <= oldCount) {
            throw new IllegalStateException("size limit exceeded");
        }
        return this.createElementBuffer(target, indexType, newCount);
    }

    private GLBuffer createElementBuffer(GLBufferTarget target, GLElementType type, long quadCount) {
        GLBuffer buffer = this.newBuffer(byteSizeForQuadElements(type, quadCount));

        bind(target, buffer);
        alloc(target, buffer.sizeInBytes(), GLBufferUsage.STATIC_COPY);

        int access = GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_INVALIDATE_BUFFER_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT;
        ByteBuffer mapping = GL30.glMapBufferRange(target.id, 0, buffer.sizeInBytes(), access);
        if (mapping == null) {
            throw new IllegalStateException("failed to bind index buffer for writing");
        }

        switch (type) {
            case SHORT -> quadPutShort(mapping, 0, (int) quadCount);
            case INT -> quadPutInt(mapping, 0, (int) quadCount);
            default -> throw new IllegalStateException("unsupported index type");
        }

        GL30.glUnmapBuffer(target.id);
        return buffer;
    }

    private static void quadPutShort(ByteBuffer buffer, int baseVertex, int quadCount) {
        for (int i = 0; i < quadCount; i++) {
            buffer.putShort((short) ((baseVertex + 0) & 0xffff));
            buffer.putShort((short) ((baseVertex + 1) & 0xffff));
            buffer.putShort((short) ((baseVertex + 2) & 0xffff));
            buffer.putShort((short) ((baseVertex + 0) & 0xffff));
            buffer.putShort((short) ((baseVertex + 2) & 0xffff));
            buffer.putShort((short) ((baseVertex + 3) & 0xffff));
            baseVertex += 4;
        }
    }

    private static void quadPutInt(ByteBuffer buffer, int baseVertex, int quadCount) {
        for (int i = 0; i < quadCount; i++) {
            buffer.putInt(baseVertex + 0);
            buffer.putInt(baseVertex + 1);
            buffer.putInt(baseVertex + 2);
            buffer.putInt(baseVertex + 0);
            buffer.putInt(baseVertex + 2);
            buffer.putInt(baseVertex + 3);
            baseVertex += 4;
        }
    }

    private static long byteSizeForQuadElements(GLElementType type, long quadCount) {
        int bytesPerQuad = type.size * 6;
        return quadCount * (long) bytesPerQuad;
    }

    public static class DeviceInfo {
        long bufferAllocatedBytes;
        long bufferCount;

        public long bufferAllocatedBytes() {
            return this.bufferAllocatedBytes;
        }

        public long bufferCount() {
            return this.bufferCount;
        }
    }
}
