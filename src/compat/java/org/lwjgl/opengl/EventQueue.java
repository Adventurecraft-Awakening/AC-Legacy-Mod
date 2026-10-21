package org.lwjgl.opengl;

/**
 * A java implementation of a LWJGL compatible event queue.
 * @author elias_naur
 */

import java.nio.ByteBuffer;

public class EventQueue {
    private static final int QUEUE_SIZE = 200;

    private final int event_size;

    private final ByteBuffer queue;

    public EventQueue(int event_size) {
        this.event_size = event_size;
        this.queue = ByteBuffer.allocate(QUEUE_SIZE*event_size);
    }

    public synchronized void clearEvents() {
        queue.clear();
    }

    /**
     * Copy available events into the specified buffer.
     */
    public synchronized void copyEvents(ByteBuffer dest) {
        queue.flip();
        int old_limit = queue.limit();
        if (dest.remaining() < queue.remaining())
            queue.limit(dest.remaining() + queue.position());
        dest.put(queue);
        queue.limit(old_limit);
        queue.compact();
    }

    /**
     * Put an event into the queue.
     * @return true if the event fitted into the queue, false otherwise
     */
    public synchronized boolean putEvent(ByteBuffer event) {
        if (event.remaining() != event_size)
            throw new IllegalArgumentException("Internal error: event size " + event_size + " does not equal the given event size " + event.remaining());
        if (queue.remaining() >= event.remaining()) {
            queue.put(event);
            return true;
        } else
            return false;
    }
}