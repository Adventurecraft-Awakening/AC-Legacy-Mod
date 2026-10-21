# Materials

Map materials define rough stone, shiny metal, foliage, glass, water, and glowing surfaces. Add a `materials.json` file and companion PNGs to your map; no shader programming is required. Follow [[Shader Creation]] for a practical walkthrough and graphics settings. This page is the file-format reference.

Normal maps and other companion-based surface effects require **Ray Tracing** on supported hardware. Classic rendering keeps the ordinary albedo textures and block lighting.

## Start a material set

1. Open the map and run `/materials defaults` if it has no manifest yet.
2. Edit the files in the **source map directory shown in chat**.
3. Save your changes, then run `/materials reload`.
4. Check surfaces in daylight, shadow, and beside a light. Check glass from both sides.

Maps without a manifest use built-in material assignments without creating files. **Create Random Map** creates a manifest and any missing albedo and companion atlases automatically.

### Files and atlas layout

With the default paths, place these files together in the map folder:

```text
materials.json
terrain.png     terrain_n.png     terrain_s.png
terrain2.png    terrain2_n.png    terrain2_s.png
terrain3.png    terrain3_n.png    terrain3_s.png
```

Each atlas is a **16 × 16 grid of tiles**. Companions should match the albedo image's dimensions and tile layout.

| Atlas ID | Albedo | Normal/AO/height | Specular/emission |
|---|---|---|---|
| `0` | `terrain.png` | `terrain_n.png` | `terrain_s.png` |
| `1` | Shares atlas `0` by default | Shares atlas `0` | Shares atlas `0` |
| `2` | `terrain2.png` | `terrain2_n.png` | `terrain2_s.png` |
| `3` | `terrain3.png` | `terrain3_n.png` | `terrain3_s.png` |

The manifest can assign different filenames. A leading `/`, such as `/terrain_n.png`, refers to a map texture resource, not your computer's root directory. Missing companions use neutral material values.

### Commands

| Command | Effect |
|---|---|
| `/materials defaults` | Creates a manifest, copies missing albedo images, and generates missing companions. Does nothing if `materials.json` already exists. |
| `/materials reload` | Reloads material files and map textures, and refreshes lighting. Does not write material files. |
| `/materials save` | Reads the manifest from disk, generates missing companions, and rewrites the manifest in the game's standard format. Preserves existing PNGs; does not copy missing albedo images. |
| `/textureatlas` | Opens the loaded atlas viewer to check resource paths and images. |

Back up a hand-edited manifest before `/materials save`: it adds generated palette/block entries and does not preserve comments or unknown top-level fields. Save your text editor's changes before using `reload` or `save`.

## Blocks and texture tiles

Use **`blocks` for behavior**: which block emits light, its light color/radius, gameplay lighting, and surface flags. Use **`tiles` for appearance**: how a texture responds to light.

A block may use different tiles on different faces, and several blocks may share one tile. Changing a block entry does not repaint its textures. Changing a shared tile can affect several block types.

Tile profiles can also supply fallback light behavior, but this uses the block's bottom-face texture (side `0`), not every face. Prefer explicit block entries for lamps and torches.

### Keys and inheritance

| Location | Key/value | Meaning |
|---|---|---|
| `blocks` | `"207"` | Block ID `207`, all metadata values. |
| `blocks` | `"207:3"` | Block ID `207`, metadata `3` only. |
| `tiles` | `"2:115"` | Atlas `2`, tile `115`. |
| Profile | `"material": "stone"` | Selects a named preset from `palette`. |

Block IDs are `0..255`; metadata is `0..15`; atlas IDs are `0..3`; tile indices are `0..255`. Count tiles from zero, left to right, then top to bottom: `index = row × 16 + column`.

