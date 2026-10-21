package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_JScriptInfo;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.primitives.TickTime;
import dev.adventurecraft.awakening.script.scheduling.ContinuationScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.*;

import java.io.Closeable;
import java.util.*;
import java.util.function.Supplier;

/** One world's Rhino context, API bindings, script catalog, execution and scheduled work. */
public final class WorldScriptRuntime implements Closeable {
    public static final String SCRIPT_PACKAGE = "dev.adventurecraft.awakening.script";
    private static final Set<String> ALLOWED_CLASSES = Set.of("java.lang.Object", "java.lang.Character",
        "java.lang.String", "java.lang.Float", "java.lang.Double", "java.lang.Boolean", "java.lang.Byte",
        "java.lang.Short", "java.lang.Integer", "java.lang.Long", "org.mozilla.javascript.ConsString");
    private static final Object SCOPE_OWNER = new Object();

    public static class CustomContextFactory extends ContextFactory {
        private Context createDetachedContext() {
            Context context = this.makeContext();
            this.onContextCreated(context);
            if (this.isSealed() && !context.isSealed()) context.seal(null);
            return context;
        }

        @Override protected Context makeContext() {
            Context context = super.makeContext();
            context.setLanguageVersion(Context.VERSION_ECMASCRIPT);
            context.setInterpretedMode(true);
            context.setClassShutter(WorldScriptRuntime::isClassVisible);
            return context;
        }
    }

    private static boolean isClassVisible(String name) {
        // Rhino also checks array classes, using JVM names such as [L...ScriptEntity;.
        // Apply the same policy to their element type so legacy API array returns work.
        int dimensions = 0;
        while (dimensions < name.length() && name.charAt(dimensions) == '[') dimensions++;
        if (dimensions > 0) {
            if (dimensions == name.length()) return false;
            if (name.length() == dimensions + 1) {
                return "ZBCSIJFD".indexOf(name.charAt(dimensions)) >= 0;
            }
            if (name.charAt(dimensions) != 'L' || !name.endsWith(";")) return false;
            name = name.substring(dimensions + 1, name.length() - 1);
        }
        return name.startsWith(SCRIPT_PACKAGE + ".") || ALLOWED_CLASSES.contains(name);
    }

    private final Minecraft client;
    private final Context context;
    private final Scriptable globalScope;
    private final Scriptable commandScope;
    private final ScriptBindings bindings;
    private final ScriptCatalog catalog;
    private final Timers timers;
    private final ContinuationScheduler<ScheduledContinuation> continuations =
        new ContinuationScheduler<>(WorldScriptRuntime::isContinuationDue, this::resumeContinuation);
    private final Map<org.mozilla.javascript.Script, String> sourceNames = new WeakHashMap<>();
    private final Map<String, AC_JScriptInfo> statistics = new LinkedHashMap<>();
    private Scriptable currentScope;
    private boolean closed;

    public WorldScriptRuntime(Level world, Minecraft client) {
        this.client = Objects.requireNonNull(client);
        ContextFactory factory = ContextFactory.getGlobal();
        this.context = (factory instanceof CustomContextFactory custom ? custom : new CustomContextFactory()).createDetachedContext();
        try (Context ignored = this.enterContext()) {
            boolean allowJava = ((ExGameOptions) client.options).getAllowJavaInScript();
            this.globalScope = allowJava ? this.context.initStandardObjects(null, false) :
                this.context.initSafeStandardObjects(null, false);
            ((ScriptableObject) this.globalScope).associateValue(SCOPE_OWNER, this);
            this.currentScope = this.globalScope;
            this.commandScope = this.newScope();
            this.catalog = new ScriptCatalog(this);
            this.bindings = new ScriptBindings(this, world, client);
            this.timers = new Timers() {
                @Override protected long getTimeMillis() { return bindings.time.getTickCount() * TickTime.MILLIS_PER_TICK; }
                @Override protected void invokeCallback(Context cx, Scriptable scope, Function callback, Object[] arguments) {
                    String name = callback instanceof BaseFunction function ? function.getFunctionName() : "";
                    AC_JScriptInfo profile = statistics.computeIfAbsent("<timer:" + name + ">", AC_JScriptInfo::new);
                    invoke(scope, profile, true, () -> {
                        cx.processMicrotasks();
                        return callback instanceof JSFunction ? cx.callFunctionWithContinuations(callback, scope, arguments) :
                            callback.call(cx, scope, scope, arguments);
                    });
                }
            };
            this.timers.install(this.globalScope);
            if (allowJava) {
                this.runString("net = { minecraft: { script: Packages." + SCRIPT_PACKAGE + " } };", "<init>", this.globalScope);
            }
            this.bindings.installClasses();
        }
    }

