# Native registry render assets

The AC-Legacy client exporter owns this directory. Do not hand-edit its PNGs or copy images from Fandom.

`render-manifest.json` uses format 1:

```json
{
  "format": 1,
  "width": 128,
  "height": 128,
  "contains_community_maps": false,
  "blocks": [
    {
      "field": "lockedDoor",
      "id": 150,
      "file": "blocks/lockedDoor.png",
      "sha256": "<lowercase SHA-256 of the PNG>",
      "alt": "Native isometric render of the Locked Door block"
    }
  ],
  "items": [
    {
      "field": "boomerang",
      "id": 456,
      "file": "items/boomerang.png",
      "sha256": "<lowercase SHA-256 of the PNG>",
      "alt": "Native inventory render of the Boomerang item"
    }
  ]
}
```

The production manifest must contain exactly one record for every field parsed from `AC_Blocks.java` and `AC_Items.java`. File names are deliberately keyed by registry field so localized-name collisions and grouped variants remain distinct. All images in one manifest have its declared square dimensions and must be non-interlaced 8-bit RGBA PNGs.

Item `id` values are runtime IDs. In Beta 1.7.3, item constructors accept an item-array index and the resulting `Item.id` is that value plus 256; for example, `new AC_ItemBoomerang(200)` registers runtime ID 456.

The exporter uses the current client renderer with a synthetic empty scene. It must never load community maps, and the manifest must keep `contains_community_maps` set to `false`.
