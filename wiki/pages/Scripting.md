# Scripting

AdventureCraft Legacy embeds Mozilla Rhino **{{RHINO_VERSION}}**. A map's scripts live in its top-level `scripts` directory. The current loader scans only that directory depth, accepts `.js` files case-insensitively, and compiles them for the map.

## Built-in globals

The current scripting scope installs `time`, `world`, `chat`, `weather`, `effect`, `particle`, `sound`, `ui`, `screen`, `script`, `keyboard`, `renderer`, and (after player initialization) `player`. It also exposes constructors including `Item`, `UILabel`, `UISprite`, `UIRect`, `UIContainer`, `Model`, `ModelBlockbench`, `Vec3`, `Vec4`, and `VecRot`.

A minimal, current chat example is:

```javascript
chat.print("Hello, AdventureCraft!");
```

Use the generated [[Scripting API]] for the exact callable surface and links to its current Java declarations. Old examples using `player.sendMessage()` or generic `world.setBlock()` are not valid descriptions of the present wrapper API; for example, current world methods include `getBlockID`, `setBlockID`, and `setBlockIDAndMetadata`.

## Security

“Allow Java In Script” is off by default. Turning it on permits maps to load arbitrary Java classes and therefore execute arbitrary code. Only enable it for maps you fully trust; the setting cannot be changed at runtime.

Historical Fandom tutorials and downloadable scripts remain indexed in [[Fandom Baseline Index]], but they are classified as historical until tested against the current runtime. Community-map pages are excluded from that index.
