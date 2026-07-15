# Getting Started

## Players

AdventureCraft Legacy targets **Minecraft Beta 1.7.3**. The project recommends the [AdventureCraft launcher](https://adventurecraft.dev/launcher) for ordinary play because it handles the game and mod installation. Launcher behavior is maintained outside this repository, so consult the launcher site for current platform-specific steps.

Maps are discovered from the configured maps directory. Without a launcher override, the game uses `../maps` relative to its working directory. Each directory there is treated as a map; `description.txt` (two lines) and `thumbnail.png` are optional metadata.

Starting a map creates a save separately from the source map. Use **New Save** to begin and **Load Save** to resume an existing save.

## Developers

Requirements:

- JDK **25**
- Git
- No separate Gradle installation; the wrapper is committed

```shell
git clone https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod.git
cd AC-Legacy-Mod
./gradlew build
```

On Windows PowerShell, use `./gradlew.bat build`.

The configured compatibility values come from [`gradle.properties`](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/gradle.properties), while Java compatibility is set in [`build.gradle`](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/build.gradle).
