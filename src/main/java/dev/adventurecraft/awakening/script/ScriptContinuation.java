package dev.adventurecraft.awakening.script;

import org.mozilla.javascript.Scriptable;

class ScriptContinuation {
    
    Object contituation;
    long wakeUp;
    Scriptable scope;

    ScriptContinuation(Object var1, long var2, Scriptable var4) {
        this.contituation = var1;
        this.wakeUp = var2;
        this.scope = var4;
    }
}
