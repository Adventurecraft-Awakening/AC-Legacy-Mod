package dev.adventurecraft.awakening.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProxyFuture<V> implements Future<V> {

    private final Future<V> original;

    public ProxyFuture(Future<V> original) {
        this.original = original;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.original.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.original.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.original.isDone();
    }

    @Override
    public V get()
        throws InterruptedException, ExecutionException {
        return this.original.get();
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return this.original.get(timeout, unit);
    }
}
