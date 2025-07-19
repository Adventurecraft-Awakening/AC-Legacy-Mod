package dev.adventurecraft.awakening.script;

import org.mozilla.javascript.Scriptable;

public record ScriptContinuation(long wakeUp, Scriptable scope) {
}
