from __future__ import annotations

import sys
import unittest
from pathlib import Path


TOOLS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS_DIR))

import wiki  # noqa: E402


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
                "id": 200,
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


if __name__ == "__main__":
    unittest.main()
