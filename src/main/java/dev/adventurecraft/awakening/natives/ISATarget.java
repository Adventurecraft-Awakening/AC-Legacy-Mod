package dev.adventurecraft.awakening.natives;

public interface ISATarget {

    int id();

    String suffix();

    static Class<? extends Enum<? extends ISATarget>> getInstance() {
        return switch (NativeLoader.NORMALIZED_ARCH) {
            case "x86_64" -> ISA_x86_64.class;
            case "aarch_64" -> ISA_aarch64.class;
            default -> null;
        };
    }
}
