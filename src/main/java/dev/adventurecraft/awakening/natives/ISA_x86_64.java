package dev.adventurecraft.awakening.natives;

public enum ISA_x86_64 implements ISATarget {

    BASE("", 0),
    SSE2("_sse2", 1),
    SSE4_1("_sse4_1", 2),
    AVX("_avx", 3),
    AVX2("_avx2", 4),
    AVX512("_avx512", 5),
    ;

    private final String suffix;
    private final int id;

    ISA_x86_64(String suffix, int id) {
        this.suffix = suffix;
        this.id = id;
    }

    public @Override String suffix() {
        return this.suffix;
    }

    public @Override int id() {
        return this.id;
    }
}
