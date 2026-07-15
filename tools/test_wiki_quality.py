import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from tools.wiki_quality import (
    check_manifest,
    check_markdown,
    check_media,
    check_wiki_links,
    extract_urls,
)


class WikiQualityTests(unittest.TestCase):
    def test_manifest_accepts_complete_output(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            pages = []
            for name in ("Home.md", "_Sidebar.md", "_Footer.md"):
                content = b"# Home\n" if name == "Home.md" else b"navigation\n"
                (root / name).write_bytes(content)
                pages.append({"file": name, "sha256": hashlib.sha256(content).hexdigest()})
            asset = root / "assets" / "logo.png"
            asset.parent.mkdir()
            asset.write_bytes(b"not-a-real-png")
            (root / "wiki-manifest.json").write_text(
                json.dumps(
                    {
                        "format": 1,
                        "pages": pages,
                        "assets": [
                            {"file": "assets/logo.png", "sha256": hashlib.sha256(asset.read_bytes()).hexdigest()}
                        ],
                    }
                )
                + "\n",
                encoding="utf-8",
            )
            self.assertEqual([], check_manifest(root, 3))

    def test_manifest_rejects_undeclared_output(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "extra.txt").write_text("surprise\n", encoding="utf-8")
            (root / "wiki-manifest.json").write_text(
                json.dumps({"format": 1, "pages": []}) + "\n", encoding="utf-8"
            )
            issues = check_manifest(root, 1)
            self.assertTrue(any("undeclared output file" in issue for issue in issues))

    def test_markdown_finds_hygiene_and_spelling_errors(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Home.md").write_bytes(b"# Home\r\n\r\nTeh teh text. \r\n")
            issues = check_markdown(root, allow_tokens=False, spellcheck=True)
            self.assertTrue(any("CRLF" in issue for issue in issues))
            self.assertTrue(any("misspelling" in issue for issue in issues))
            self.assertTrue(any("trailing whitespace" in issue for issue in issues))

    def test_markdown_rejects_piped_wiki_links_in_tables(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Current-Commands.md").write_text(
                "# Current Commands\n\n| Syntax | Behavior |\n| --- | --- |\n"
                "| [[Command config|`/config`]] | Opens config. |\n",
                encoding="utf-8",
            )
            issues = check_markdown(root, allow_tokens=False, spellcheck=False)
            self.assertTrue(
                any("piped wiki link breaks Markdown table" in issue for issue in issues)
            )

    def test_markdown_allows_piped_wiki_link_example_in_fence(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Example.md").write_text(
                "# Example\n\n```markdown\n| [[target|label]] |\n```\n",
                encoding="utf-8",
            )
            issues = check_markdown(root, allow_tokens=False, spellcheck=False)
            self.assertFalse(any("piped wiki link" in issue for issue in issues))

    def test_wiki_links_validate_relative_markdown_destinations(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Home.md").write_text(
                "# Home\n\n[Works](Existing-Page) [Broken](Missing-Page)\n",
                encoding="utf-8",
            )
            (root / "Existing-Page.md").write_text("# Existing\n", encoding="utf-8")
            issues = check_wiki_links(root)
            self.assertFalse(any("Existing-Page" in issue for issue in issues))
            self.assertTrue(any("Missing-Page" in issue for issue in issues))

    def test_media_requires_alt_text_and_existing_file(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Home.md").write_text("# Home\n\n![](missing.png)\n", encoding="utf-8")
            issues = check_media(root)
            self.assertTrue(any("alt text" in issue for issue in issues))
            self.assertTrue(any("missing local image" in issue for issue in issues))

    def test_external_urls_are_deduplicated_without_fragments(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "Home.md").write_text(
                "# Home\n\n[one](https://example.test/page#a) "
                "[two](https://example.test/page#b)\n",
                encoding="utf-8",
            )
            urls = extract_urls(root)
            self.assertEqual(["https://example.test/page"], list(urls))
            self.assertEqual(1, len(urls["https://example.test/page"]))


if __name__ == "__main__":
    unittest.main()