The game starts with generated block/tile assignments and merges your supplied fields into them. For block behavior and companion generation, priority runs from **defaults → selected palette preset → block/tile fields**. Existing generated fields remain when omitted, so explicit fields on an entry are the clearest way to change it. Changing `defaults.material` alone does not reassign generated blocks.

Editing a built-in palette name merges your changes into that preset. Block-wide and metadata-specific entries are processed **in file order**: put the block-wide entry first, then its exceptions. A later block-wide entry can overwrite earlier exceptions.

Omitted fields inherit; `null` does not reset them. The shorthand flags `thin`, `decorative`, and `water` enable a flag with `true`; `false` does not clear an inherited flag. Live appearance overrides have the additional rules under **Live tile overrides** below.

## Manifest example

This complete example gives one block subtype a warm light and adjusts one texture tile. Replace the IDs with those used by your map.

```json
{
  "version": 1,
  "textures": {
    "atlases": [
      {
        "id": 0,
        "albedo": "/terrain.png",
        "normal": "/terrain_n.png",
        "specular": "/terrain_s.png"
      },
      {
        "id": 2,
        "albedo": "/terrain2.png",
        "normal": "/terrain2_n.png",
        "specular": "/terrain2_s.png"
      },
      {
        "id": 3,
        "albedo": "/terrain3.png",
        "normal": "/terrain3_n.png",
        "specular": "/terrain3_s.png"
      }
    ]
  },
  "defaults": {
    "smoothness": 0.0,
    "f0": 0.04
  },
  "palette": {
    "warmLamp": {
      "emission": 15,
      "light": {
        "color": "#FFD194",
        "radius": 16.0,
        "intensity": 1.0
      }
    }
  },
  "blocks": {
    "207:3": {
      "material": "warmLamp",
      "thin": true,
      "gameplayLight": 14
    }
  },
  "tiles": {
    "2:115": {
      "smoothness": 0.3,
      "metallic": false,
      "porosity": 0.5
    }
  }
}
```

You may omit sections you do not need. Use valid JSON with quoted keys and no trailing commas. The game writes `version: 1`.

### Profile fields

These fields are accepted in `defaults`, `palette`, `blocks`, and `tiles`. Choose the scope according to the effect: block behavior or tile appearance.

| Field | Value | Practical meaning |
|---|---|---|
| `material` | Preset name | Selects a built-in or map-defined material. |
| `emission` | Integer `0..15` | Surface glow and nearby-light strength. Explicit block emission takes priority for that block's light behavior. |
| `light.color` | Hex string, e.g. `"#FFD194"` | Nearby light's color. |
| `light.radius` | Number, in blocks | Positive values set its reach; nonpositive values use the emission-derived radius. |
| `light.intensity` | Number `0` or greater | Nearby-light multiplier. `0` disables that light without removing surface glow. |
| `gameplayLight` | Integer `0..15` | Legacy block-light value, separate from the material's glow. |
| `thin` | Boolean | Use for thin crossed plants, which skip normal-map relief. |
| `decorative` | Boolean | Decorative grass treatment; normal-map relief is skipped. |
| `water` | Boolean | Marks a water surface. |
| `transmission` | Number `0..1` | Block values above zero enable translucent treatment; tile values adjust refraction strength. |
| `smoothness` | Number `0..1` | Rough at `0`, smooth at `1`. |
| `f0` | Number `0..1` | Reflectivity viewed straight on; `0.04` is a useful nonmetal starting point. |
| `metallic` | Boolean | Metal appearance. Metal uses albedo as its reflection color unless the tile also supplies `f0`. |
| `porosity` | Number `0..1` | How strongly rain wets the surface. |
| `sss` | Number `0..1` | Reserved subsurface-scattering value; currently has no visible lighting effect. |
| `ior` | Number `1..4` | Refraction index. Without an override, glass uses `1.5` and water `1.333`. |
| `absorption` | `[r, g, b]`, each `0..64` | Color absorbed over distance through glass/water. Higher values remove more of that channel; zero preserves it. |
| `parallax` | Boolean | Permits optional height-map relief; allowed by default, with the client quality setting initially OFF. |

