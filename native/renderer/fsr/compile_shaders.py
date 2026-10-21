#!/usr/bin/env python3
"""Compile external MIT FidelityFX GLSL and reflect its SPIR-V resource ABI.

Only this integration's low-resolution, unjittered-motion, ordinary-depth
configuration is built, including standalone optical flow and interpolation.
All arithmetic uses the official FP32 shader path.
Generated SDK-derived binaries/metadata stay in the native build directory.
"""
import argparse
import concurrent.futures
import json
import os
from pathlib import Path
import struct
import subprocess
import tempfile

PASSES = [
    "prepare_inputs", "luma_pyramid", "shading_change_pyramid", "shading_change",
    "prepare_reactivity", "luma_instability", "accumulate", "accumulate",
    "rcas", "debug_view", "autogen_reactive",
]
INTERPOLATION = [
    "reconstruct_and_dilate", "setup", "reconstruct_previous_depth",
    "game_motion_vector_field", "optical_flow_vector_field", "disocclusion_mask",
    "", "compute_inpainting_pyramid", "inpainting",
    "compute_game_vector_field_inpainting_pyramid", "debug_view",
]
OPTICAL_FLOW = [
    "prepare_luma", "compute_luminance_pyramid", "generate_scd_histogram",
    "compute_scd_divergence", "compute_optical_flow_advanced_pass_v5",
    "filter_optical_flow_pass_v5", "scale_optical_flow_advanced_pass_v5",
]
KINDS = ("cbv", "srvTexture", "uavTexture", "srvBuffer", "uavBuffer", "sampler", "rtAccelStruct")


def string(words):
    return struct.pack("<" + "I" * len(words), *words).split(b"\0", 1)[0].decode("utf-8")


