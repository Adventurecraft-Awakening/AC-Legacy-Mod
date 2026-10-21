# Optional NVIDIA RTX SHARC SDK

AdventureCraft's optional NVIDIA RTX™ SHARC integration uses NVIDIA's **Spatially Hashed Radiance Cache** shader library. The integration code in this repository does not replace the upstream algorithm. This directory records its supported upstream revision; it contains no NVIDIA SDK headers or copied implementation.

## License and attribution

The supported SDK is separately licensed under the [NVIDIA RTX SDKs License and supplement](https://github.com/NVIDIA-RTX/SHARC/blob/4e21b585c33c83d723ca9a1e11bbb1090d145793/License.md). It is not covered by AdventureCraft's MIT license. Sections 1–2 and 4 govern use and distribution; supplement section 7 covers attribution and NVIDIA marks. Review those terms before using or distributing SDK-enabled builds. SDK source redistribution is not authorized by this repository's MIT license.

Attribution for builds containing the optional NVIDIA RTX SHARC implementation:

> This software contains source code provided by NVIDIA Corporation.

NVIDIA RTX is a trademark of NVIDIA Corporation. An SDK-enabled build must retain the applicable upstream notices and satisfy NVIDIA's distribution requirements. Supplying a local SDK path does not change the SDK's license or automatically accept its terms for anyone else. Builds without the optional SDK retain AdventureCraft's existing indirect-lighting fallback.

## Install for local development

After accepting the NVIDIA license linked above, run:

```shell
./gradlew installSharcSdk -PacceptSharcLicense=true
./gradlew build
```

The explicit installer downloads the five pinned headers and license directly
from NVIDIA's repository, verifies their SHA-256 hashes, and installs them in
`<Gradle user home>/sdks/nvidia-sharc/<commit>/`. It records the accepted license
alongside the SDK and writes the SDK path into this checkout's ignored
`gradle-local.properties`. It preserves other local properties. The second Gradle
invocation reads that path: ordinary `build`, `assemble`, IDE Gradle builds, and
`runClient` then include SHARC without repeating a flag. Installation does not
change video preferences; select **Ray Tracing: ON** and **Indirect Light: SHARC
64MiB**, **128MiB**, or **256MiB** in Video Settings.

The installer never adds NVIDIA source to the repository. Ordinary builds do not
download or accept the SDK. Developers without a local SDK still build the
existing vertex-cache fallback. Override the local path with
`-PsharcSdkDir=/other/sdk`, or use `-PsharcSdkDir=` for an SDK-free build.

## Supply an existing local SDK

Obtain the SDK directly from the [official NVIDIA SHARC repository](https://github.com/NVIDIA-RTX/SHARC), review its license, and select this revision:

```text
Version: 1.8.3
Commit:  4e21b585c33c83d723ca9a1e11bbb1090d145793
```

The local SDK directory must have this layout:

```text
<sharc-sdk>/
  License.md
  include/
    HashGridCommon.h
    HashGridTypes.h
    SharcCommon.h
    SharcGlslHelpers.h
    SharcTypes.h
```

Point the build at that directory:

```shell
./gradlew build -PsharcSdkDir=/absolute/path/to/sharc-sdk
```

Without `sharcSdkDir` in Gradle properties or `gradle-local.properties`, ordinary builds do not require or download the SDK. Do not add the external checkout or its headers to this repository. `verifySharcSdk` checks all five header hashes and the license hash recorded in `provenance.json` before compiling optional shaders. Other SDK revisions can change shader interfaces, layouts, and safety behavior and require a separate compatibility review.

SDK-enabled builds package compiled shaders and `META-INF/licenses/NVIDIA-SHARC.txt`, containing the attribution above and the supplied upstream license. They do not package SDK headers in either the mod archive or its sources archive. Rebuilding without `sharcSdkDir` removes stale optional shader binaries and their generated NVIDIA notice.

The pin includes upstream fixes from 1.8.2 for resolve-probe bounds and from 1.8.3 for failed cache allocation during updates. See the [upstream changelog](https://github.com/NVIDIA-RTX/SHARC/blob/4e21b585c33c83d723ca9a1e11bbb1090d145793/CHANGELOG.md).

## Integration contract

The following facts describe the supported upstream configuration, rather than a second implementation. See NVIDIA's [integration guide](https://github.com/NVIDIA-RTX/SHARC/blob/4e21b585c33c83d723ca9a1e11bbb1090d145793/docs/Integration.md) and [shader interfaces](https://github.com/NVIDIA-RTX/SHARC/tree/4e21b585c33c83d723ca9a1e11bbb1090d145793/include) when updating the wrapper.

- Include `SharcGlslHelpers.h` before `SharcCommon.h`, with `SHARC_ENABLE_GLSL=1`. `SharcCommon.h` includes the remaining three headers.
- Compile update shaders with `SHARC_UPDATE=1`, and query shaders with `SHARC_QUERY=1`. Keep pass-specific definitions consistent across shared types.
- The wrapper uses 64-bit hash keys and SSBO atomics: `HASH_GRID_COMPACT=0`, `HASH_GRID_ENABLE_64_BIT_ATOMICS=1`. Material demodulation and separate emissive storage are enabled. Responsive lighting and directional SH encoding are disabled.
- Cache resampling and adjacent-level blending are disabled, with `SHARC_PROPAGATION_DEPTH=4` and `SHARC_LINEAR_PROBE_WINDOW_SIZE=0`; resolve works on individual entries before the read-only query snapshot is refreshed.
- Native fp16 arithmetic and storage remain necessary when `SHARC_USE_FP16=0`, because resolved cache entries contain native fp16 fields. The minimal headers do not invoke subgroup operations or shared-memory 64-bit atomics.
- Allocate equal-capacity hash, accumulation, and resolved arrays with strides **8**, **16**, and **16 bytes**, respectively. Hash capacity must accommodate the 16-slot probe bucket. Clear all three arrays before first use.
- The integration adds a **24-byte read-only snapshot per cell** so query passes can read a stable cache while queued update/resolve work modifies the persistent SDK buffers. Thus the selectable cache budgets are **64**, **128**, and **256 MiB** for capacities `2^20`, `2^21`, and `2^22`, respectively; screen-sized capture/filter resources are additional.
- `SharcParameters` carries the hash-grid parameters and data, radiance scale, and accumulation/resolved buffer references. Use absolute world positions consistently for hits and the grid camera.
- Begin each update path with `SharcInit`. Submit hit radiance with `SharcUpdateHit`; stop when it returns false. Apply the newly sampled segment throughput through `SharcSetThroughput`. Submit environment radiance through `SharcUpdateMiss` on a miss.
- Resolve entries through `SharcResolveEntry`. In the baseline configuration, resolve updates persistent resolved data in place and clears accumulation entries for the next update.
- Query eligible non-primary hits through `SharcGetCachedRadiance`; a successful query terminates that path. Retain explicit tracing when a segment is shorter than its cache voxel or when a sharp specular lobe needs finer detail.
- Synchronize cache writes between update, resolve, and query passes, including across queued frames. Changes in geometry, materials, lighting, or world identity require the integration's invalidation policy.
