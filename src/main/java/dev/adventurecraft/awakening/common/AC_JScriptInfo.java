package dev.adventurecraft.awakening.common;

import org.mozilla.javascript.Script;

public class AC_JScriptInfo implements Comparable<AC_JScriptInfo> {

    public String name;
    public Script compiledScript;
    public long totalTime;
    public long maxTime;
    public int count;

    public AC_JScriptInfo(String name, Script script) {
        if (script == null) {
            throw new IllegalArgumentException("script");
        }

        this.name = name.replace(".js", "");
        this.compiledScript = script;
    }

    public void addStat(long var1) {
        this.totalTime += var1;
        if (var1 > this.maxTime) {
            this.maxTime = var1;
        }

        ++this.count;
    }

    @Override
    public int compareTo(AC_JScriptInfo var1) {
        return Long.compare(var1.totalTime, this.totalTime);
    }
}
