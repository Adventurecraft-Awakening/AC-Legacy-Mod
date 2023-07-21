package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.Stack;

public class AC_Profiler {

    static Stack<ProfileContext> profileStack;
    static Object2LongOpenHashMap<String> times;
    static long startTime;

    public static void startTiming(String var0) {
        if (profileStack != null) {
            profileStack.push(new ProfileContext(var0));
        }
    }

    public static void stopTiming() {
        if (profileStack == null) {
            return;
        }

        ProfileContext context = profileStack.pop();
        long time = context.getTime();
        if (times.containsKey(context.contextName)) {
            times.put(context.contextName, times.getLong(context.contextName) + time);
        } else {
            times.put(context.contextName, time);
        }
    }

    public static void startFrame() {
        profileStack = new Stack<>();
        times = new Object2LongOpenHashMap<>();
        startTime = System.nanoTime();
    }

    public static void stopFrame() {
        if (profileStack == null) {
            return;
        }

        long elapsedTime = System.nanoTime() - startTime;
        if (elapsedTime > 100000000L) {

            for (Object2LongMap.Entry<String> entry : times.object2LongEntrySet()) {
                ACMod.LOGGER.info("{}\t\t{}", entry.getKey(), entry.getLongValue());
            }
        }

        profileStack = null;
        times = null;
    }
}
