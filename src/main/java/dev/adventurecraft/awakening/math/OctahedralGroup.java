package dev.adventurecraft.awakening.math;

import dev.adventurecraft.awakening.math.Direction.Axis;
import dev.adventurecraft.awakening.math.Direction.Polarity;
import dev.adventurecraft.awakening.text.NamedEnum;
import dev.adventurecraft.awakening.util.ObjectUtil;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.adventurecraft.awakening.math.BVec3.*;

public enum OctahedralGroup implements NamedEnum {

    IDENTITY("identity", SymmetricGroup3.P123, X0_Y0_Z0),
    ROT_180_FACE_XY("rot_180_face_xy", SymmetricGroup3.P123, X1_Y1_Z0),
    ROT_180_FACE_XZ("rot_180_face_xz", SymmetricGroup3.P123, X1_Y0_Z1),
    ROT_180_FACE_YZ("rot_180_face_yz", SymmetricGroup3.P123, X0_Y1_Z1),
    ROT_120_NNN("rot_120_nnn", SymmetricGroup3.P231, X0_Y0_Z0),
    ROT_120_NNP("rot_120_nnp", SymmetricGroup3.P312, X1_Y0_Z1),
    ROT_120_NPN("rot_120_npn", SymmetricGroup3.P312, X0_Y1_Z1),
    ROT_120_NPP("rot_120_npp", SymmetricGroup3.P231, X1_Y0_Z1),
    ROT_120_PNN("rot_120_pnn", SymmetricGroup3.P312, X1_Y1_Z0),
    ROT_120_PNP("rot_120_pnp", SymmetricGroup3.P231, X1_Y1_Z0),
    ROT_120_PPN("rot_120_ppn", SymmetricGroup3.P231, X0_Y1_Z1),
    ROT_120_PPP("rot_120_ppp", SymmetricGroup3.P312, X0_Y0_Z0),
    ROT_180_EDGE_XY_NEG("rot_180_edge_xy_neg", SymmetricGroup3.P213, X1_Y1_Z1),
    ROT_180_EDGE_XY_POS("rot_180_edge_xy_pos", SymmetricGroup3.P213, X0_Y0_Z1),
    ROT_180_EDGE_XZ_NEG("rot_180_edge_xz_neg", SymmetricGroup3.P321, X1_Y1_Z1),
    ROT_180_EDGE_XZ_POS("rot_180_edge_xz_pos", SymmetricGroup3.P321, X0_Y1_Z0),
    ROT_180_EDGE_YZ_NEG("rot_180_edge_yz_neg", SymmetricGroup3.P132, X1_Y1_Z1),
    ROT_180_EDGE_YZ_POS("rot_180_edge_yz_pos", SymmetricGroup3.P132, X1_Y0_Z0),
    ROT_90_X_NEG("rot_90_x_neg", SymmetricGroup3.P132, X0_Y0_Z1),
    ROT_90_X_POS("rot_90_x_pos", SymmetricGroup3.P132, X0_Y1_Z0),
    ROT_90_Y_NEG("rot_90_y_neg", SymmetricGroup3.P321, X1_Y0_Z0),
    ROT_90_Y_POS("rot_90_y_pos", SymmetricGroup3.P321, X0_Y0_Z1),
    ROT_90_Z_NEG("rot_90_z_neg", SymmetricGroup3.P213, X0_Y1_Z0),
    ROT_90_Z_POS("rot_90_z_pos", SymmetricGroup3.P213, X1_Y0_Z0),
    INVERSION("inversion", SymmetricGroup3.P123, X1_Y1_Z1),
    INVERT_X("invert_x", SymmetricGroup3.P123, X1_Y0_Z0),
    INVERT_Y("invert_y", SymmetricGroup3.P123, X0_Y1_Z0),
    INVERT_Z("invert_z", SymmetricGroup3.P123, X0_Y0_Z1),
    ROT_60_REF_NNN("rot_60_ref_nnn", SymmetricGroup3.P312, X1_Y1_Z1),
    ROT_60_REF_NNP("rot_60_ref_nnp", SymmetricGroup3.P231, X1_Y0_Z0),
    ROT_60_REF_NPN("rot_60_ref_npn", SymmetricGroup3.P231, X0_Y0_Z1),
    ROT_60_REF_NPP("rot_60_ref_npp", SymmetricGroup3.P312, X0_Y0_Z1),
    ROT_60_REF_PNN("rot_60_ref_pnn", SymmetricGroup3.P231, X0_Y1_Z0),
    ROT_60_REF_PNP("rot_60_ref_pnp", SymmetricGroup3.P312, X1_Y0_Z0),
    ROT_60_REF_PPN("rot_60_ref_ppn", SymmetricGroup3.P312, X0_Y1_Z0),
    ROT_60_REF_PPP("rot_60_ref_ppp", SymmetricGroup3.P231, X1_Y1_Z1),
    SWAP_XY("swap_xy", SymmetricGroup3.P213, X0_Y0_Z0),
    SWAP_YZ("swap_yz", SymmetricGroup3.P132, X0_Y0_Z0),
    SWAP_XZ("swap_xz", SymmetricGroup3.P321, X0_Y0_Z0),
    SWAP_NEG_XY("swap_neg_xy", SymmetricGroup3.P213, X1_Y1_Z0),
    SWAP_NEG_YZ("swap_neg_yz", SymmetricGroup3.P132, X0_Y1_Z1),
    SWAP_NEG_XZ("swap_neg_xz", SymmetricGroup3.P321, X1_Y0_Z1),
    ROT_90_REF_X_NEG("rot_90_ref_x_neg", SymmetricGroup3.P132, X1_Y0_Z1),
    ROT_90_REF_X_POS("rot_90_ref_x_pos", SymmetricGroup3.P132, X1_Y1_Z0),
    ROT_90_REF_Y_NEG("rot_90_ref_y_neg", SymmetricGroup3.P321, X1_Y1_Z0),
    ROT_90_REF_Y_POS("rot_90_ref_y_pos", SymmetricGroup3.P321, X0_Y1_Z1),
    ROT_90_REF_Z_NEG("rot_90_ref_z_neg", SymmetricGroup3.P213, X0_Y1_Z1),
    ROT_90_REF_Z_POS("rot_90_ref_z_pos", SymmetricGroup3.P213, X1_Y0_Z1);

