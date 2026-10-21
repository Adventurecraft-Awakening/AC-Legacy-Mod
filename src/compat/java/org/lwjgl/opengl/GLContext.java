package org.lwjgl.opengl;

/** Original game ABI backed by LWJGL 3's thread-local OpenGL context. */
public final class GLContext {
    private static final ThreadLocal<GLCapabilities> SOURCE = new ThreadLocal<>();
    private static final ThreadLocal<ContextCapabilities> CAPABILITIES = new ThreadLocal<>();

    private GLContext() { }

    public static ContextCapabilities getCapabilities() {
        GLCapabilities current = GL.getCapabilities();
        if (SOURCE.get() != current) {
            SOURCE.set(current);
            CAPABILITIES.set(new ContextCapabilities(current));
        }
        return CAPABILITIES.get();
    }
}
