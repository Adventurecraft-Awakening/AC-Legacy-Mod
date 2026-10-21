# Optional native rendering SDKs

AdventureCraft's original Vulkan/JNI bridge can use NVIDIA DLSS Ray
Reconstruction, NVIDIA Real-Time Denoisers (NRD), DLSS Super Resolution, AMD FSR
temporal upscaling, and supported frame generation. The built-in renderer remains
the default. These are engine integrations; maps do not supply SDKs or shader packs.

## Install for development

Read the pinned [DLSS license](https://github.com/NVIDIA/DLSS/blob/9981c6350dc58c0ce21f024fd65e31e851c31689/LICENSE.txt)
and [NRD license](https://github.com/NVIDIA-RTX/NRD/blob/792eff196afdd350fd9c3f862119017ccb438a0e/LICENSE.txt).
After accepting both, run:

```shell
./gradlew setupNativeRendering -PacceptNvidiaNativeLicense=true
./gradlew runClient
```

The first command downloads verified SDK files and builds `ac_render_native`.
It remembers the SDK path and exact license acceptance in ignored
`gradle-local.properties`. Later development builds and game launches install any
missing accepted files and rebuild changed native sources automatically. Acceptance
of the separate SHARC SDK does not enable this setup.

The pinned cache lives under
`~/.gradle/sdks/ac-native/dlss310.9.16-nrd4.17.3-fsr1.1.4-v1/<platform>` by default.
Use `-PnativeRenderingSdkRoot=/absolute/external/path` to choose another location
outside the checkout. Downloads are hash-checked, extracted with path checks, and
verified again against installation receipts. With the SDK and ordinary Gradle
dependencies already cached, `./gradlew setupNativeRendering --offline` performs
no downloads; missing or damaged required files cause an error.

Without remembered or explicit acceptance, ordinary builds do not download these
SDKs and use the built-in rendering paths. Remove the two `nativeRendering*`
entries from the local properties file to stop automatic native development setup.

## Build requirements and paths

The setup targets Linux x86-64 and Windows x86-64. Native compilation needs
JDK 27, CMake 3.30 or newer, Ninja, a C++17 compiler, Vulkan development files,
Python 3, and `glslangValidator`. The installer supplies the pinned SDK sources
and DirectX Shader Compiler used for NRD's Vulkan shaders. Runtime availability
also depends on the actual GPU, driver, enabled Vulkan features, and SDK probes;
an installed SDK alone does not make every mode available.

Useful tasks are `installNativeRenderingSdks`, `configureNativeRendering`,
`buildNativeRendering`, and `setupNativeRendering`. Native outputs are
`build/native/renderer/libac_render_native.so` on Linux and
`build/native/renderer/ac_render_native.dll` on Windows. Development runs receive
the absolute paths through `ac.nativeRendering.library` and
`ac.nativeRendering.sdkRoot`. Standalone launches must supply those properties
and the matching native build themselves.

## Rendering behavior

Open **Video Settings → Ray Tracing**. The page groups lighting and image
reconstruction controls. Buttons show supported saved selections; tooltips show
capabilities, active status, and reasons for unavailable choices. Unavailable
preferences remain saved while their labels show the fallback.

- **Reconstruction:** Built-in, DLSS RR, or NRD. DLSS RR includes DLSS Super
  Resolution. NRD filters separate diffuse/specular direct lighting and diffuse
  indirect lighting, preserving the engine's material remodulation.
- **Upscaler:** Cubic, DLSS SR, or FSR. Render Scale supplies the world resolution;
  native temporal reconstruction replaces the built-in TAA/spatial resolve for
  that frame. Hands and interface rendering remain at native resolution.
- **Frame Generation:** OFF, DLSS, or FSR when supported. DLSS produces an
  interpolated image and a real image; the renderer checks its GPU-written disable
  flag after the source frame fence. FSR uses the official Vulkan optical-flow and
  interpolation compute, including on supported Linux devices, with host-owned
  presentation rather than AMD's Windows swapchain. Both present generated before
  real through FIFO and skip interpolation on reset. FIFO preserves the pair but
  can add latency; it does not guarantee even spacing or a particular frame-rate
  gain. Simulation and input cadence remain unchanged.

SR and frame generation can operate with classic rendering when supported;
ray-lighting reconstruction needs Ray Tracing. Classic display colors retain
their original treatment. Camera cuts and resize reset history. Incompatible
views, unavailable SDKs, and native evaluation failures use the built-in fallback
instead of reporting an inactive vendor mode as enabled.

Motion vectors describe camera-relative world reprojection. Reactive masks mark
dynamic draws and changed texture footprints, but full skinned/per-object motion
is not exported. Fast actors, particles, disocclusions, and animated materials
can still expose reconstruction or interpolation artifacts. Native features need
a compatible single-camera world pass; frame generation also needs a presentation
window. This integration does not claim feature or image-quality parity with
another engine's renderer.

## Pinned components and licensing

The authoritative versions, download URLs, SHA-256 hashes, platform files, and
accepted license hashes are in the
[download manifest](../../buildSrc/src/main/resources/native-rendering-sdks.json).

| Component | Pinned version | License source |
|---|---|---|
| NVIDIA DLSS | 310.9.16 | [NVIDIA RTX SDK license](https://github.com/NVIDIA/DLSS/blob/9981c6350dc58c0ce21f024fd65e31e851c31689/LICENSE.txt) |
| NVIDIA NRD | 4.17.3 | [NVIDIA RTX SDK license](https://github.com/NVIDIA-RTX/NRD/blob/792eff196afdd350fd9c3f862119017ccb438a0e/LICENSE.txt) |
| NVIDIA NRI | 179 | MIT, retained in the cache |
| NVIDIA MathLib | 11 | MIT, retained in the cache |
| NVIDIA ShaderMake | `18f5a344` | MIT, retained in the cache |
| AMD FidelityFX SDK | 1.1.4, FSR SR 3.1.4 | [AMD MIT license](https://github.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK/blob/c6efa6bf7f2027b3ec94f28578bb5965eabb9e55/LICENSE.txt) |
| Vulkan-Headers / VMA | 1.4.349 / `b3cbbb43` | Supplied upstream licenses, retained in the cache |
| DirectX Shader Compiler | 1.8.2505 | Supplied license and notices, retained in the cache |

SDK sources and runtime libraries stay outside the repository and are not copied
into mod or sources JARs. The separately built shared library contains vendor
implementations; AdventureCraft's MIT license does not replace their licenses.
Keep their supplied notices and review their distribution terms before shipping
native binaries. SHARC is a separate optional integration described in
[its own README](../sharc/README.md).
