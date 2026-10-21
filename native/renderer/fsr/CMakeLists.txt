find_package(Vulkan REQUIRED)
find_package(Python3 REQUIRED COMPONENTS Interpreter)
find_program(AC_GLSLANG glslangValidator REQUIRED)
set(AC_FSR_SDK_ROOT "${AC_NATIVE_SDK_ROOT}/fidelityfx" CACHE PATH "External FidelityFX SDK 1.1.4 root")
set(AC_FSR_SDK "${AC_FSR_SDK_ROOT}/sdk")
if(NOT EXISTS "${AC_FSR_SDK}/include/FidelityFX/host/ffx_fsr3upscaler.h")
    message(FATAL_ERROR "FSR requires the installed FidelityFX SDK 1.1.4")
endif()
set(AC_FSR_BLOBS "${CMAKE_CURRENT_BINARY_DIR}/FsrShaderBlobs.cpp")
# Linux wchar_t is 32-bit. Preserve its ABI rather than using -fshort-wchar,
# which is incompatible with libc; enlarge only the SDK's opaque host context.
# This SDK-derived header is generated outside the source tree with its notice.
set(AC_FSR_OVERLAY "${CMAKE_CURRENT_BINARY_DIR}/include")
file(READ "${AC_FSR_SDK}/include/FidelityFX/host/ffx_fsr3upscaler.h" AC_FSR_CONTEXT_HEADER)
if(NOT WIN32)
    string(REPLACE "#define FFX_FSR3UPSCALER_CONTEXT_SIZE (FFX_SDK_DEFAULT_CONTEXT_SIZE)"
        "#define FFX_FSR3UPSCALER_CONTEXT_SIZE (FFX_SDK_DEFAULT_CONTEXT_SIZE * 2)"
        AC_FSR_CONTEXT_HEADER "${AC_FSR_CONTEXT_HEADER}")
endif()
file(MAKE_DIRECTORY "${AC_FSR_OVERLAY}/FidelityFX/host")
file(WRITE "${AC_FSR_OVERLAY}/FidelityFX/host/ffx_fsr3upscaler.h" "${AC_FSR_CONTEXT_HEADER}")
foreach(AC_FSR_EFFECT IN ITEMS frameinterpolation opticalflow)
    file(READ "${AC_FSR_SDK}/include/FidelityFX/host/ffx_${AC_FSR_EFFECT}.h" AC_FSR_EFFECT_HEADER)
    if(NOT WIN32)
        string(REPLACE "(FFX_SDK_DEFAULT_CONTEXT_SIZE)" "(FFX_SDK_DEFAULT_CONTEXT_SIZE * 2)"
            AC_FSR_EFFECT_HEADER "${AC_FSR_EFFECT_HEADER}")
    endif()
    file(WRITE "${AC_FSR_OVERLAY}/FidelityFX/host/ffx_${AC_FSR_EFFECT}.h" "${AC_FSR_EFFECT_HEADER}")
endforeach()
# The SDK's EffectContext is alignas(32), but its scratch-array layout pads to
# four bytes. Respect that alignment in the build-only copy and in our allocator;
# optimized Linux builds otherwise emit aligned stores to misaligned addresses.
set(AC_FSR_BACKEND "${CMAKE_CURRENT_BINARY_DIR}/FsrVulkanBackend.cpp")
file(READ "${AC_FSR_SDK}/src/backends/vk/ffx_vk.cpp" AC_FSR_BACKEND_SOURCE)
string(REPLACE ", sizeof(uint32_t));" ", 32u);" AC_FSR_BACKEND_SOURCE "${AC_FSR_BACKEND_SOURCE}")
string(REPLACE "sizeof(BackendContext_VK) + extensionPropArraySize"
    "FFX_ALIGN_UP(sizeof(BackendContext_VK), 32u) + extensionPropArraySize"
    AC_FSR_BACKEND_SOURCE "${AC_FSR_BACKEND_SOURCE}")
