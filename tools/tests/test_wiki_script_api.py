from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path


TOOLS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS_DIR))

import wiki_script_api as api  # noqa: E402


class JavaParserTests(unittest.TestCase):
    def test_extracts_public_surface_and_inheritance(self) -> None:
        source = """
            package example;

            public final class Child extends Parent implements First, Generic<T> {
                public int count = 2;
                private int hidden;

                public Child(int count) { this.count = count; }

                public String getName() { return "not a } // comment"; }

                @Deprecated
                public void setName(
                    String value
                ) { }

                public <T> T convert(T value) throws Exception { return value; }
                protected void ignored() { }
            }
        """
        parsed = api.parse_java_source(source, "example/Child.java")

        self.assertEqual("Child", parsed.name)
        self.assertEqual("class", parsed.kind)
        self.assertEqual(("Parent",), parsed.extends)
        self.assertEqual(("First", "Generic<T>"), parsed.implements)
        self.assertEqual(
            ["count", "Child", "getName", "setName", "convert"],
            [member.name for member in parsed.members],
        )
        self.assertEqual("fields", parsed.members[0].category)
        self.assertEqual("constructors", parsed.members[1].category)
        self.assertEqual("getters", parsed.members[2].category)
        self.assertEqual("setters", parsed.members[3].category)
        self.assertTrue(parsed.members[3].deprecated)
        self.assertEqual("methods", parsed.members[4].category)
        self.assertNotIn("hidden", [member.name for member in parsed.members])
        self.assertNotIn("ignored", [member.name for member in parsed.members])

    def test_interface_members_are_implicitly_public(self) -> None:
        source = """
            public interface Tint extends Color {
                ScriptVec4 getColor();

                @Deprecated
                default void setRed(double value) { }

                private void helper() { }
            }
        """
        parsed = api.parse_java_source(source, "Tint.java")

        self.assertEqual(("Color",), parsed.extends)
        self.assertEqual(["getColor", "setRed"], [member.name for member in parsed.members])
        self.assertEqual("public ScriptVec4 getColor()", parsed.members[0].signature)
        self.assertTrue(parsed.members[1].deprecated)

    def test_record_components_become_constructor_and_accessors(self) -> None:
        parsed = api.parse_java_source(
            "public record Pair(long id, String name) {}", "Pair.java"
        )

        self.assertEqual("record", parsed.kind)
        self.assertEqual(["Pair", "id", "name"], [member.name for member in parsed.members])
        self.assertEqual("constructors", parsed.members[0].category)
        self.assertEqual("getters", parsed.members[1].category)


class RenderingTests(unittest.TestCase):
    def test_writes_deterministic_navigable_reference_and_cleans_stale_pages(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source_dir = root / "source"
            output_dir = root / "output"
            source_dir.mkdir()
            output_dir.mkdir()
            (source_dir / "Beta.java").write_text(
                "public class Beta extends Alpha { public void run() {} }",
                encoding="utf-8",
            )
            (source_dir / "Alpha.java").write_text(
                "public class Alpha { public int value; }", encoding="utf-8"
            )
            stale = output_dir / "Scripting-API-Removed.md"
            stale.write_text("stale", encoding="utf-8")

            first_paths = api.write_reference(
                output_dir,
                source_dir,
                "https://example.invalid/repository/blob/main",
            )
            first = {path.name: path.read_bytes() for path in first_paths}
            second_paths = api.write_reference(
                output_dir,
                source_dir,
                "https://example.invalid/repository/blob/main",
            )
            second = {path.name: path.read_bytes() for path in second_paths}

            self.assertEqual(first, second)
            self.assertFalse(stale.exists())
            self.assertEqual(
                ["Scripting-API-Alpha.md", "Scripting-API-Beta.md", "Scripting-API.md"],
                sorted(first),
            )
            index = first["Scripting-API.md"].decode()
            beta = first["Scripting-API-Beta.md"].decode()
            self.assertLess(
                index.index("[Alpha](Scripting-API-Alpha)"),
                index.index("[Beta](Scripting-API-Beta)"),
            )
            self.assertNotIn("[[", index)
            self.assertIn("[[Scripting-API-Alpha|`Alpha`]]", beta)
            self.assertIn("## Other methods", beta)
            self.assertIn("https://example.invalid/repository/blob/main/Beta.java#L1", beta)
            self.assertIn("[[Scripting-API|API index]]", beta)


if __name__ == "__main__":
    unittest.main()
