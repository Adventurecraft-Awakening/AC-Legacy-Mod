
#[repr(i32)]
pub enum ISA_x86 {
    BASE = 0,
    SSE2 = 1,
    SSE4_1 = 2,
    AVX = 3,
    AVX2 = 4,
    AVX512 = 5,
}
impl ISA_x86 {
    pub fn suffix(&self) -> &str {
        match self {
            ISA_x86::BASE => "",
            ISA_x86::SSE2 => "_sse2",
            ISA_x86::SSE4_1 => "_sse4_1",
            ISA_x86::AVX => "_avx",
            ISA_x86::AVX2 => "_avx2",
            ISA_x86::AVX512 => "_avx512",
        }
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn ac_native_math_isa_get_system_target() -> ISA_x86 {
    #[cfg(any(target_arch = "x86", target_arch = "x86_64"))]
    {
        if is_x86_feature_detected!("avx512f")
            && is_x86_feature_detected!("avx512cd")
            && is_x86_feature_detected!("avx512bw")
            && is_x86_feature_detected!("avx512dq")
            && is_x86_feature_detected!("avx512vl")
        {
            return ISA_x86::AVX512;
        }
        if is_x86_feature_detected!("avx2") {
            return ISA_x86::AVX2;
        }
        if is_x86_feature_detected!("avx") {
            return ISA_x86::AVX;
        }
        if is_x86_feature_detected!("sse4.1") {
            return ISA_x86::SSE4_1;
        }
        if is_x86_feature_detected!("sse2") {
            return ISA_x86::SSE2;
        }
        panic!("unsupported x86 ISA");
    }

    #[cfg(target_arch = "aarch64")]
    {
        0
    }
}