Use the `light` object for light settings. Legacy top-level `color`, `radius`, and `intensity` are also accepted, but matching values inside `light` take priority.

Built-in presets: `stone`, `granular`, `wood`, `metal`, `cloth`, `foliage`, `glass`, `ice`, `water`, `torch`, `lamp`, and `lava`.

The `torch` and `lava` presets include emission. The `lamp` preset supplies warm light settings without forcing emission, so switchable lights can retain their own on/off state.

## Paint companion atlases

Albedo contains ordinary color. Companion channels contain **data**: preserve their exact byte values and export RGBA so height and emission alpha are retained. Avoid color corrections or color-space conversions on companion channels.

### Normal atlas: `_n.png`

| Channel | Paint this |
|---|---|
| R | Normal X; `128` is approximately flat. |
| G | Normal Y, **DirectX green-down** convention; `128` is approximately flat. |
| B | Ambient occlusion: `0` fully occluded, `255` unoccluded. This is **not normal Z**. |
| A | Height: `255` original top plane, `0` deepest cavity. Optional parallax relief uses a shallow `0.08`-block depth. |

A flat, unoccluded normal tile is RGBA **`(128, 128, 255, 255)`**.

### Specular atlas: `_s.png`

| Channel | Paint this |
|---|---|
| R | Smoothness: `0` rough, `255` smooth. |
| G | `0..229`: nonmetal reflectivity (`10` ≈ `0.04`). `230..237`: fixed metal presets. `238..255`: metal using albedo as reflection color. |
| B | `0..64`: porosity. `65..255`: subsurface scattering from zero to full strength, currently without a visible effect. These share one channel. |
| A | **`255` means nonemissive**. `0..254` represents emission from zero to approximately full strength. |

A rough, nonemissive nonmetal is RGBA **`(0, 10, 0, 255)`**. Strong painted emission uses alpha **`254`, not `255`**. Image editors may hide RGB in transparent pixels; inspect and preserve those channel values.

### Live tile overrides

Individual appearance values in `tiles` replace the corresponding painted values after reload. For example, `"2:115": {"smoothness": 0.85, "porosity": 0.0}` adjusts only those properties; its normal, AO, height, and painted emission remain available. Explicit `0` and `false` count as overrides. Remove a field to reveal the painted value again.

An entry containing only individual appearance values changes **only fields written on that entry**. An entry with `"material": "metal"` instead selects the full resolved preset: defaults, then that preset, then the tile's explicit fields. Reloading palette edits affects tiles that explicitly select it. Defaults and palette entries alone do not replace painted channels throughout an atlas.

Tile `emission` replaces painted emission uniformly across that tile. Positive block emission still provides a minimum surface glow: a block with `emission: 15` cannot be darkened by painting `_s.A = 255` or setting tile emission to zero. For pixel-shaped glow, avoid positive block/preset emission and uniform tile emission on that surface.

Conversely, block `emission: 0` prevents its configured nearby light, but painted emissive pixels can still glow.

`parallax` has a wider effect: `"defaults": {"parallax": false}` disables relief on **all tiles**, even without tile entries. A tile's `parallax: true`, or a true value in its explicitly selected preset, can re-enable it. The client must also enable Parallax Mapping. Relief does not change silhouettes, geometry, or shadows, and is skipped on water, transmissive, thin, and decorative surfaces.

Put numeric `ior` and `absorption` overrides on **tiles**, or select their preset on a tile. Block-only values do not adjust these properties. Keep the block's translucent/water behavior appropriate; appearance settings do not change its shape.

### Generate or paint

`/materials defaults` and `/materials save` generate **missing companion files only**, using tile profiles. Generated normals are flat; generated specular tiles use the tile's material values. Changing block appearance fields does not update these independent tile profiles.

