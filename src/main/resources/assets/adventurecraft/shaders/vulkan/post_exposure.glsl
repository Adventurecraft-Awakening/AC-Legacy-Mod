layout(set = 0, binding = 2, std430) readonly buffer MeteredExposure {
    vec4 exposureState;
};

float postExposure(float manualExposure) {
    return manualExposure*exposureState.x;
}
