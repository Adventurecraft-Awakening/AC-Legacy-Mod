package dev.adventurecraft.awakening.script;

import org.mozilla.javascript.Scriptable;

class ScriptContinuation {
    
    Object contituation;
    long wakeUp;
    Scriptable scope;

    ScriptContinuation(Object continuation, long wakeUp, Scriptable scope) {
        this.contituation = continuation;
        this.wakeUp = wakeUp;
        this.scope = scope;
    }
}
