package org.lwjgl.util.vector;

import java.io.Serializable;
import java.nio.FloatBuffer;

/**
 *
 * Base class for vectors.
 *
 * @author cix_foo <cix_foo@users.sourceforge.net>
 * @version $Revision$
 * $Id$
 */
public sealed abstract class Vector<T extends Vector<T>>
    implements Serializable, ReadableVector
    permits Vector2f, Vector3f, Vector4f
{
    /**
     * Constructor for Vector.
     */
    protected Vector() {
        super();
    }

    protected abstract T create();

    /**
     * @return the length of the vector
     */
    public final float length() {
        return (float) Math.sqrt(lengthSquared());
    }


    /**
     * @return the length squared of the vector
     */
    public abstract float lengthSquared();

    /**
     * Load this vector from a FloatBuffer
     * @param buf The buffer to load it from, at the current position
     * @return this
     */
    public abstract T load(FloatBuffer buf);

    /**
     * Negate this vector
     * @param dest The destination vector or null if a new vector is to be created
     * @return dest
     */
    public abstract T negate(T dest);


    /**
     * Negate this vector
     * @return this
     */
    public T negate() {
        //noinspection unchecked
        return negate((T) this);
    }


    /**
     * Normalise this vector and place the result in another vector.
     * @param dest The destination vector, or null if a new vector is to be created
     * @return dest
     */
    public T normalise(T dest) {
        if (dest == null) {
            dest = create();
        }
        float len = length();
        return divide(len, dest);
    }


    /**
     * Normalise this vector
     * @return this
     */
    public T normalise() {
        //noinspection unchecked
        return normalise((T) this);
    }


    /**
     * Store this vector in a FloatBuffer
     * @param buf The buffer to store it in, at the current position
     * @return this
     */
    public abstract T store(FloatBuffer buf);


    /**
     * Scale this vector
     * @param scale The scale factor
     * @param dest The destination vector
     * @return dest
     */
    public abstract T scale(float scale, T dest);

    public T scale(float scale) {
        //noinspection unchecked
        return scale(scale, (T) this);
    }

    /**
     * Divide this vector
     * @param scale The divisor
     * @param dest The destination vector
     * @return dest
     */
    public abstract T divide(float scale, T dest);

    /**
     * Divide this vector
     * @param scale The divisor
     * @return this
     */
    public T divide(float scale) {
        //noinspection unchecked
        return divide(scale, (T) this);
    }
}