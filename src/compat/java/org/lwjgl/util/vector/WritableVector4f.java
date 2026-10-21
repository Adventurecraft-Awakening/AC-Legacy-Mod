package org.lwjgl.util.vector;

/**
 * Writable interface to Vector4fs
 * @author $author$
 * @version $revision$
 * $Id$
 */
public interface WritableVector4f extends WritableVector3f {

    /**
     * Set the W value
     * @param w
     */
    void setW(float w);

    /**
     * Set the X,Y,Z,W values
     * @param x
     * @param y
     * @param z
     * @param w
     */
    void set(float x, float y, float z, float w);

}