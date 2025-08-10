package dev.adventurecraft.awakening.client.gl;

public abstract sealed class GLResource permits GLBuffer {

    private static final int INVALID_HANDLE = Integer.MIN_VALUE;

    private int handle;

    protected GLResource(int handle) {
        this.handle = handle;
    }

    public final int handle() {
        this.checkHandle();
        return this.handle;
    }

    public final int takeHandle() {
        int handle = this.handle();
        this.handle = INVALID_HANDLE;
        return handle;
    }

    public abstract long sizeInBytes();

    protected abstract void delete();

    protected void checkHandle() {
        if (this.handle == INVALID_HANDLE) {
            throw new IllegalStateException("Resource is disposed.");
        }
    }
}