    public Context context() { this.ensureOpen(); return this.context; }
    public Scriptable globalScope() { return this.globalScope; }
    public ScriptKeyboard keyboard() { return this.bindings.keyboard; }
    public ScriptCatalog catalog() { return this.catalog; }
    public ScriptAttachment attachment() { return new ScriptAttachment(); }
    public Collection<AC_JScriptInfo> statistics() { return this.statistics.values().stream().sorted(Comparator.comparing(info -> info.name)).toList(); }
    public ContinuationScheduler.Statistics continuationStatistics() { return this.continuations.statistics(); }
    public Timers.Statistics timerStatistics() { return this.timers.statistics(); }
    public void initPlayer(LocalPlayer player) { this.bindings.player(player); }

    /** Enters this world's context for one operation, including nested API calls from its scripts. */
    public <T> T withContext(Supplier<T> operation) {
        this.ensureOpen();
        try (Context ignored = this.enterContext()) {
            return operation.get();
        }
    }

    private Context enterContext() {
        Context active = Context.getCurrentContext();
        if (active != null && active != this.context) {
            throw new IllegalStateException("Cannot enter a world script runtime from another Rhino context");
        }
        return this.context.getFactory().enterContext(this.context);
    }

    static WorldScriptRuntime ownerOf(Scriptable scope) {
        if (scope == null) return null;
        Scriptable top = ScriptableObject.getTopLevelScope(scope);
        return top instanceof ScriptableObject object && object.getAssociatedValue(SCOPE_OWNER) instanceof WorldScriptRuntime owner ? owner : null;
    }

    public Scriptable newScope() {
        return this.withContext(() -> {
            Scriptable scope = this.context.newObject(this.globalScope);
            scope.setParentScope(this.globalScope);
            return scope;
        });
    }

    public void bind(String name, Object value) { this.bind(this.globalScope, name, value); }

    public void bind(Scriptable scope, String name, Object value) {
        this.withContext(() -> {
            ScriptableObject.putProperty(scope, name, Context.javaToJS(value, scope));
            return null;
        });
    }

    public org.mozilla.javascript.Script compileString(String source, String name) {
        return this.withContext(() -> {
            try {
                var compiled = this.context.compileString(source, name, 1, null);
                this.sourceNames.put(compiled, name);
                return compiled;
            } catch (Exception error) {
                this.message("(JS) Compile: " + error.getMessage());
                return null;
            }
        });
    }

    public String runString(String source, String name) { return this.runString(source, name, this.commandScope); }

    private String runString(String source, String name, Scriptable scope) {
        var compiled = this.compileString(source, name);
        if (compiled == null) return null;
        Object result = this.runScript(compiled, scope);
        if (result == null) return null;
        return this.withContext(() -> {
            try { return Context.toString(result); }
            catch (RhinoException error) { this.report(error); return null; }
        });
    }

    public Object runScript(org.mozilla.javascript.Script script, Scriptable scope) {
        this.ensureOpen();
        String name = this.sourceNames.getOrDefault(script, "<script>");
        AC_JScriptInfo profile = this.statistics.computeIfAbsent(key(name), ignored -> new AC_JScriptInfo(name, script));
        return this.invoke(scope, profile, true, () -> this.context.executeScriptWithContinuations(script, this.currentScope));
    }

