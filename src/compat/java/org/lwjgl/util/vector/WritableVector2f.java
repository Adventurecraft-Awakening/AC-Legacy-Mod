package org.lwjgl.util.vector;

/**
 * Writable interface to Vector2fs
 * @author $author$
 * @version $revision$
 * $Id$
 */
public interface WritableVector2f {

    /**
     * Set the X value
     * @param x
     */
    void setX(float x);

    /**
     * Set the Y value
     * @param y
     */
    void setY(float y);

    /**
     * Set the X,Y values
     * @param x
     * @param y
     */
    void set(float x, float y);

}