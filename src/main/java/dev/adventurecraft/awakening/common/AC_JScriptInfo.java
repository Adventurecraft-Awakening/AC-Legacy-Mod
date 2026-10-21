package dev.adventurecraft.awakening.common;

import org.mozilla.javascript.Script;

import java.util.Objects;

public class AC_JScriptInfo {

    public String name;
    public Script compiledScript;
    public long totalTime;
    public long maxTime;
    public int count;

    public AC_JScriptInfo(String name, Script script) {
        this.name = name.replace(".js", "");
        this.compiledScript = Objects.requireNonNull(script);
    }

    /** Scheduled callbacks have profiling data without a separately compiled source file. */
    public AC_JScriptInfo(String name) {
        this.name = name;
    }

    public void addTime(long time) {
        this.addTime(time, true);
    }

    public void addTime(long time, boolean invocation) {
        this.totalTime += time;
        if (time > this.maxTime) {
            this.maxTime = time;
        }

        if (invocation) ++this.count;
    }

    public void clear() {
        this.totalTime = 0L;
        this.maxTime = 0L;
        this.count = 0;
    }
}
