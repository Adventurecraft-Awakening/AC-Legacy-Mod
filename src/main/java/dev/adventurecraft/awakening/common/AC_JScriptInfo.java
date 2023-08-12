package dev.adventurecraft.awakening.common;

import org.mozilla.javascript.Script;

import java.util.Objects;

public class AC_JScriptInfo implements Comparable<AC_JScriptInfo> {

    public String name;
    public Script compiledScript;
    public long totalTime;
    public long maxTime;
    public int count;

    public AC_JScriptInfo(String name, Script script) {
        this.name = name.replace(".js", "");
        this.compiledScript = Objects.requireNonNull(script);
    }

    public void addTime(long time) {
        this.totalTime += time;
        if (time > this.maxTime) {
            this.maxTime = time;
        }

        ++this.count;
    }

    public void clear() {
        this.totalTime = 0L;
        this.maxTime = 0L;
        this.count = 0;
    }

    @Override
    public int compareTo(AC_JScriptInfo info) {
        return Long.compare(info.totalTime, this.totalTime);
    }
}
