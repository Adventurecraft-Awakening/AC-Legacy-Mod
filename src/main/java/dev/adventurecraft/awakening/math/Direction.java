package dev.adventurecraft.awakening.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.text.NamedEnum;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum Direction implements NamedEnum {

    DOWN(0, 1, -1, "down", Polarity.NEGATIVE, Axis.Y, new Coord(0, -1, 0)),
    UP(1, 0, -1, "up", Polarity.POSITIVE, Axis.Y, new Coord(0, 1, 0)),
    NORTH(2, 3, 2, "north", Polarity.NEGATIVE, Axis.Z, new Coord(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Polarity.POSITIVE, Axis.Z, new Coord(0, 0, 1)),
    WEST(4, 5, 1, "west", Polarity.NEGATIVE, Axis.X, new Coord(-1, 0, 0)),
    EAST(5, 4, 3, "east", Polarity.POSITIVE, Axis.X, new Coord(1, 0, 0));

    private static final ImmutableList<Axis> YXZ_AXIS_ORDER = ImmutableList.of(Axis.Y, Axis.X, Axis.Z);
    private static final ImmutableList<Axis> YZX_AXIS_ORDER = ImmutableList.of(Axis.Y, Axis.Z, Axis.X);

    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Axis axis;
    private final Polarity polarity;
    private final Coord normal;

    private static final Direction[] VALUES = values();

    private static final Direction[] BY_3D_DATA = Arrays
        .stream(VALUES)
        .sorted(Comparator.comparingInt(d -> d.data3d))
        .toArray(Direction[]::new);

    private static final Direction[] BY_2D_DATA = Arrays
        .stream(VALUES)
        .filter(d -> d.axis().isHorizontal())
        .sorted(Comparator.comparingInt(d -> d.data2d))
        .toArray(Direction[]::new);

    Direction(int data3d, int oppositeIndex, int data2d, String name, Polarity polarity, Axis axis, Coord normal) {
        this.data3d = data3d;
        this.data2d = data2d;
        this.oppositeIndex = oppositeIndex;
        this.name = name;
        this.axis = axis;
        this.polarity = polarity;
        this.normal = normal;
    }

    public Axis axis() {
        return this.axis;
    }

    public Polarity polarity() {
        return this.polarity;
    }

    public Coord normal() {
        return this.normal;
    }

    public int data3D() {
        return this.data3d;
    }

    public int data2D() {
        return this.data2d;
    }

    public float toYRot() {
        return (this.data2d & 3) * 90;
    }

    public boolean isFacingAngle(float angle) {
        float a = MathF.toRadians(angle);
        float s = MathF.sin(a);
        float c = MathF.cosFromSin(s, a);
        return ((this.normal.x * -s) + (this.normal.z * c)) > 0.0F;
    }

    public void storeRotation(Quaternion q) {
        switch (this) {
            case DOWN -> q.rotationX(MathF.PI);
            case UP -> q.identity();
            case NORTH -> q.rotationXYZ(MathF.PI_OVER_2, 0.0F, MathF.PI);
            case SOUTH -> q.rotationX(MathF.PI_OVER_2);
            case WEST -> q.rotationXYZ(MathF.PI_OVER_2, 0.0F, MathF.PI_OVER_2);
            case EAST -> q.rotationXYZ(MathF.PI_OVER_2, 0.0F, -MathF.PI_OVER_2);
        }
    }

    public Direction opposite() {
        return from3DDataValue(this.oppositeIndex);
    }

    public Direction right(Axis axis) {
        return switch (axis) {
            case X -> (this != WEST && this != EAST) ? this.rightX() : this;
            case Y -> (this != UP && this != DOWN) ? this.rightY() : this;
            case Z -> (this != NORTH && this != SOUTH) ? this.rightZ() : this;
        };
    }

    public Direction rightX() {
        return switch (this) {
            case DOWN -> SOUTH;
            case UP -> NORTH;
            case NORTH -> DOWN;
            case SOUTH -> UP;
            default -> throw new IllegalStateException("No right around X for " + this);
        };
    }

    public Direction rightY() {
        return switch (this) {
            case NORTH -> EAST;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            case EAST -> SOUTH;
            default -> throw new IllegalStateException("No right around Y for " + this);
        };
    }

    public Direction rightZ() {
        return switch (this) {
            case DOWN -> WEST;
            case UP -> EAST;
            case WEST -> UP;
            case EAST -> DOWN;
            default -> throw new IllegalStateException("No right around Z for " + this);
        };
    }

    public Direction left(Axis axis) {
        return switch (axis) {
            case X -> (this != WEST && this != EAST) ? this.leftX() : this;
            case Y -> (this != UP && this != DOWN) ? this.leftY() : this;
            case Z -> (this != NORTH && this != SOUTH) ? this.leftZ() : this;
        };
    }

    public Direction leftY() {
        return switch (this) {
            case NORTH -> WEST;
            case SOUTH -> EAST;
            case WEST -> SOUTH;
            case EAST -> NORTH;
            default -> throw new IllegalStateException("No left around Y for " + this);
        };
    }

    public Direction leftX() {
        return switch (this) {
            case DOWN -> NORTH;
            case UP -> SOUTH;
            case NORTH -> UP;
            case SOUTH -> DOWN;
            default -> throw new IllegalStateException("No left around X for " + this);
        };
    }

    public Direction leftZ() {
        return switch (this) {
            case DOWN -> EAST;
            case UP -> WEST;
            case WEST -> DOWN;
            case EAST -> UP;
            default -> throw new IllegalStateException("No left around Z for " + this);
        };
    }

    public @Override @NotNull String getName() {
        return this.name;
    }

    public @Override String toString() {
        return this.name;
    }

    public static Stream<Direction> stream() {
        return Stream.of(VALUES);
    }

    public static float getYRot(Direction direction) {
        return switch (direction) {
            case NORTH -> 180.0F;
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> throw new IllegalStateException("No y-rot for " + direction);
        };
    }

    public static Direction from3DDataValue(int i) {
        return BY_3D_DATA[Math.abs(i % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int i) {
        return BY_2D_DATA[Math.abs(i % BY_2D_DATA.length)];
    }

    public static Direction fromYRot(double d) {
        return from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndPolarity(Axis axis, Polarity polarity) {
        return switch (axis) {
            case X -> polarity == Polarity.POSITIVE ? EAST : WEST;
            case Y -> polarity == Polarity.POSITIVE ? UP : DOWN;
            case Z -> polarity == Polarity.POSITIVE ? SOUTH : NORTH;
        };
    }

    public static Direction getApproximateNearest(float x, float y, float z) {
        Direction maxDir = NORTH;
        float maxDist = Float.MIN_VALUE;

        for (Direction dir : VALUES) {
            float dist = x * dir.normal.x + y * dir.normal.y + z * dir.normal.z;
            if (dist > maxDist) {
                maxDist = dist;
                maxDir = dir;
            }
        }
        return maxDir;
    }

    public static @Nullable Direction getNearest(int x, int y, int z, @Nullable Direction direction) {
        int aX = Math.abs(x);
        int aY = Math.abs(y);
        int aZ = Math.abs(z);
        if (aX > aZ && aX > aY) {
            return x < 0 ? WEST : EAST;
        }
        else if (aZ > aX && aZ > aY) {
            return z < 0 ? NORTH : SOUTH;
        }
        else if (aY > aX && aY > aZ) {
            return y < 0 ? DOWN : UP;
        }
        return direction;
    }

    public static Direction get(Polarity polarity, Axis axis) {
        for (Direction d : VALUES) {
            if (d.polarity() == polarity && d.axis() == axis) {
                return d;
            }
        }
        throw new IllegalArgumentException("No such direction: " + polarity + " " + axis);
    }

    public static ImmutableList<Axis> axisStepOrder(Vec3 vec) {
        return Math.abs(vec.x) < Math.abs(vec.z) ? YZX_AXIS_ORDER : YXZ_AXIS_ORDER;
    }

    public enum Axis implements NamedEnum, Predicate<Direction> {
        X("x") {
            public @Override int choose(int x, int y, int z) {
                return x;
            }

            public @Override boolean choose(boolean x, boolean y, boolean z) {
                return x;
            }

            public @Override double choose(double x, double y, double z) {
                return x;
            }

            public @Override Direction positive() {
                return Direction.EAST;
            }

            public @Override Direction negative() {
                return Direction.WEST;
            }
        },
        Y("y") {
            public @Override int choose(int x, int y, int z) {
                return y;
            }

            public @Override double choose(double x, double y, double z) {
                return y;
            }

            public @Override boolean choose(boolean x, boolean y, boolean z) {
                return y;
            }

            public @Override Direction positive() {
                return Direction.UP;
            }

            public @Override Direction negative() {
                return Direction.DOWN;
            }
        },
        Z("z") {
            public @Override int choose(int x, int y, int z) {
                return z;
            }

            public @Override double choose(double x, double y, double z) {
                return z;
            }

            public @Override boolean choose(boolean x, boolean y, boolean z) {
                return z;
            }

            public @Override Direction positive() {
                return Direction.SOUTH;
            }

            public @Override Direction negative() {
                return Direction.NORTH;
            }
        };

        private static final Axis[] VALUES = values();

        private final String name;

        Axis(String name) {
            this.name = name;
        }

        public @NotNull String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public abstract Direction positive();

        public abstract Direction negative();

        @Override
        public String toString() {
            return this.name;
        }

        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.axis() == this;
        }

        public Direction.Plane getPlane() {
            return switch (this) {
                case X, Z -> Direction.Plane.HORIZONTAL;
                case Y -> Direction.Plane.VERTICAL;
            };
        }

        public abstract int choose(int x, int y, int z);

        public abstract double choose(double x, double y, double z);

        public abstract boolean choose(boolean x, boolean y, boolean z);

        public static Axis byOrdinal(int ordinal) {
            return VALUES[ordinal];
        }
    }

    public enum Polarity implements NamedEnum {
        POSITIVE(1, "pos"),
        NEGATIVE(-1, "neg");

        private final int step;
        private final String name;

        Polarity(int step, String name) {
            this.step = step;
            this.name = name;
        }

        public @Override @NotNull String getName() {
            return this.name;
        }

        public @Override String toString() {
            return this.name;
        }

        public int step() {
            return this.step;
        }

        public Polarity opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public enum Plane implements NamedEnum, Iterable<Direction>, Predicate<Direction> {
        HORIZONTAL(
            new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST},
            new Axis[]{Axis.X, Axis.Z},
            "xz"
        ),
        VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Axis[]{Axis.Y}, "y");

        private final Direction[] faces;
        private final List<Axis> axis;
        private final String name;

        Plane(Direction[] faces, Axis[] axis, String name) {
            this.faces = faces;
            this.axis = Arrays.asList(axis);
            this.name = name;
        }

        public @Override @NotNull String getName() {
            return this.name;
        }

        public Collection<Axis> axis() {
            return this.axis;
        }

        public int length() {
            return this.faces.length;
        }

        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.axis().getPlane() == this;
        }

        public @NotNull Iterator<Direction> iterator() {
            return Iterators.forArray(this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }
    }
}

