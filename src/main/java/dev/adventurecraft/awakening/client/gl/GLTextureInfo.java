package dev.adventurecraft.awakening.client.gl;

import dev.adventurecraft.awakening.layout.Size;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GLTextureInfo {

    public final String name;
    public final int id;
    public final Size size;
    public final int format;
    public final int baseLevel;
    public final int maxLevel;
    public final GLMipMode mipMode;
    public final GLFilter minFilter;
    public final GLFilter magFilter;

    public GLTextureInfo(GLTextureTarget target, String name, int id) {
        this.name = name;
        this.id = id;

        this.size = GLTexture.getSize(target, 0);
        this.format = GL11.glGetTexLevelParameteri(target.symbol, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);

        this.baseLevel = GL11.glGetTexParameteri(target.symbol, GL12.GL_TEXTURE_BASE_LEVEL);
        this.maxLevel = GL11.glGetTexParameteri(target.symbol, GL12.GL_TEXTURE_MAX_LEVEL);

        int minSymbol = GL11.glGetTexParameteri(target.symbol, GL11.GL_TEXTURE_MIN_FILTER);
        this.mipMode = GLMipMode.fromFilterSymbol(minSymbol);
        this.minFilter = GLFilter.fromSymbol(minSymbol);
        this.magFilter = GLFilter.fromSymbol(GL11.glGetTexParameteri(target.symbol, GL11.GL_TEXTURE_MAG_FILTER));
    }

    public void restoreParameters(GLTextureTarget target) {
        GL11.glTexParameteri(target.symbol, GL12.GL_TEXTURE_BASE_LEVEL, this.baseLevel);
        GL11.glTexParameteri(target.symbol, GL12.GL_TEXTURE_MAX_LEVEL, this.maxLevel);

        GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_MIN_FILTER, this.minFilter.getMinSymbol(this.mipMode));
        GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_MAG_FILTER, this.magFilter.getMagSymbol(this.mipMode));
    }

    @Override
    public String toString() {
        return "GLTextureInfo{name='%s', id=%d, size=%s, format=%d, baseLevel=%d, maxLevel=%d, mipMode=%s, minFilter=%s, magFilter=%s}".formatted(
            this.name,
            this.id,
            this.size,
            this.format,
            this.baseLevel,
            this.maxLevel,
            this.mipMode,
            this.minFilter,
            this.magFilter
        );
    }
}