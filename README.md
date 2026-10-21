# AdventureCraft Legacy

[![Build](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/actions/workflows/build.yml/badge.svg)](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

AdventureCraft Legacy is a Fabric-based revival of AdventureCraft for Minecraft Beta 1.7.3.

## Links

- [Wiki](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/wiki)
- [AdventureCraft launcher](https://adventurecraft.dev/launcher)
- [Issue tracker](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/issues)

## Building

Without a local SDK, builds use the vertex indirect-lighting cache. To install
the pinned NVIDIA SHARC SDK for development after accepting its separate license:

```shell
./gradlew installSharcSdk -PacceptSharcLicense=true
./gradlew build
```

The installer keeps the SDK outside this repository and saves its path in ignored
`gradle-local.properties`. Subsequent builds and `runClient` launches include SHARC.
Open **Video Settings → Ray Tracing**, enable **Ray Tracing**, and select
**Indirect Light: SHARC 64MiB** to use it.
An existing SDK can instead be supplied with `-PsharcSdkDir=/absolute/path/to/sdk`;
use `-PsharcSdkDir=` to build without it. See
[setup, licensing, and quality settings](third_party/sharc/README.md).

Use JDK 27 for development and game launches. Gradle selects the Java 27 toolchain
and can download it when needed. The desktop launcher bypasses the legacy applet
bootstrap, so Minecraft Beta does not need the removed `java.applet` APIs.

```shell
./gradlew assemble
```

On Windows, use `./gradlew.bat assemble`. This compiles shaders and packages the
game. Build artifacts are written to `build/libs`. Use
`./gradlew runClient` to playtest the normal development profile.

The optional native reconstruction bridge adds NVIDIA DLSS Ray Reconstruction,
NRD denoising, DLSS Super Resolution, AMD FSR temporal upscaling, and DLSS/FSR
frame generation on supported devices. After reviewing and accepting the pinned DLSS and NRD licenses:

```shell
./gradlew setupNativeRendering -PacceptNvidiaNativeLicense=true
./gradlew runClient
```

Accepted setup is remembered locally. Subsequent development builds and launches
verify the external SDK cache and rebuild the native bridge as needed; a complete
cache also works with Gradle's `--offline` option. Vendor SDKs and runtime libraries
stay outside the repository and mod JAR. Without accepted setup, the built-in
renderer remains available and no native SDK is downloaded. See
[native setup, requirements, and licensing](third_party/native-rendering/README.md).
Choose supported modes in **Video Settings → Ray Tracing**; the page explains
unavailable modes and retains saved preferences.

## License

AdventureCraft Legacy's own code is available under the [MIT License](LICENSE).
Bundled compatibility sources retain their upstream licenses; see
[third-party notices](THIRD-PARTY-NOTICES.md).
