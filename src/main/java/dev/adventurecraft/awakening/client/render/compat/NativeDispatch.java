package dev.adventurecraft.awakening.client.render.compat;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

/** Symbolic method references survive production remapping; class ownership is cached by callers. */
final class NativeDispatch {
    private NativeDispatch() { }
    static String methodName(Serializable reference) {
        try {
            var serialize=reference.getClass().getDeclaredMethod("writeReplace");
            serialize.setAccessible(true);
            return ((SerializedLambda)serialize.invoke(reference)).getImplMethodName();
        } catch(ReflectiveOperationException failure) { throw new IllegalStateException("Cannot inspect native rendering boundary",failure); }
    }
    static boolean ownsOverride(Class<?> type,String legacy,Class<?>[] legacyParameters,String nativeName,Class<?>[] nativeParameters) {
        try {
            Class<?> legacyOwner=type.getMethod(legacy,legacyParameters).getDeclaringClass();
            Class<?> nativeOwner=type.getMethod(nativeName,nativeParameters).getDeclaringClass();
            return !nativeOwner.isInterface() && legacyOwner.isAssignableFrom(nativeOwner);
        } catch(ReflectiveOperationException failure) { throw new IllegalStateException("Invalid native rendering contract for "+type.getName(),failure); }
    }
}
