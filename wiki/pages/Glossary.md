# Glossary

## Adventure mode

The ordinary map-playing state. Debug-only authoring interactions and palette access are restricted while debug mode is inactive.

## Debug mode

The authoring state toggled with `F4`. It enables editing interactions, the palette, command chat in single-player, undo/redo shortcuts, and debug visualization controls.

## Map-editing mode

The state controlled by `/mapedit [true|false]`. It is distinct from both debug mode and the `F5` third-person-view control.

## Palette

The debug inventory of blocks and items opened with `F7` or the debug-mode inventory shortcut. `F7` and `F8` change pages while it is open.

## Script global

A pre-installed JavaScript object such as `world`, `player`, `chat`, or `ui`. Globals are created by the current scripting runtime rather than by a map script.

## Trigger block

A block participating in AdventureCraft's trigger-area system. Trigger blocks, triggerable blocks, redstone bridges, timers, and memory/inverter blocks have different roles; consult the generated [[Current Blocks]] reference for current implementations.

## Verified

A page whose current claims have repository evidence recorded in [[Verification Status]]. “Verified” applies to the configured AC-Legacy version and source revision, not automatically to the original mod or community scripts.
