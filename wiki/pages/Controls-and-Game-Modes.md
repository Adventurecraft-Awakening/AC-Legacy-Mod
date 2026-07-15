# Controls and Game Modes

AdventureCraft has an adventure state for playing maps and a debug state for authoring them. **F4** toggles debug mode in the current input handler.

| Input | Current behavior |
| --- | --- |
| `F4` | Toggle debug mode |
| `F5` | Toggle third-person view |
| `F6` | In debug mode, reset all blocks in loaded chunks |
| `F7` | Open the item/block palette; inside it, move to the previous page |
| `F8` | Inside the palette, move to the next page |
| `Shift` + inventory key (normally `E`) | Open the palette while debug mode is active |
| `Ctrl+Z` | Undo while debug mode is active |
| `Ctrl+Y` | Redo while debug mode is active |
| `F11` | Toggle fullscreen |

Use `/mapedit [true|false]` to control map-editing mode. The historical claim that F5 toggles map editing is false for this codebase: F5 toggles third-person view.

The authoritative implementation is [`MixinMinecraft.java`](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/src/main/java/dev/adventurecraft/awakening/mixin/client/MixinMinecraft.java); command registration is in [`ServerCommands.java`](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java).
