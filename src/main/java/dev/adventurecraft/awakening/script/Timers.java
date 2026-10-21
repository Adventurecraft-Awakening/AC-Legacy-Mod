package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.script.scheduling.IndexedMinHeap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.mozilla.javascript.*;

import java.util.Arrays;
import java.util.Objects;

public abstract class Timers {

    private int lastId;
    private final Int2ObjectMap<Timeout> timers = new Int2ObjectOpenHashMap<>();
    private final IndexedMinHeap<Timeout> timerQueue = new IndexedMinHeap<>();
    private long scheduled;
    private long cancelled;
    private long callbacks;
    private long skipped;
    private long overwritten;
    private boolean reusedIds;

    protected abstract long getTimeMillis();

    public void install(Scriptable scope) {
        installMethod(scope, "setTimeout", this::setTimeout);
        installMethod(scope, "clearTimeout", this::clearTimeout);
    }

    private void installMethod(Scriptable scope, String name, SerializableCallable callable) {
        var func = new LambdaFunction(scope, name, 1, callable);
        ScriptableObject.defineProperty(scope, name, func, ScriptableObject.DONTENUM);
    }

    public void runTimers(Context cx, Scriptable scope) {
        Timeout first = this.timerQueue.peek();
        if (first == null) return;
        long time = this.getTimeMillis();
        if (!first.isDue(time)) return;

        // Collect first so callbacks can schedule/cancel timers without mutating an iterator.
        // The detached chain belongs to this invocation, so nested runs get their own batch.
        Timeout due = null;
        Timeout tail = null;
        while ((first = this.timerQueue.peek()) != null && first.isDue(time)) {
            Timeout timeout = this.timerQueue.poll();
            timeout.detached = true;
            if (tail == null) due = timeout;
            else tail.nextDue = timeout;
            tail = timeout;
        }
        try {
            while (due != null) {
                Timeout timeout = due;
                due = timeout.nextDue;
                timeout.nextDue = null;
                timeout.detached = false;
                if (this.timers.remove(timeout.id, timeout)) {
                    Function function = timeout.func;
                    Object[] arguments = timeout.funcArgs;
                    timeout.release();
                    this.callbacks++;
                    this.invokeCallback(cx, scope, function, arguments);
                }
                else {
                    timeout.release();
                    this.skipped++;
                }
            }
        }
        finally {
            // A throwing callback leaves later detached timers in the ID map, as before.
            // Break only the temporary links; explicit cancellation can still release them.
            while (due != null) {
                Timeout next = due.nextDue;
                due.nextDue = null;
                due.detached = false;
                if (this.timers.get(due.id) != due) due.release();
                due = next;
            }
        }
    }

    protected void invokeCallback(Context cx, Scriptable scope, Function callback, Object[] arguments) {
        cx.processMicrotasks();
        callback.call(cx, scope, scope, arguments);
    }

    public void clear() {
        this.cancelled += this.timers.size();
        for (Timeout timeout : this.timers.values()) {
            // Detached entries still participate in the legacy record-equality check.
            // Keep them until their batch finishes in case an ID is immediately reused.
            if (!timeout.detached) timeout.release();
        }
        this.timers.clear();
        this.timerQueue.clear(Timeout::release);
        this.reusedIds = false;
    }

    public Statistics statistics() {
        return new Statistics(this.scheduled, this.cancelled, this.callbacks, this.skipped, this.overwritten,
            this.timerQueue.size(), this.timers.size(), this.timerQueue.comparisons(), this.timerQueue.removals(),
            this.timerQueue.equalityTests());
    }

    public record Statistics(long scheduled, long cancelled, long callbacks, long skipped, long overwritten,
                             int queued, int live, long heapComparisons, long heapRemovals, long heapEqualityTests) { }

    private Object setTimeout(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (args.length == 0) {
            throw ScriptRuntime.typeError("Expected function parameter");
        }
        if (!(args[0] instanceof Function func)) {
            throw ScriptRuntime.typeError("Expected first argument to be a function");
        }

        long expiration = 0;
        if (args.length > 1) {
            int delay = ScriptRuntime.toInt32(args[1]);
            expiration = delay + this.getTimeMillis();
        }

        Object[] funcArgs = ScriptRuntime.emptyArgs;
        if (args.length > 2) {
            funcArgs = Arrays.copyOfRange(args, 2, args.length);
        }

        int id = ++this.lastId;
        var t = new Timeout(id, expiration, func, funcArgs);

        Timeout previous = this.timers.put(id, t);
        if (previous != null) {
            // Preserve queue orphans and value equality if the signed ID counter wraps.
            this.reusedIds = true;
            this.overwritten++;
        }
        this.timerQueue.add(t);
        this.scheduled++;
        return id;
    }

    private Object clearTimeout(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (args.length == 1) {
            int id = ScriptRuntime.toInt32(args[0]);
            Timeout t = this.timers.remove(id);
            if (t != null) {
                Timeout removed = this.reusedIds ? this.timerQueue.removeFirstEqual(t) :
                    this.timerQueue.remove(t) ? t : null;
                if (removed != null) removed.release();
                else if (!t.detached) t.release();
                this.cancelled++;
            }
        }
        return Undefined.instance;
    }

    private static final class Timeout extends IndexedMinHeap.Entry<Timeout> {
        final int id;
        final long expiration;
        Function func;
        Object[] funcArgs;
        Timeout nextDue;
        boolean detached;

        Timeout(int id, long expiration, Function func, Object[] funcArgs) {
            this.id = id;
            this.expiration = expiration;
            this.func = func;
            this.funcArgs = funcArgs;
        }

        void release() {
            this.func = null;
            this.funcArgs = null;
        }

        // The former record compared callback objects with Objects.equals and argument
        // arrays by identity. Retain that behavior for the exceptionally rare ID reuse.
        public @Override boolean equals(Object other) {
            return this == other || other instanceof Timeout timeout && this.id == timeout.id &&
                this.expiration == timeout.expiration && Objects.equals(this.func, timeout.func) &&
                Objects.equals(this.funcArgs, timeout.funcArgs);
        }

        public @Override int hashCode() {
            int hash = Integer.hashCode(this.id);
            hash = 31 * hash + Long.hashCode(this.expiration);
            hash = 31 * hash + Objects.hashCode(this.func);
            return 31 * hash + Objects.hashCode(this.funcArgs);
        }

        public boolean isDue(long time) {
            return this.expiration - time <= 0;
        }

        public @Override int compareTo(Timeout o) {
            int time = Long.compare(this.expiration, o.expiration);
            return time != 0 ? time : Integer.compare(this.id, o.id);
        }
    }
}
