package dev.adventurecraft.awakening.natives;

public enum ISA_aarch64 implements ISATarget {

    GENERIC("_generic", 0),
    ;

    private final String suffix;
    private final int id;

    ISA_aarch64(String suffix, int id) {
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
