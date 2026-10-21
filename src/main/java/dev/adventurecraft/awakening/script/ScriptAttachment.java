package dev.adventurecraft.awakening.script;

import net.minecraft.nbt.CompoundTag;
import org.mozilla.javascript.Scriptable;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Owns a script scope and its deferred state until the owning world runtime is available. */
public final class ScriptAttachment {
    private final Map<String, Object> bindings = new LinkedHashMap<>();
    private @Nullable WorldScriptRuntime owner;
    private @Nullable Scriptable scope;
    private @Nullable CompoundTag pending;

    public ScriptAttachment bind(String name, Object value) {
        this.bindings.put(name, value);
        if (this.scope != null) this.install(name, value);
        return this;
    }

    public Scriptable attach(Script script) { return this.attach(script.runtime()); }

    public Scriptable attach(WorldScriptRuntime runtime) {
        if (this.owner != runtime || this.scope == null) {
            if (this.scope != null && this.pending == null) this.pending = ScriptState.save(this.scope);
            this.owner = runtime;
            this.scope = runtime.newScope();
            this.bindings.forEach(this::install);
        }
        if (this.pending != null) {
            ScriptState.load(this.scope, this.pending);
            this.pending = null;
        }
        return this.scope;
    }

    public void load(@Nullable CompoundTag tag) {
        this.pending = tag;
        if (this.scope != null && tag != null) {
            ScriptState.load(this.scope, tag);
            this.pending = null;
        }
    }

    /** Saving an unattached tile preserves its pending NBT without requiring a client world. */
    public CompoundTag save() {
        return this.scope != null ? ScriptState.save(this.scope) : this.pending != null ? this.pending : new CompoundTag();
    }

    public void detach() {
        if (this.scope != null) this.pending = ScriptState.save(this.scope);
        this.scope = null;
        this.owner = null;
    }

    private void install(String name, Object value) {
        this.owner.bind(this.scope, name, value);
    }
}