    private static final OctahedralGroup[] VALUES = values();

    private final String name;
    private final BVec3 invert;
    private final SymmetricGroup3 permutation;
    private final Map<Direction, Direction> rotatedDirections;

    private static final OctahedralGroup[][] CAYLEY_TABLE = ObjectUtil.make(
        new OctahedralGroup[VALUES.length][VALUES.length], lookup -> {
            Map<Pair<SymmetricGroup3, BVec3>, OctahedralGroup> map = Arrays
                .stream(VALUES)
                .collect(Collectors.toMap(og -> Pair.of(og.permutation, og.inversions()), og -> og));

            for (OctahedralGroup og1 : VALUES) {
                for (OctahedralGroup og2 : VALUES) {
                    BVec3 i1 = og1.inversions();
                    BVec3 i2 = og2.inversions();
                    SymmetricGroup3 sg = og2.permutation.compose(og1.permutation);
                    BVec3 i3 = BVec3.ZERO;
                    for (int i = 0; i < 3; i++) {
                        i3 = i3.with(i, i1.get(i) ^ i2.get(og1.permutation.permutation(i)));
                    }
                    lookup[og1.ordinal()][og2.ordinal()] = map.get(Pair.of(sg, i3));
                }
            }
        }
    );

    private static final OctahedralGroup[] INVERSE_TABLE = Arrays
        .stream(VALUES)
        .map(og1 -> Arrays.stream(VALUES).filter(og2 -> og1.compose(og2) == IDENTITY).findAny().orElseThrow())
        .toArray(OctahedralGroup[]::new);

    private static final OctahedralGroup[][] XY_TABLE = ObjectUtil.make(
        new OctahedralGroup[Quadrant.values().length][Quadrant.values().length], groups -> {
            for (Quadrant q1 : Quadrant.values()) {
                for (Quadrant q2 : Quadrant.values()) {
                    OctahedralGroup q = IDENTITY;
                    for (int i = 0; i < q2.shift; i++) {
                        q = q.compose(ROT_90_Y_NEG);
                    }
                    for (int i = 0; i < q1.shift; i++) {
                        q = q.compose(ROT_90_X_NEG);
                    }
                    groups[q1.ordinal()][q2.ordinal()] = q;
                }
            }
        }
    );

    OctahedralGroup(String name, SymmetricGroup3 permutation, BVec3 invert) {
        this.name = name;
        this.invert = invert;
        this.permutation = permutation;

        this.rotatedDirections = ObjectUtil.makeEnumMap(
            Direction.class, dir -> {
                Axis a = this.permute(dir.axis());
                Polarity ad = dir.polarity();
                Polarity ad2 = this.inverts(a) ? ad.opposite() : ad;
                return Direction.fromAxisAndPolarity(a, ad2);
            }
        );
    }

    private BVec3 inversions() {
        return this.invert;
    }

    public OctahedralGroup compose(OctahedralGroup other) {
        return CAYLEY_TABLE[this.ordinal()][other.ordinal()];
    }

    public OctahedralGroup inverse() {
        return INVERSE_TABLE[this.ordinal()];
    }

    public @Override String toString() {
        return this.name;
    }

    public @Override @NotNull String getName() {
        return this.name;
    }

    public Direction rotate(Direction direction) {
        return this.rotatedDirections.get(direction);
    }

    public boolean inverts(Axis axis) {
        return this.invert.get(axis.ordinal());
    }

    public Axis permute(Axis axis) {
        return Axis.byOrdinal(this.permutation.permutation(axis.ordinal()));
    }

    public FrontAndTop rotate(FrontAndTop orientation) {
        return FrontAndTop.fromFrontAndTop(this.rotate(orientation.front()), this.rotate(orientation.top()));
    }

    public static OctahedralGroup fromXYAngles(Quadrant x, Quadrant y) {
        return XY_TABLE[x.ordinal()][y.ordinal()];
    }
}
