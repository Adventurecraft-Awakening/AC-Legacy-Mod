package dev.adventurecraft.awakening.natives;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public final class Bindings {

    private static MethodHandle bind(MethodHandle template, String prefix) {
        return template.bindTo(NativeLoader.lookup.find(prefix + NativeLoader.currentMachineTarget.suffix()).get());
    }

    private static final MethodHandle handle_ac_native_math_frustum_contains_cube = bind(
        BindingsTemplate.ac_native_math_frustum_contains_cube,
        "ac_native_math_frustum_contains_cube"
    );

    public static boolean ac_native_math_frustum_contains_cube(
        MemorySegment frustumPlanes,
        double x0,
        double y0,
        double z0,
        double x1,
        double y1,
        double z1
    ) {
        try {
            return (boolean) handle_ac_native_math_frustum_contains_cube.invokeExact(
                frustumPlanes,
                x0,
                y0,
                z0,
                x1,
                y1,
                z1
            );
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
