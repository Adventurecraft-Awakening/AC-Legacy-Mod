package dev.adventurecraft.awakening.natives;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class BindingsTemplate {

    public static final MethodHandle ac_native_math_frustum_contains_cube = NativeLoader.linker.downcallHandle(
        FunctionDescriptor.of(
            ValueLayout.JAVA_BOOLEAN,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE,
            ValueLayout.JAVA_DOUBLE
        ), Linker.Option.critical(true)
    );
}