Paint companions for pixel detail; use tile overrides for uniform adjustments. Reloading JSON does not change PNGs. To regenerate an atlas, back up that companion, remove it, and run `/materials save`. This replaces the **whole missing atlas**, including any painted details you removed.

## Animate normal and specular companions

Keep the existing seven-column `animations.txt` format; add only the albedo entry:

```text
Torch, /terrain3.png, /animated/torch.png, 192, 128, 16, 16
```

This writes a `16 × 16` frame at pixel `(192, 128)` in `terrain3.png`. Add optional siblings beside the source:

```text
animated/torch.png       # albedo
animated/torch_n.png     # normal/AO/height
animated/torch_s.png     # smoothness/F0/porosity/emission
```

Custom sources contain **vertical frames**. Source width must equal the entry's frame width; source height must be an exact multiple of frame height. Rectangular frames are allowed. Siblings must match albedo's complete dimensions and frame layout to animate together. Missing/mismatched siblings leave static companions in use; check the log for mismatch warnings.

Create the full destination companion atlases first: strips do not create them. Custom atlas filenames follow `materials.json`. The destination rectangle must fit; different atlas resolutions work when its scaled coordinates are exact integers.

Loaded built-in strips such as `custom_water_still.png` and `misc/fan.png` also accept `_n`/`_s` siblings, using square horizontal or vertical frames. Procedural animations without a loaded albedo strip keep static companions. Save and run `/materials reload` to restart the sources together.

Tile overrides still apply over animated channels. Animated `_s.A` can brighten/darken painted glow, but does **not** animate nearby point-light intensity. Positive block emission still prevents the surface becoming dark. Indirect lighting may take time to catch up.

### Texture trigger variants

Replacing an atlas with `variant.png` also looks for `variant_n.png` and `variant_s.png`; absent siblings restore the original static companion. Reverting restores the texture set together. Existing animation strips continue writing their configured rectangles; an atlas replacement does not select new animation sources.

## Reload and package

Save your JSON and PNGs, run `/materials reload`, and check the source directory shown in chat if an edit is missing. Ship `materials.json`, all referenced atlases, `animations.txt`, and its source strips with the map. Graphics quality choices belong to each player's client settings.

## Troubleshooting and limits

| Symptom | What to check |
|---|---|
| Changes do not appear | Save the files in the source map directory, reload, and check the log for JSON or image warnings. |
| Smoothness or metal settings do nothing | Enable Ray Tracing and edit the correct `tiles` entry. Block-only appearance fields do not replace painted channels. |
| A painted lamp glows without lighting nearby blocks | Check block `emission` and `light.intensity`. Explicit block emission, including `0`, takes priority over tile/texture fallback light behavior. |
| A whole block glows instead of only flame pixels | Remove positive block/preset emission and uniform tile emission if painted alpha should control the glow. |
| White emission alpha gives no glow | `_s.A = 255` is nonemissive; use `1..254` for positive painted emission. |
| Relief looks inverted | Use green-down normals; blue contains AO, not normal Z. |
| Another block changes unexpectedly | Several blocks may share a tile. Use block entries for behavior. Translucent treatment is per block ID, so one transmissive subtype can affect the whole ID. |
| A Light Bulb at Light Value `0` still shows a bright patch | Light Bulb (`171`) is an invisible, non-solid emitter helper. Its setting changes only its own light, and transparent cells may reveal surfaces behind it. Use solid blocks for a visible wall. |

Clip Blocks provide an **invisible collision and traced-light barrier**; they do not visually fill a wall. Hidden clip walls still block traced lighting, regardless of transparent pixels in their debug icons. Other debug helpers keep their usual behavior.

There is no in-game material painter: edit the JSON and PNGs externally. Thin crossed plants need suitable artwork and alpha silhouettes because normal-map relief is skipped. Use [[Shader Creation]] for lighting, glass/water, and graphics-quality guidance.
