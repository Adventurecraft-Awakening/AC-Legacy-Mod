package dev.adventurecraft.awakening.math;

import dev.adventurecraft.awakening.text.NamedEnum;
import org.jetbrains.annotations.NotNull;

public enum Mirror implements NamedEnum {

    NONE("none", OctahedralGroup.IDENTITY),
    LEFT_RIGHT("left_right", OctahedralGroup.INVERT_Z),
    FRONT_BACK("front_back", OctahedralGroup.INVERT_X);

    public static final EnumCodec<Mirror> CODEC = NamedEnum.codec(Mirror.values());

    private final String name;
    private final String symbol;
    private final OctahedralGroup rotation;

    Mirror(String name, OctahedralGroup octahedralGroup) {
        this.name = name;
        this.symbol = "mirror." + name;
        this.rotation = octahedralGroup;
    }

    public int mirror(int i, int j) {
        int k = j / 2;
        int l = i > k ? i - j : i;
        return switch (this) {
            case LEFT_RIGHT -> (k - l + j) % j;
            case FRONT_BACK -> (j - l) % j;
            default -> i;
        };
    }

    public Rotation rotation(Direction direction) {
        Direction.Axis axis = direction.axis();
        if (this == LEFT_RIGHT && axis == Direction.Axis.Z) {
            return Rotation.RIGHT_180;
        }
        if (this == FRONT_BACK && axis == Direction.Axis.X) {
            return Rotation.RIGHT_180;
        }
        return Rotation.NONE;
    }

    public Direction mirror(Direction direction) {
        if (this == FRONT_BACK && direction.axis() == Direction.Axis.X) {
            return direction.opposite();
        }
        if (this == LEFT_RIGHT && direction.axis() == Direction.Axis.Z) {
            return direction.opposite();
        }
        return direction;
    }

    public @Override @NotNull String getName() {
        return this.name;
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public String symbol() {
        return this.symbol;
    }
}

