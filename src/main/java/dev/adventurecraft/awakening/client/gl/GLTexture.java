package dev.adventurecraft.awakening.client.gl;

import dev.adventurecraft.awakening.layout.Size;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;

public final class GLTexture extends GLResource {

    private GLTexture(int handle) {
        super(handle);
    }

    GLTexture() {
        this(GL11.glGenTextures());
    }

    public @Override long sizeInBytes() {
        throw new NotImplementedException();
    }

    public static Size getSize(GLTextureTarget target, int level) {
        return new Size(
            GL11.glGetTexLevelParameteri(target.symbol, level, GL11.GL_TEXTURE_WIDTH),
            GL11.glGetTexLevelParameteri(target.symbol, level, GL11.GL_TEXTURE_HEIGHT)
        );
    }
}
