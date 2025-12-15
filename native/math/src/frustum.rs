const NUM_PLANES: usize = 6;
const NUM_DIMS: usize = 4;

macro_rules! ac_native_math_frustum_contains_cube {
    ($suffix: ident, $features: literal) => {
        #[target_feature(enable = $features)]
        #[unsafe(no_mangle)]
        pub extern "system" fn ${concat("ac_native_math_frustum_contains_cube", $suffix)} (
            frustum_planes: *const [[f32; NUM_DIMS]; NUM_PLANES],
            x0: f64,
            y0: f64,
            z0: f64,
            x1: f64,
            y1: f64,
            z1: f64,
        ) -> bool {
            contains_cube(
                unsafe { frustum_planes.as_ref() }.unwrap(),
                x0,
                y0,
                z0,
                x1,
                y1,
                z1
            )
        }
    };
}
ac_native_math_frustum_contains_cube!(_sse2, "sse2");
ac_native_math_frustum_contains_cube!(_sse4_1, "sse4.1");
ac_native_math_frustum_contains_cube!(_avx, "avx");
ac_native_math_frustum_contains_cube!(_avx2, "avx2");
ac_native_math_frustum_contains_cube!(_avx512, "avx512f,avx512cd,avx512bw,avx512dq,avx512vl");

#[inline]
fn contains_cube(
    frustum_planes: &[[f32; NUM_DIMS]; NUM_PLANES],
    x0: f64,
    y0: f64,
    z0: f64,
    x1: f64,
    y1: f64,
    z1: f64,
) -> bool {
    for i in 0..6 {
        let mat = frustum_planes[i];
        let m0 = mat[0] as f64;
        let m1 = mat[1] as f64;
        let m2 = mat[2] as f64;
        let m3 = mat[3] as f64;

        let m0x0 = m0 * x0;
        let m0x1 = m0 * x1;
        let m1y0 = m1 * y0;
        let m1y1 = m1 * y1;
        let m2z0 = (m2 * z0) + m3;
        let m2z1 = (m2 * z1) + m3;

        let b0 = m0x0 + m1y0 + m2z0 < 0.0;
        let b1 = m0x1 + m1y0 + m2z0 < 0.0;
        let b2 = m0x0 + m1y1 + m2z0 < 0.0;
        let b3 = m0x1 + m1y1 + m2z0 < 0.0;
        let b4 = m0x0 + m1y0 + m2z1 < 0.0;
        let b5 = m0x1 + m1y0 + m2z1 < 0.0;
        let b6 = m0x0 + m1y1 + m2z1 < 0.0;
        let b7 = m0x1 + m1y1 + m2z1 < 0.0;

        if b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 {
            return false;
        }
    }
    return true;
}