    /** Catalog entries and ad-hoc scripts use the same execution/profiling boundary. */
    Object runScript(AC_JScriptInfo script, Scriptable scope) {
        return this.invoke(scope, script, true, () -> this.context.executeScriptWithContinuations(script.compiledScript, this.currentScope));
    }

    AC_JScriptInfo register(String name, org.mozilla.javascript.Script compiled) {
        var profile = new AC_JScriptInfo(name, compiled);
        this.sourceNames.put(compiled, name);
        this.statistics.put(key(name), profile);
        return profile;
    }

    void forget(String name, AC_JScriptInfo profile) { this.statistics.remove(key(name), profile); }
    static String key(String name) { return name.toLowerCase(Locale.ROOT); }

    private Object invoke(Scriptable scope, AC_JScriptInfo profile, boolean count, Supplier<Object> execution) {
        return this.withContext(() -> this.invokeEntered(scope, profile, count, execution));
    }

    private Object invokeEntered(Scriptable scope, AC_JScriptInfo profile, boolean count, Supplier<Object> execution) {
        Scriptable previous = this.currentScope;
        long start = System.nanoTime();
        try {
            if (scope != null) this.currentScope = scope;
            return execution.get();
        } catch (ContinuationPending continuation) {
            var scheduled = new ScheduledContinuation(continuation, profile);
            // sleep() is the sole internal capture site and installs an immutable deadline.
            // Unknown states retain the ordered live-state scan instead of being discarded.
            if (continuation.getApplicationState() instanceof ScriptContinuation(long wakeUp, Scriptable ignored)) {
                this.continuations.schedule(scheduled, wakeUp);
            } else {
                this.continuations.scheduleUnknown(scheduled);
            }
        } catch (RhinoException error) {
            this.report(error);
        } finally {
            profile.addTime(System.nanoTime() - start, count);
            this.currentScope = previous;
        }
        return null;
    }

    public void tick() {
        this.withContext(() -> {
            this.tickEntered();
            return null;
        });
    }

    private void tickEntered() {
        this.ensureOpen();
        this.continuations.tick(this.bindings.time.getTickCount());
        this.timers.runTimers(this.context, this.globalScope);
    }

    private static boolean isContinuationDue(ScheduledContinuation scheduled, long tick) {
        return scheduled.pending.getApplicationState() instanceof ScriptContinuation(long wakeUp, Scriptable ignored) && wakeUp <= tick;
    }

    private void resumeContinuation(ScheduledContinuation scheduled) {
        var state = (ScriptContinuation) scheduled.pending.getApplicationState();
        this.invoke(state.scope(), scheduled.profile, false,
            () -> this.context.resumeContinuation(scheduled.pending.getContinuation(), this.currentScope, null));
    }

    public void sleep(float seconds) {
        int ticks = (int) (20.0F * seconds);
        if (ticks <= 0) return;
        ContinuationPending continuation = this.context.captureContinuation();
        continuation.setApplicationState(new ScriptContinuation(this.bindings.time.getTickCount() + ticks, this.currentScope));
        throw continuation;
    }

    void missing(String name) { this.message(String.format("(JS) Missing '%s'", name)); }
    void loadFailure(String name, Exception error) {
        this.message("JS: " + error.getMessage());
        ACMod.JS_LOGGER.error("Failed to load script file \"{}\".", name, error);
    }
    private void message(String message) { this.client.gui.addMessage(message); }
    private void report(RhinoException error) {
        this.message("JS: " + error.getMessage());
        Exception trace = ACMod.JS_LOGGER.isTraceEnabled() ? error : null;
        ACMod.JS_LOGGER.warn("{}\n{}", error.getMessage(), error.getScriptStackTrace(), trace);
    }
    private void ensureOpen() { if (this.closed) throw new IllegalStateException("World script runtime is closed"); }

    @Override public void close() {
        if (this.closed) return;
        this.closed = true;
        this.continuations.clear();
        this.timers.clear();
        this.catalog.clear();
        this.sourceNames.clear();
        this.statistics.clear();
    }

    private record ScheduledContinuation(ContinuationPending pending, AC_JScriptInfo profile) { }
}
