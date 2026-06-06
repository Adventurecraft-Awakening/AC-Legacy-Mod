package dev.adventurecraft.awakening.math;

import dev.adventurecraft.awakening.text.NamedEnum;
import org.jetbrains.annotations.NotNull;

public enum Rotation implements NamedEnum {

    NONE(0, "none", OctahedralGroup.IDENTITY),
    RIGHT_90(1, "right_90", OctahedralGroup.ROT_90_Y_NEG),
    RIGHT_180(2, "180", OctahedralGroup.ROT_180_FACE_XZ),
    LEFT_90(3, "left_90", OctahedralGroup.ROT_90_Y_POS);

    public static final EnumCodec<Rotation> CODEC = NamedEnum.codec(Rotation.values());

    private final int index;
    private final String id;
    private final OctahedralGroup group;

    Rotation(int index, String id, OctahedralGroup group) {
        this.index = index;
        this.id = id;
        this.group = group;
    }

    public OctahedralGroup group() {
        return this.group;
    }

    public Direction rotate(Direction direction) {
        if (direction.axis() == Direction.Axis.Y) {
            return direction;
        }
        return switch (this) {
            case RIGHT_90 -> direction.rightY();
            case RIGHT_180 -> direction.opposite();
            case LEFT_90 -> direction.leftY();
            default -> direction;
        };
    }

    public int rotate(int angle, int steps) {
        return switch (this) {
            case RIGHT_90 -> (angle + steps / 4) % steps;
            case RIGHT_180 -> (angle + steps / 2) % steps;
            case LEFT_90 -> (angle + steps * 3 / 4) % steps;
            default -> angle;
        };
    }

    public @Override @NotNull String getName() {
        return this.id;
    }

    public int index() {
        return this.index;
    }
}

