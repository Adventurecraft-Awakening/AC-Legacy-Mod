package dev.adventurecraft.awakening.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class CustomForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    private String prefix;
    private long counter;

    public CustomForkJoinWorkerThreadFactory() {
        super();
    }

    public CustomForkJoinWorkerThreadFactory name(String prefix, long counter) {
        this.prefix = prefix;
        this.counter = counter;
        return this;
    }

    public String nextName() {
        String name = this.prefix;
        if (name == null) {
            name = "Custom-ForkJoin-Thread";
        }
        if (this.counter >= 0) {
            name = name + "-" + this.counter;
            this.counter++;
        }
        return name;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        var thread = new Thread(pool);
        thread.setName(this.nextName());
        return thread;
    }

    static class Thread extends ForkJoinWorkerThread {
        Thread(ForkJoinPool pool) {
            super(pool);
        }
    }
}