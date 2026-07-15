from __future__ import annotations

import hashlib
import json
import sys
import tempfile
import unittest
import zlib
from pathlib import Path


TOOLS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS_DIR))

import wiki  # noqa: E402


def write_rgba_png(path: Path, width: int = 2, height: int = 2) -> None:
    def chunk(kind: bytes, payload: bytes) -> bytes:
        checksum = zlib.crc32(payload, zlib.crc32(kind)) & 0xFFFFFFFF
        return len(payload).to_bytes(4, "big") + kind + payload + checksum.to_bytes(4, "big")

    ihdr = width.to_bytes(4, "big") + height.to_bytes(4, "big") + bytes((8, 6, 0, 0, 0))
    rows = b"".join(b"\0" + bytes(width * 4) for _ in range(height))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(rows))
        + chunk(b"IEND", b"")
    )


class FeaturePageRenderingTests(unittest.TestCase):
    def setUp(self) -> None:
        self.blocks = [
            {
                "name": "Locked Door",
                "field": "lockedDoor",
                "id": 150,
                "class": "AC_BlockLockedDoor",
                "line": 10,
            }
        ]
        self.items = [
            {
                "name": "Boomerang",
                "field": "boomerang",
                "id": 456,
                "constructor_id": 200,
                "class": "AC_ItemBoomerang",
                "line": 20,
            }
        ]
        self.entities = [
            {
                "name": "Boomerang",
                "class": "AC_EntityBoomerang",
                "id": 1001,
                "line": 30,
            }
        ]
        detail = {
            "purpose": "Purpose.",
            "behavior": "Behavior.",
            "mode_notes": None,
            "interactions": [],
            "evidence": ["README.md"],
        }
        self.details = {
            "blocks": {"lockedDoor": detail},
            "items": {"boomerang": detail},
            "entities": {"AC_EntityBoomerang": detail},
        }

    def test_prefers_human_names_and_namespaces_collisions(self) -> None:
        targets = wiki.build_feature_page_targets(
            self.blocks, self.items, self.entities
        )

        self.assertEqual("Locked-Door", targets[("Block", "Locked Door")])
        self.assertEqual("Item-Boomerang", targets[("Item", "Boomerang")])
        self.assertEqual("Entity-Boomerang", targets[("Entity", "Boomerang")])

        script_target = wiki.build_feature_page_targets(
            [], [], [{"name": "Script"}]
        )
        self.assertEqual("Entity-Script", script_target[("Entity", "Script")])

    def test_renders_canonical_and_compatibility_pages(self) -> None:
        targets = wiki.build_feature_page_targets(
            self.blocks, self.items, self.entities
        )
        pages, evidence = wiki.render_feature_pages(
            self.blocks, self.items, self.entities, self.details, targets
        )

        self.assertEqual(pages["Locked-Door.md"], pages["Block-Locked-Door.md"])
        self.assertIn("Locked-Door", evidence)
        self.assertIn("Block-Locked-Door", evidence)
        self.assertIn("[Locked Door](Locked-Door)", wiki.render_blocks(self.blocks, targets))

        aliases, _ = wiki.render_alias_pages(
            self.blocks, self.items, self.entities, targets
        )
        self.assertNotIn("Locked-Door.md", aliases)
        self.assertIn("Boomerang.md", aliases)
        self.assertIn("[Item: Boomerang](Item-Boomerang)", aliases["Boomerang.md"])
        self.assertIn("[Entity: Boomerang](Entity-Boomerang)", aliases["Boomerang.md"])

    def test_current_registry_preserves_every_compatibility_page(self) -> None:
        language = wiki.read_lang()
        blocks = wiki.parse_blocks(language)
        items = wiki.parse_items(language)
        entities = wiki.parse_entities()
        details = wiki.load_feature_details(blocks, items, entities)
        targets = wiki.build_feature_page_targets(blocks, items, entities)
        pages, evidence = wiki.render_feature_pages(
            blocks, items, entities, details, targets
        )

        for label, entries in (
            ("Block", blocks),
            ("Item", items),
            ("Entity", entities),
        ):
            for name in {str(entry["name"]) for entry in entries}:
                canonical = targets[(label, name)]
                compatibility = wiki.feature_page_name(label, name)
                self.assertIn(f"{canonical}.md", pages)
                self.assertIn(f"{compatibility}.md", pages)
                self.assertEqual(
                    pages[f"{canonical}.md"], pages[f"{compatibility}.md"]
                )
                self.assertEqual(evidence[canonical], evidence[compatibility])

        folded_names = [name.casefold() for name in pages]
        self.assertEqual(len(folded_names), len(set(folded_names)))

    def test_registry_render_manifest_validates_assets_and_embeds_image(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            registry = Path(temporary) / "registry"
            block_path = registry / "blocks/lockedDoor.png"
            item_path = registry / "items/boomerang.png"
            write_rgba_png(block_path)
            write_rgba_png(item_path)
            manifest = {
                "format": 1,
                "width": 2,
                "height": 2,
                "contains_community_maps": False,
                "blocks": [
                    {
                        "field": "lockedDoor",
                        "id": 150,
                        "file": "blocks/lockedDoor.png",
                        "sha256": hashlib.sha256(block_path.read_bytes()).hexdigest(),
                        "alt": "Native isometric render of the Locked Door block",
                    }
                ],
                "items": [
                    {
                        "field": "boomerang",
                        "id": 456,
                        "file": "items/boomerang.png",
                        "sha256": hashlib.sha256(item_path.read_bytes()).hexdigest(),
                        "alt": "Native inventory render of the Boomerang item",
                    }
                ],
            }
            manifest_path = registry / "render-manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")

            renders = wiki.load_registry_render_manifest(
                self.blocks, self.items, manifest_path
            )
            targets = wiki.build_feature_page_targets(
                self.blocks, self.items, self.entities
            )
            pages, evidence = wiki.render_feature_pages(
                self.blocks,
                self.items,
                self.entities,
                self.details,
                targets,
                renders,
            )

            page = pages["Locked-Door.md"]
            self.assertIn(
                "![Native isometric render of the Locked Door block]"
                "(assets/registry/blocks/lockedDoor.png)",
                page,
            )
            self.assertIn("wiki/assets/registry/render-manifest.json", evidence["Locked-Door"])
            self.assertIn("wiki/assets/registry/blocks/lockedDoor.png", evidence["Locked-Door"])

    def test_grouped_registry_fields_render_a_variant_gallery(self) -> None:
        blocks = [
            self.blocks[0],
            {
                "name": "Locked Door",
                "field": "lockedDoor2",
                "id": 151,
                "class": "AC_BlockLockedDoor",
                "line": 11,
            },
        ]
        details = {
            **self.details,
            "blocks": {
                **self.details["blocks"],
                "lockedDoor2": self.details["blocks"]["lockedDoor"],
            },
        }
        renders = {
            "blocks": {
                "lockedDoor": {
                    "file": "blocks/lockedDoor.png",
                    "alt": "Locked Door block variant one",
                },
                "lockedDoor2": {
                    "file": "blocks/lockedDoor2.png",
                    "alt": "Locked Door block variant two",
                },
            },
            "items": {
                "boomerang": {
                    "file": "items/boomerang.png",
                    "alt": "Boomerang item",
                }
            },
        }
        targets = wiki.build_feature_page_targets(blocks, self.items, self.entities)
        pages, _ = wiki.render_feature_pages(
            blocks, self.items, self.entities, details, targets, renders
        )

        page = pages["Locked-Door.md"]
        self.assertIn("## Registry render gallery", page)
        self.assertIn("`lockedDoor`", page)
        self.assertIn("assets/registry/blocks/lockedDoor2.png", page)

    def test_missing_manifest_is_allowed_but_present_manifest_must_be_complete(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            manifest_path = Path(temporary) / "registry/render-manifest.json"
            self.assertIsNone(
                wiki.load_registry_render_manifest(self.blocks, self.items, manifest_path)
            )
            manifest_path.parent.mkdir(parents=True)
            manifest_path.write_text(
                json.dumps(
                    {
                        "format": 1,
                        "width": 128,
                        "height": 128,
                        "contains_community_maps": False,
                        "blocks": [],
                        "items": [],
                    }
                ),
                encoding="utf-8",
            )
            with self.assertRaisesRegex(wiki.WikiError, "coverage mismatch"):
                wiki.load_registry_render_manifest(self.blocks, self.items, manifest_path)

    def test_registry_manifest_explicitly_rejects_community_maps(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            manifest_path = Path(temporary) / "registry/render-manifest.json"
            manifest_path.parent.mkdir(parents=True)
            manifest_path.write_text(
                json.dumps(
                    {
                        "format": 1,
                        "width": 128,
                        "height": 128,
                        "contains_community_maps": True,
                        "blocks": [],
                        "items": [],
                    }
                ),
                encoding="utf-8",
            )
            with self.assertRaisesRegex(wiki.WikiError, "exclude community maps"):
                wiki.load_registry_render_manifest(self.blocks, self.items, manifest_path)

    def test_current_registry_render_contract_covers_70_blocks_and_35_items(self) -> None:
        language = wiki.read_lang()
        self.assertEqual(70, len(wiki.parse_blocks(language)))
        items = wiki.parse_items(language)
        self.assertEqual(35, len(items))
        boomerang = next(entry for entry in items if entry["field"] == "boomerang")
        self.assertEqual(200, boomerang["constructor_id"])
        self.assertEqual(456, boomerang["id"])
        current_items = wiki.render_items(
            items,
            wiki.build_feature_page_targets([], items, []),
        )
        self.assertIn("runtime `Item.id` values", current_items)


if __name__ == "__main__":
    unittest.main()
