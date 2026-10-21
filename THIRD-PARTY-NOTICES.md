# Third-party notices

AdventureCraft's source license is in `LICENSE`. LWJGL 3 notices are in
`LICENSE-LWJGL3`.

## Optional NVIDIA RTX SHARC

Builds created with `-PsharcSdkDir` can include compiled NVIDIA RTX SHARC 1.8.3
shaders. The NVIDIA implementation is separately licensed under the NVIDIA RTX
SDKs License and supplement; AdventureCraft's MIT license does not cover it.

**This software contains source code provided by NVIDIA Corporation.**
NVIDIA RTX is a trademark of NVIDIA Corporation.

SDK-enabled archives include the supplied license and attribution in
`META-INF/licenses/NVIDIA-SHARC.txt`. Ordinary builds include no NVIDIA SHARC
implementation. SDK headers are supplied externally and are not redistributed
in this repository or its sources archives. Setup, the pinned revision and
license links are in [the integration notes](third_party/sharc/README.md).

## Optional native reconstruction

The development-only native bridge uses separately installed NVIDIA DLSS
**310.9.16** and NRD **4.17.3**. Their NVIDIA RTX SDK licenses apply independently
of AdventureCraft's MIT license. Explicit acceptance of those pinned licenses is
required before automatic installation; accepting SHARC does not accept them.
The native bridge also builds AMD FidelityFX SDK **1.1.4** components, including
FSR temporal upscaling and optical-flow/frame-interpolation compute, under AMD's
supplied MIT license.

Native build dependencies include NVIDIA NRI, MathLib and ShaderMake (MIT),
Vulkan-Headers (Apache-2.0 or MIT), Vulkan Memory Allocator (MIT), and Microsoft's
DirectX Shader Compiler (its supplied license and notices). The installer pins
revisions and SHA-256 hashes and retains the upstream license files in the
external SDK cache. These sources, runtime libraries, and the native shared
library are not packaged in the mod or sources JARs. The separately built native
library contains vendor implementations and remains subject to their licenses.

See [native integration and license links](third_party/native-rendering/README.md)
and the [pinned download manifest](buildSrc/src/main/resources/native-rendering-sdks.json).
