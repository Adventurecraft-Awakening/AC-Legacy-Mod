package dev.adventurecraft.awakening.script;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.mozilla.javascript.*;

import java.util.Arrays;
import java.util.SortedSet;

public abstract class Timers {

    private int lastId;
    private final Int2ObjectMap<Timeout> timers = new Int2ObjectOpenHashMap<>();
    private final SortedSet<Timeout> timerQueue = new ObjectLinkedOpenHashSet<>();

    protected abstract long getTimeMillis();

    public void install(Scriptable scope) {
        installMethod(scope, "setTimeout", this::setTimeout);
        installMethod(scope, "clearTimeout", this::clearTimeout);
    }

    private void installMethod(Scriptable scope, String name, Callable callable) {
        var func = new LambdaFunction(scope, name, 1, callable);
        ScriptableObject.defineProperty(scope, name, func, ScriptableObject.DONTENUM);
    }

    public void runTimers(Context cx, Scriptable scope) {
        if (this.timerQueue.isEmpty()) {
            return;
        }
        long time = this.getTimeMillis();

        // Check if first timer is due.
        var first = this.timerQueue.getFirst();
        if (!first.isDue(time)) {
            return; // Safe to early-exit, since the queue is sorted.
        }
        this.timerQueue.removeFirst();
        this.execute(cx, scope, first);

        // Don't invoke iterator unless there are more items.
        if (!this.timerQueue.isEmpty()) {
            this.checkTimers(cx, scope, time);
        }
    }

    private void checkTimers(Context cx, Scriptable scope, long time) {
        var iterator = this.timerQueue.iterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (item.isDue(time)) {
                iterator.remove();
                this.execute(cx, scope, item);
            }
        }
    }

    private void execute(Context cx, Scriptable scope, Timeout t) {
        this.timers.remove(t.id);
        cx.processMicrotasks();
        t.func.call(cx, scope, scope, t.funcArgs);
    }

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

        this.timers.put(id, t);
        this.timerQueue.add(t);
        return id;
    }

    private Object clearTimeout(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (args.length == 1) {
            int id = ScriptRuntime.toInt32(args[0]);
            Timeout t = this.timers.remove(id);
            if (t != null) {
                this.timerQueue.remove(t);
            }
        }
        return Undefined.instance;
    }

    private record Timeout(int id, long expiration, Function func, Object[] funcArgs) implements Comparable<Timeout> {
        public boolean isDue(long time) {
            return this.expiration - time <= 0;
        }

        public @Override int compareTo(Timeout o) {
            return Long.compare(this.expiration, o.expiration);
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof Timeout timeout) {
                return this.expiration == timeout.expiration;
            }
            return false;
        }

        public @Override int hashCode() {
            return Long.hashCode(this.expiration);
        }
    }
}
