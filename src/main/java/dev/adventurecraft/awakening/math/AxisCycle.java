package dev.adventurecraft.awakening.math;

import dev.adventurecraft.awakening.math.Direction.Axis;

@SuppressWarnings("SuspiciousNameCombination")
public enum AxisCycle {
    NONE {
        public @Override int cycle(int x, int y, int z, Axis axis) {
            return axis.choose(x, y, z);
        }

        public @Override double cycle(double x, double y, double z, Axis axis) {
            return axis.choose(x, y, z);
        }

        public @Override Axis cycle(Axis axis) {
            return axis;
        }

        public @Override AxisCycle inverse() {
            return this;
        }
    },
    FORWARD {
        public @Override int cycle(int x, int y, int z, Axis axis) {
            return axis.choose(z, x, y);
        }

        public @Override double cycle(double x, double y, double z, Axis axis) {
            return axis.choose(z, x, y);
        }

        public @Override Axis cycle(Axis axis) {
            return Axis.values()[Math.floorMod(axis.ordinal() + 1, 3)];
        }

        public @Override AxisCycle inverse() {
            return BACKWARD;
        }
    },
    BACKWARD {
        public @Override int cycle(int x, int y, int z, Axis axis) {
            return axis.choose(y, z, x);
        }

        public @Override double cycle(double x, double y, double z, Axis axis) {
            return axis.choose(y, z, x);
        }

        public @Override Axis cycle(Axis axis) {
            return Axis.values()[Math.floorMod(axis.ordinal() - 1, 3)];
        }

        public @Override AxisCycle inverse() {
            return FORWARD;
        }
    };

    public abstract int cycle(int x, int y, int z, Axis axis);

    public abstract double cycle(double x, double y, double z, Axis axis);

    public abstract Axis cycle(Axis axis);

    public abstract AxisCycle inverse();

    public static AxisCycle between(Axis a, Axis b) {
        return values()[Math.floorMod(b.ordinal() - a.ordinal(), 3)];
    }
}