string(REPLACE "(uint8_t*)((BackendContext_VK*)(backendContext + 1))"
    "(uint8_t*)backendContext + FFX_ALIGN_UP(sizeof(BackendContext_VK), 32u)"
    AC_FSR_BACKEND_SOURCE "${AC_FSR_BACKEND_SOURCE}")
# The upstream array has six types; its hard-coded count omits storage buffers
# used by interpolation's counters and can exhaust pools on stricter drivers.
string(REPLACE "descriptorPoolCreateInfo.poolSizeCount = 5;"
    "descriptorPoolCreateInfo.poolSizeCount = static_cast<uint32_t>(sizeof(poolSizes) / sizeof(poolSizes[0]));"
    AC_FSR_BACKEND_SOURCE "${AC_FSR_BACKEND_SOURCE}")
file(WRITE "${AC_FSR_BACKEND}" "${AC_FSR_BACKEND_SOURCE}")
set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS
    "${AC_FSR_SDK}/src/backends/vk/ffx_vk.cpp"
    "${AC_FSR_SDK}/include/FidelityFX/host/ffx_fsr3upscaler.h"
    "${AC_FSR_SDK}/include/FidelityFX/host/ffx_frameinterpolation.h"
    "${AC_FSR_SDK}/include/FidelityFX/host/ffx_opticalflow.h")
file(GLOB_RECURSE AC_FSR_GPU_INPUTS CONFIGURE_DEPENDS "${AC_FSR_SDK}/include/FidelityFX/gpu/*.h"
    "${AC_FSR_SDK}/src/backends/vk/shaders/fsr3upscaler/*.glsl"
    "${AC_FSR_SDK}/src/backends/vk/shaders/frameinterpolation/*.glsl"
    "${AC_FSR_SDK}/src/backends/vk/shaders/opticalflow/*.glsl")
add_custom_command(OUTPUT "${AC_FSR_BLOBS}"
    COMMAND "${Python3_EXECUTABLE}" "${CMAKE_CURRENT_SOURCE_DIR}/compile_shaders.py"
        --sdk "${AC_FSR_SDK}" --compiler "${AC_GLSLANG}" --output "${AC_FSR_BLOBS}"
    DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/compile_shaders.py" ${AC_FSR_GPU_INPUTS}
    VERBATIM)
add_library(ac_fsr STATIC FsrBackend.cpp FsrFrameGeneration.cpp "${AC_FSR_BLOBS}"
    "${AC_FSR_BACKEND}"
    "${AC_FSR_SDK}/src/components/fsr3upscaler/ffx_fsr3upscaler.cpp"
    "${AC_FSR_SDK}/src/components/frameinterpolation/ffx_frameinterpolation.cpp"
    "${AC_FSR_SDK}/src/components/opticalflow/ffx_opticalflow.cpp"
    "${AC_FSR_SDK}/src/shared/ffx_assert.cpp"
    "${AC_FSR_SDK}/src/shared/ffx_message.cpp"
    "${AC_FSR_SDK}/src/shared/ffx_object_management.cpp"
    "${AC_FSR_SDK}/src/shared/ffx_breadcrumbs_list.cpp")
set_target_properties(ac_fsr PROPERTIES POSITION_INDEPENDENT_CODE ON CXX_STANDARD 17 CXX_STANDARD_REQUIRED ON CXX_EXTENSIONS OFF)
target_compile_features(ac_fsr PUBLIC cxx_std_17)
target_include_directories(ac_fsr PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}" PRIVATE "${AC_FSR_OVERLAY}" "${AC_FSR_SDK}/include"
    "${AC_FSR_SDK}/src/shared" "${AC_FSR_SDK}/src/backends/shared"
    "${AC_FSR_SDK}/src/components")
target_compile_definitions(ac_fsr PRIVATE FFX_GCC)
if(NOT WIN32)
    target_compile_options(ac_fsr PRIVATE -include "${CMAKE_CURRENT_SOURCE_DIR}/LinuxCompat.hpp")
endif()
target_link_libraries(ac_fsr PUBLIC Vulkan::Vulkan)