def reflect(data):
    words = struct.unpack("<" + "I" * (len(data) // 4), data)
    if words[0] != 0x07230203:
        raise ValueError("Invalid SPIR-V header")
    names, decorations, types, constants, variables, capabilities = {}, {}, {}, {}, [], set()
    offset = 5
    while offset < len(words):
        length, opcode = words[offset] >> 16, words[offset] & 65535
        if not length or offset + length > len(words):
            raise ValueError("Malformed SPIR-V instruction")
        args = words[offset + 1:offset + length]
        if opcode == 5:
            names[args[0]] = string(args[1:])
        elif opcode == 17:
            capabilities.add(args[0])
        elif opcode == 71:
            decorations.setdefault(args[0], {})[args[1]] = args[2:]
        elif 19 <= opcode <= 33:
            types[args[0]] = (opcode, args[1:])
        elif opcode == 43:
            constants[args[1]] = args[2]
        elif opcode == 59:
            variables.append(args[:3])
        offset += length
    resources = {kind: [] for kind in KINDS}
    for pointer, identifier, storage in variables:
        decorated = decorations.get(identifier, {})
        if 33 not in decorated:
            continue
        _, pointer_type = types[pointer]
        resource_type = pointer_type[1]
        count = 1
        while types[resource_type][0] == 28:
            _, array = types[resource_type]
            count *= constants[array[1]]
            resource_type = array[0]
        opcode, description = types[resource_type]
        if storage == 2 and opcode == 30:
            kind = "cbv"
        elif storage == 0 and opcode == 26:
            kind = "sampler"
        elif storage == 0 and opcode == 25:
            kind = "srvTexture" if description[5] == 1 else "uavTexture"
        elif storage == 12 and opcode == 30:
            kind = "uavBuffer" if names.get(identifier, "").startswith("rw_") else "srvBuffer"
        else:
            raise ValueError(f"Unsupported FSR descriptor {names.get(identifier)}: storage={storage}, type={opcode}")
        resources[kind].append((names[identifier], decorated[33][0], count, decorated.get(34, (0,))[0]))
    for entries in resources.values():
        entries.sort(key=lambda entry: entry[1])
    return resources, capabilities


def compile_one(arguments, temporary, pass_id, key):
    hdr, lut = (key >> 1) & 1, key & 1
    definitions = {
        "FFX_GLSL": 1, "FFX_GPU": 1, "FFX_HALF": 0,
        "FFX_FSR3UPSCALER_OPTION_REPROJECT_USE_LANCZOS_TYPE": lut,
        "FFX_FSR3UPSCALER_OPTION_HDR_COLOR_INPUT": hdr,
        "FFX_FSR3UPSCALER_OPTION_LOW_RESOLUTION_MOTION_VECTORS": 1,
        "FFX_FSR3UPSCALER_OPTION_JITTERED_MOTION_VECTORS": 0,
        "FFX_FSR3UPSCALER_OPTION_INVERTED_DEPTH": 0,
        "FFX_FSR3UPSCALER_OPTION_APPLY_SHARPENING": int(pass_id == 7),
        "FFX_FSR3UPSCALER_OPTION_UPSAMPLE_SAMPLERS_USE_DATA_HALF": 0,
        "FFX_FSR3UPSCALER_OPTION_ACCUMULATE_SAMPLERS_USE_DATA_HALF": 0,
        "FFX_FSR3UPSCALER_OPTION_REPROJECT_SAMPLERS_USE_DATA_HALF": 0,
        "FFX_FSR3UPSCALER_OPTION_POSTPROCESSLOCKSTATUS_SAMPLERS_USE_DATA_HALF": 0,
        "FFX_FSR3UPSCALER_OPTION_UPSAMPLE_USE_LANCZOS_TYPE": 2,
    }
    gpu = arguments.sdk / "include/FidelityFX/gpu"
    if pass_id >= 200:
        family = "opticalflow"
        name = OPTICAL_FLOW[pass_id - 200]
        name = name if name.endswith("_v5") else name + "_pass"
        source = arguments.sdk / f"src/backends/vk/shaders/{family}/ffx_{family}_{name}.glsl"
        definitions = {"FFX_GLSL": 1, "FFX_GPU": 1, "FFX_HALF": 0}
    elif pass_id >= 100:
        family = "frameinterpolation"
        name = INTERPOLATION[pass_id - 100]
        source = arguments.sdk / f"src/backends/vk/shaders/{family}/ffx_{family}{'_' + name if name else ''}_pass.glsl"
        definitions = {"FFX_GLSL": 1, "FFX_GPU": 1, "FFX_HALF": 0,
            "FFX_FRAMEINTERPOLATION_OPTION_LOW_RES_MOTION_VECTORS": 1,
            "FFX_FRAMEINTERPOLATION_OPTION_JITTER_MOTION_VECTORS": 0,
            "FFX_FRAMEINTERPOLATION_OPTION_INVERTED_DEPTH": 0}
    else:
        family = "fsr3upscaler"
        source = arguments.sdk / f"src/backends/vk/shaders/fsr3upscaler/ffx_fsr3upscaler_{PASSES[pass_id]}_pass.glsl"
    binary = temporary / f"{pass_id}-{key}.spv"
    command = [arguments.compiler, "-V", "--target-env", "vulkan1.2", "-S", "comp", "-Os",
               *[f"-D{name}={value}" for name, value in definitions.items()],
               "-I" + str(arguments.overlay), "-I" + str(gpu), "-I" + str(gpu / family), str(source), "-o", str(binary)]
    result = subprocess.run(command, text=True, capture_output=True)
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    data = binary.read_bytes()
    resources, capabilities = reflect(data)
    return pass_id, key, data, resources, capabilities


def write_cpp(path, compiled):
    with path.open("w") as output:
        output.write('// Generated from the external AMD FidelityFX SDK; retain its MIT notice.\n')
        output.write('#include <FidelityFX/host/ffx_interface.h>\n#include <cstring>\n')
        for pass_id, key, data, resources, _ in compiled:
            prefix = f"fsr_{pass_id}_{key}"
            words = struct.unpack("<" + "I" * (len(data) // 4), data)
            output.write(f"alignas(4) static const uint32_t {prefix}_code[] = {{\n")
            for start in range(0, len(words), 12):
                output.write(",".join(f"0x{word:08x}" for word in words[start:start + 12]) + ",\n")
            output.write("};\n")
            for kind, entries in resources.items():
                if not entries:
                    continue
                output.write(f"static const char* {prefix}_{kind}_names[] = {{" + ",".join(json.dumps(e[0]) for e in entries) + "};\n")
                for index, field in ((1, "bindings"), (2, "counts"), (3, "spaces")):
                    output.write(f"static const uint32_t {prefix}_{kind}_{field}[] = {{" + ",".join(str(e[index]) for e in entries) + "};\n")
            output.write(f"static const FfxShaderBlob {prefix}_blob = {{reinterpret_cast<const uint8_t*>({prefix}_code), sizeof({prefix}_code),\n")
            output.write(",".join(str(len(resources[kind])) for kind in KINDS) + ",\n")
            for kind in KINDS:
                output.write(",".join(f"{prefix}_{kind}_{field}" for field in ("names", "bindings", "counts", "spaces"))
                             if resources[kind] else "nullptr,nullptr,nullptr,nullptr")
                output.write(",\n")
            output.write("};\n")
        output.write('extern "C" FfxErrorCode ffxGetPermutationBlobByIndex(FfxEffect effect, FfxPass pass, FfxBindStage stage, uint32_t options, FfxShaderBlob* out) {\n')
        output.write('if (!out) return FFX_ERROR_INVALID_POINTER;\n')
        output.write('if (stage != FFX_BIND_COMPUTE_SHADER_STAGE) return FFX_ERROR_INVALID_ARGUMENT;\n')
        output.write('const FfxShaderBlob* blob = nullptr;\nif (effect == FFX_EFFECT_FSR3UPSCALER) {\n')
        output.write('if (!(options & 4u) || (options & 24u)) return FFX_ERROR_INVALID_ARGUMENT;\nswitch (pass) {\n')
        for pass_id in range(len(PASSES)):
            output.write(f"case {pass_id}: switch (options & 3u) {{\n")
            for key in range(4):
                output.write(f"case {key}: blob = &fsr_{pass_id}_{key}_blob; break;\n")
            output.write("} break;\n")
        output.write('default: return FFX_ERROR_INVALID_ENUM;\n}\n')
        for effect, base, passes in (("FRAMEINTERPOLATION", 100, INTERPOLATION), ("OPTICALFLOW", 200, OPTICAL_FLOW)):
            output.write(f'}} else if (effect == FFX_EFFECT_{effect}) {{\n')
            if base == 100:
                output.write('if (!(options & 1u) || (options & 6u)) return FFX_ERROR_INVALID_ARGUMENT;\n')
            output.write('switch (pass) {\n')
            for pass_id in range(len(passes)):
                output.write(f'case {pass_id}: blob = &fsr_{base + pass_id}_0_blob; break;\n')
            output.write('default: return FFX_ERROR_INVALID_ENUM;\n}\n')
        output.write('} else return FFX_ERROR_INVALID_ENUM;\nstd::memcpy(out, blob, sizeof(*out)); return FFX_OK;\n}\n')
        output.write('extern "C" FfxErrorCode ffxIsWave64(FfxEffect, uint32_t, bool& enabled) { enabled = false; return FFX_OK; }\n')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sdk", type=Path, required=True)
    parser.add_argument("--compiler", required=True)
    parser.add_argument("--output", type=Path, required=True)
    arguments = parser.parse_args()
    arguments.output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="ac-fsr-shaders-") as directory:
        temporary = Path(directory)
        # SDK 1.1.4 allocates RGBA16F luma history, but its Vulkan callback
        # mistakenly declares rgba8. Match the actual resource, retaining the
        # original SDK header and notice only in this temporary build overlay.
        arguments.overlay = temporary / "gpu"
        callbacks = Path("fsr3upscaler/ffx_fsr3upscaler_callbacks_glsl.h")
        original = (arguments.sdk / "include/FidelityFX/gpu" / callbacks).read_text()
        before = "FSR3UPSCALER_BIND_UAV_LUMA_HISTORY, rgba8)"
        if before not in original:
            raise ValueError("Unexpected FidelityFX callback revision; review the luma-history format patch")
        patched = arguments.overlay / callbacks
        patched.parent.mkdir(parents=True)
        patched.write_text(original.replace(before, "FSR3UPSCALER_BIND_UAV_LUMA_HISTORY, rgba16f)"))
        # The SDK stores an inpainting weight in alpha, and its final pass only
        # rewrites pixels needing repair/HUD composition. Our presented images
        # are opaque: finalize alpha for every valid pixel after reading that
        # weight, preserving the SDK's interpolated RGB and HUD computation.
        inpainting = Path("frameinterpolation/ffx_frameinterpolation_inpainting.h")
        original = (arguments.sdk / "include/FidelityFX/gpu" / inpainting).read_text()
        before = "void computeInpainting(FfxInt32x2 iPxPos)\n{\n    FfxBoolean bWriteColor = false;"
        if before not in original:
            raise ValueError("Unexpected FidelityFX interpolation revision; review opaque output finalization")
        after = ("void computeInpainting(FfxInt32x2 iPxPos)\n{\n"
                 "    if (any(FFX_GREATER_THAN_EQUAL(iPxPos, DisplaySize()))) return;\n"
                 "    FfxBoolean bWriteColor = true;")
        patched = arguments.overlay / inpainting
        patched.parent.mkdir(parents=True, exist_ok=True)
        patched.write_text(original.replace(before, after))
        with concurrent.futures.ThreadPoolExecutor(max_workers=min(6, os.cpu_count() or 1)) as executor:
            futures = [executor.submit(compile_one, arguments, temporary, pass_id, key)
                       for pass_id in range(len(PASSES)) for key in range(4)]
            futures += [executor.submit(compile_one, arguments, temporary, base + pass_id, 0)
                        for base, passes in ((100, INTERPOLATION), (200, OPTICAL_FLOW))
                        for pass_id in range(len(passes))]
            compiled = sorted(future.result() for future in futures)
        write_cpp(arguments.output, compiled)
    capabilities = sorted(set().union(*(entry[4] for entry in compiled)))
    print(f"Compiled {len(compiled)} official FSR3.1.4 shader permutations; SPIR-V capabilities {capabilities}")


if __name__ == "__main__":
    main()
