package org.lwjgl.opengl;

/**
 * <p/>
 * Thrown by the debug build library of the LWJGL if any OpenGL operation causes an error.
 *
 * @author cix_foo <cix_foo@users.sourceforge.net>
 * @version $Revision$
 * $Id$
 */
public class OpenGLException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Constructor for OpenGLException. */
    public OpenGLException(int gl_error_code) {
        this(createErrorMessage(gl_error_code));
    }

    private static String createErrorMessage(int gl_error_code) {
        String error_string = Util.translateGLErrorString(gl_error_code);
        return error_string + " (" + gl_error_code + ")";
    }

    /** Constructor for OpenGLException. */
    public OpenGLException() {
        super();
    }

    /**
     * Constructor for OpenGLException.
     *
     * @param message
     */
    public OpenGLException(String message) {
        super(message);
    }

    /**
     * Constructor for OpenGLException.
     *
     * @param message
     * @param cause
     */
    public OpenGLException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for OpenGLException.
     *
     * @param cause
     */
    public OpenGLException(Throwable cause) {
        super(cause);
    }

}