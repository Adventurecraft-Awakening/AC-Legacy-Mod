# Shader Creation

Create your map's look with painted textures, materials, lighting, and weather.
This walkthrough uses AdventureCraft's built-in rendering; legacy OpenGL shader
packs do not run here. Use [[Materials]] for channel meanings and the manifest reference.

## Contents

- [Choose the supported workflow](#choose-the-supported-workflow)
- [Prepare a material test area](#prepare-a-material-test-area)
- [Paint a surface](#paint-a-surface)
- [Preview with graphics settings](#preview-with-graphics-settings)
- [Create an amber lamp](#create-an-amber-lamp)
- [Choose indirect lighting](#choose-indirect-lighting)
- [Author glass and water](#author-glass-and-water)
- [Test weather and wet surfaces](#test-weather-and-wet-surfaces)
- [Animate material details](#animate-material-details)
- [Reload and package the map](#reload-and-package-the-map)
- [Troubleshooting](#troubleshooting)

## Choose the supported workflow

| What you want to change | What to edit |
|---|---|
| Color, painted detail, or transparency | The albedo atlas, such as `terrain.png` |
| Bumps, shine, metal, or surface glow | Matching `_n.png` and `_s.png` companion atlases |
| A block's light color, light radius, or glass/water behavior | `materials.json` |
| Sunlight, rain, clouds, or fog | Map time, weather, and existing map/video settings |

Keep essential paths and interactions readable with **Ray Tracing OFF** as well
as ON. Players choose their own graphics settings; those preferences are not
packaged with your map.

## Prepare a material test area

1. Make a working copy of your map. Edit its **source map folder**, alongside
   `level.dat`, rather than a separate playthrough save.
2. Open the map and run `/materials defaults` if it has no `materials.json`.
   The chat message shows the directory. Existing material sets are preserved.
3. Open the manifest in a text editor and the atlases in an image editor that
   preserves RGBA channels, including color in transparent pixels.
4. Build a small courtyard and covered room with a floor, wall, lamp, glass,
   and shallow/deep pool. Use `/day` and `/night` for lighting comparisons.

The usual files are:

```text
<map>/
  materials.json
  terrain.png       terrain_n.png       terrain_s.png
  terrain2.png      terrain2_n.png      terrain2_s.png
  terrain3.png      terrain3_n.png      terrain3_s.png
```

New random maps already have material files; older maps use defaults until you
create them. See [[Materials]] if your manifest assigns different atlas names.

## Paint a surface

### Locate the tile

Use `/textureatlas` to view the loaded images. Each atlas has a 16 × 16 tile grid;
larger images keep that layout with larger tiles. Check the block's top, side,
and bottom before painting, since they can use different tiles.

Find the same tile in equal-sized albedo, normal, and specular images. A tile can be
shared by unrelated blocks; place those blocks nearby to catch unintended changes.

### Paint color and normal detail

1. Paint albedo color and transparency, then reload before adding other effects.
2. Start from the generated neutral normal tile. Add modest bumps and inspect
   them from several angles beside a lamp.
3. Follow [[Materials]] when exporting companions; ordinary RGB normal maps need repacking.

### Choose a surface response

Paint `_s` for rough stone, polished tile, metal, or glow. Use [[Materials]] for
channel meanings and starting values, changing one property at a time.

For a uniform tile change, use an appearance override in `materials.json`; remove
it to use the painted channel again. See [[Materials]] for choosing the right entry.

### Add shallow height detail

Choose **Parallax Mapping: Fast** or **Fancy**, then follow [[Materials]] to paint
shallow recesses. Compare front and oblique views of bricks with recessed mortar.

Height detail needs Ray Tracing and fades with distance. It does not change
outlines, collision, or shadows, or apply to water, glass, or thin plants.
Model deep openings and important silhouettes with actual geometry.

## Preview with graphics settings

Open **Video Settings → Ray Tracing** for lighting and image quality. Tooltips
explain availability and active modes. These preferences are not map files.

### Built-in HDR and bloom

Ray Tracing enables advanced lighting, reflections, water, atmosphere, and glow.
Pause after moving between bright and dark areas before judging a lamp. A glowing
texture and room illumination are different effects; tune both in the example
below. Ray Tracing OFF keeps the original appearance.

### Temporal antialiasing

**Temporal AA** reduces shimmering. Compare still views and slow movement around
leaves and animations; turn it off to check smearing. It also works without Ray Tracing.

### Render scale

Judge detail at **Native 100%**, then check a lower scale for softness or shimmer.
Hands and menus stay at native resolution.

### Choose reconstruction and frame generation

**Reconstruction** filters noisy lighting; **Upscaler** improves the lower-resolution
image. Available Built-in, DLSS, NRD, and FSR choices depend on the player's setup.
**Frame Generation** adds displayed frames without speeding up scripts or simulation.
Compare movement and animations, keeping the map readable with the default choices.

## Create an amber lamp

Repaint **Plant 1, subtype 3**: block `207:3`, **atlas 2, tile 115** in `terrain2.png`
(column 3, row 7, counting from zero). Its crossed-plant shape suits a small flame.

1. Paint that tile as a lamp or flame, leaving the surroundings transparent.
2. Merge this entry into the existing `blocks` object in `materials.json`.
   Keep the other entries and the rest of the manifest.

```json
{
  "207:3": {
    "material": "torch",
    "thin": true,
    "emission": 15,
    "light": {
      "color": "#FFD194",
      "radius": 12,
      "intensity": 0.8
    },
    "gameplayLight": 14
  }
}
```

3. Save, run `/materials reload`, and place that subtype in the covered room.
4. Compare day/night and Ray Tracing ON/OFF. Adjust the light radius to fit the
   room and the intensity to keep nearby surfaces readable.

The painted flame does not automatically choose the light color. Here `emission`
makes the surface luminous, `light` sets its warm illumination, and `gameplayLight`
provides original block lighting. See [[Materials]] for the fields and limits.
Texture emission cannot cancel emission already supplied by the block.

## Choose indirect lighting

**Indirect Light** changes bounced light. **Vertex cache** is the default;
**SHARC** choices use more memory. Check the tooltip if unavailable.

In a room with a bright opening and colored walls, let lighting settle, move,
and switch a lamp with a trigger. Check Vertex cache and Ray Tracing OFF too.

## Author glass and water

Start with existing glass or water. Painting stone blue does not give it water
behavior. Use [[Materials]] for custom variants; changing appearance does not make
a block flow or change its collision rules.

For glass, compare both sides against bright and dark backgrounds. Tune color,
transparency, and transmission together. For water, compare a shallow shelf, a
deep pool, and the underwater view. Keep animated texture detail visible; waves
do not move the pool's outline.

Check actors and particles through the surface too. Reflections do not include
every kind of object, so avoid puzzles that depend on a perfect mirror image.

## Test weather and wet surfaces

Compare the same warm-weather floor outdoors and under a roof. Rain gradually
adds a dark, reflective wet appearance to eligible surfaces; drying takes time.
Porosity affects the response, while metals, glowing surfaces, glass, water, and
thin plants do not behave like an ordinary wet stone floor.

A map script can start a repeatable rain comparison:

```javascript
weather.setPrecipitating(true);
weather.setThundering(false);
```

Run `weather.setPrecipitating(false);` later to watch it dry. See [[Scripting]]
for running scripts. Cold/snowy areas, covered floors, and steep walls give
different results. Also check clouds and your map's fog overrides, which can
change the atmosphere around the same materials.

## Animate material details

Keep the existing albedo animation in `animations.txt`, and add matching `_n`
and `_s` source strips beside its image. For example, this existing entry:

```text
Torch, /terrain3.png, /animated/torch.png, 192, 128, 16, 16
```

Add `animated/torch_n.png` and `animated/torch_s.png` with the same frame layout.
A 16 × 64 strip holds four vertical 16 × 16 frames; all channels advance together.
Destination companion atlases must also exist.

Reload and watch several loops beside a static tile. Albedo animation works with
Ray Tracing off; normal/specular appearance needs it on. Glowing frames do not
automatically animate room lighting. Missing companions keep the static response.
See [[Materials]] for layouts, overrides, and texture-trigger variants.

## Reload and package the map

1. Save all PNG and JSON changes in the source map folder.
2. Run `/materials reload`; wait for lighting and wetness to settle again.
3. Check shared tiles, daylight, darkness, rain, and both Ray Tracing modes.
4. Include the manifest, referenced atlases, animation strips, `animations.txt`,
   and other resources used by the map when distributing it.

Use `/materials reload` for ordinary edits. Back up before `/materials save`,
which rewrites the manifest and creates missing companions.

## Troubleshooting

| Symptom | Check |
|---|---|
| A shader pack has no effect | Use the built-in material workflow; legacy OpenGL packs are unsupported |
| Bumps, shine, or height are missing | Enable Ray Tracing and the relevant option; check companion paths and tile positions |
| A material edit has no effect | Save, reload, and check whether a tile override replaces the painted property |
| A surface glows unexpectedly | Check specular alpha and block emission in [[Materials]] |
| A lamp glows but the room stays dark | Check emission, radius, intensity, walls, and the correct block subtype |
| A Light Bulb at value 0 looks like a hole | It is a non-solid helper; use real solid blocks for a visible wall, or invisible Clip Blocks to block traced light |
| Rain does not wet a floor | Allow time and compare a warm, exposed stone floor with a covered one |
| Animation changes color but not shine | Check matching companion strips, destination atlases, and tile overrides |
| Fine detail shimmers or smears | Compare Native scale, Temporal AA ON/OFF, and slow camera movement |
| A graphics choice is unavailable | Read its tooltip; keep a usable appearance with the available/default modes |
| Settings disappear after reload | Check JSON syntax and the game log; keep a backup of the last working manifest |
