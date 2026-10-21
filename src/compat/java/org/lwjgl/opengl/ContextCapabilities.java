package org.lwjgl.opengl;

/** Original-game capability names backed by the active LWJGL 3 OpenGL context. */
public final class ContextCapabilities {
    public final boolean OpenGL11;
    public final boolean OpenGL12;
    public final boolean OpenGL13;
    public final boolean OpenGL14;
    public final boolean OpenGL15;
    public final boolean OpenGL20;
    public final boolean OpenGL21;
    public final boolean OpenGL30;
    public final boolean OpenGL31;
    public final boolean OpenGL32;
    public final boolean OpenGL33;
    public final boolean OpenGL40;
    public final boolean OpenGL41;
    public final boolean OpenGL42;
    public final boolean OpenGL43;
    public final boolean OpenGL44;
    public final boolean OpenGL45;
    public final boolean OpenGL46;
    public final boolean GL_ARB_vertex_buffer_object;
    public final boolean GL_ARB_occlusion_query;
    public final boolean GL_NV_fog_distance;
    public final boolean GL_KHR_debug;
    public final boolean GL_ARB_debug_output;
    public final boolean GL_ARB_query_buffer_object;
    public final boolean GL_EXT_texture_filter_anisotropic;
    public final boolean GL_ARB_multitexture;

    ContextCapabilities(GLCapabilities capabilities) {
        this.OpenGL11 = capabilities.OpenGL11;
        this.OpenGL12 = capabilities.OpenGL12;
        this.OpenGL13 = capabilities.OpenGL13;
        this.OpenGL14 = capabilities.OpenGL14;
        this.OpenGL15 = capabilities.OpenGL15;
        this.OpenGL20 = capabilities.OpenGL20;
        this.OpenGL21 = capabilities.OpenGL21;
        this.OpenGL30 = capabilities.OpenGL30;
        this.OpenGL31 = capabilities.OpenGL31;
        this.OpenGL32 = capabilities.OpenGL32;
        this.OpenGL33 = capabilities.OpenGL33;
        this.OpenGL40 = capabilities.OpenGL40;
        this.OpenGL41 = capabilities.OpenGL41;
        this.OpenGL42 = capabilities.OpenGL42;
        this.OpenGL43 = capabilities.OpenGL43;
        this.OpenGL44 = capabilities.OpenGL44;
        this.OpenGL45 = capabilities.OpenGL45;
        this.OpenGL46 = capabilities.OpenGL46;
        this.GL_ARB_vertex_buffer_object = capabilities.GL_ARB_vertex_buffer_object;
        this.GL_ARB_occlusion_query = capabilities.GL_ARB_occlusion_query;
        this.GL_NV_fog_distance = capabilities.GL_NV_fog_distance;
        this.GL_KHR_debug = capabilities.GL_KHR_debug;
        this.GL_ARB_debug_output = capabilities.GL_ARB_debug_output;
        this.GL_ARB_query_buffer_object = capabilities.GL_ARB_query_buffer_object;
        this.GL_EXT_texture_filter_anisotropic = capabilities.GL_EXT_texture_filter_anisotropic;
        this.GL_ARB_multitexture = capabilities.GL_ARB_multitexture;
    }
}
