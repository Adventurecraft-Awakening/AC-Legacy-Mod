#!/usr/bin/env python3
"""Dependency-free quality and external-link checks for the generated wiki."""

from __future__ import annotations

import argparse
import concurrent.futures
import hashlib
import json
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter, defaultdict
from pathlib import Path, PurePosixPath


IMAGE_SUFFIXES = {".avif", ".gif", ".jpeg", ".jpg", ".png", ".svg", ".webp"}
MISSPELLINGS = {
    "adress": "address",
    "begining": "beginning",
    "commnad": "command",
    "dependancy": "dependency",
    "enviroment": "environment",
    "occurence": "occurrence",
    "recieve": "receive",
    "seperate": "separate",
    "teh": "the",
    "wether": "whether",
}
MARKDOWN_IMAGE_RE = re.compile(r"!\[([^\]]*)\]\(([^)]+)\)")
HTML_IMAGE_RE = re.compile(r"<img\b([^>]*)>", re.IGNORECASE)
WIKI_LINK_RE = re.compile(r"\[\[([^\]]+)\]\]")
URL_RE = re.compile(r"https?://[^\s<>\"\]]+")
TOKEN_RE = re.compile(r"\{\{[A-Z0-9_]+\}\}")


class QualityError(RuntimeError):
    pass


def markdown_files(root: Path) -> list[Path]:
    if root.is_file():
        return [root]
    return sorted(path for path in root.rglob("*.md") if path.is_file())


def read_utf8(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError as error:
        raise QualityError(f"{path}: is not valid UTF-8 ({error})") from error


def display_path(path: Path, root: Path) -> str:
    try:
        return path.relative_to(root).as_posix()
    except ValueError:
        return path.as_posix()


def strip_prose_markup(line: str) -> str:
    """Remove code and link destinations before lightweight prose checks."""
    line = re.sub(r"`+[^`]*`+", " ", line)
    line = re.sub(r"!?(\[[^\]]*\])\([^)]*\)", r"\1", line)
    line = re.sub(r"https?://\S+", " ", line)
    return re.sub(r"[*_~>#|]", " ", line)


def check_markdown(root: Path, *, allow_tokens: bool, spellcheck: bool) -> list[str]:
    issues: list[str] = []
    files = markdown_files(root)
    if not files:
        return [f"{root}: contains no Markdown files"]

    for path in files:
        rel = display_path(path, root)
        raw = path.read_bytes()
        try:
            text = raw.decode("utf-8")
        except UnicodeDecodeError as error:
            issues.append(f"{rel}: invalid UTF-8 ({error})")
            continue
        if b"\r" in raw:
            issues.append(f"{rel}: use LF, not CRLF, line endings")
        if raw and not raw.endswith(b"\n"):
            issues.append(f"{rel}: missing final newline")
        if "\x00" in text:
            issues.append(f"{rel}: contains a NUL byte")
        if not allow_tokens:
            for token in sorted(set(TOKEN_RE.findall(text))):
                issues.append(f"{rel}: unresolved template token {token}")
        for marker in ("<<<<<<<", "=======", ">>>>>>>"):
            if any(line.startswith(marker) for line in text.splitlines()):
                issues.append(f"{rel}: contains a merge-conflict marker")
                break

        in_fence = False
        fence_char = ""
        previous_heading = 0
        h1_count = 0
        previous_word = ""
        previous_line = 0
        for number, line in enumerate(text.splitlines(), 1):
            if line.rstrip(" \t") != line:
                issues.append(f"{rel}:{number}: trailing whitespace")
            if "\t" in line:
                issues.append(f"{rel}:{number}: tab character; use spaces")

            fence = re.match(r"^\s*(`{3,}|~{3,})", line)
            if fence:
                current = fence.group(1)[0]
                if not in_fence:
                    in_fence = True
                    fence_char = current
                elif current == fence_char:
                    in_fence = False
                    fence_char = ""
                previous_word = ""
                continue
            if in_fence:
                continue

            heading = re.match(r"^(#{1,6})\s+\S", line)
            if heading:
                level = len(heading.group(1))
                if level == 1:
                    h1_count += 1
                if previous_heading and level > previous_heading + 1:
                    issues.append(
                        f"{rel}:{number}: heading jumps from H{previous_heading} to H{level}"
                    )
                previous_heading = level

            if spellcheck:
                prose = strip_prose_markup(TOKEN_RE.sub(" ", line) if allow_tokens else line)
                words = re.findall(r"[A-Za-z]+(?:['’-][A-Za-z]+)*", prose)
                for word in words:
                    folded = word.casefold()
                    if folded in MISSPELLINGS:
                        issues.append(
                            f"{rel}:{number}: possible misspelling {word!r}; "
                            f"use {MISSPELLINGS[folded]!r}"
                        )
                    if (
                        folded == previous_word
                        and previous_line == number
                        and folded not in {"had", "that"}
                    ):
                        issues.append(f"{rel}:{number}: repeated word {word!r}")
                    previous_word = folded
                    previous_line = number
            else:
                previous_word = ""

        if in_fence:
            issues.append(f"{rel}: unclosed fenced code block")
        if h1_count > 1:
            issues.append(f"{rel}: contains {h1_count} H1 headings; expected at most one")
        if path.name == "Home.md" and h1_count != 1:
            issues.append(f"{rel}: Home.md must contain exactly one H1 heading")
    return issues


def split_destination(destination: str) -> str:
    value = destination.strip()
    if value.startswith("<") and ">" in value:
        return value[1 : value.index(">")]
    # Markdown permits an optional quoted title after the destination.
    return re.split(r"\s+[\"']", value, maxsplit=1)[0]


def local_media_path(page: Path, root: Path, destination: str) -> Path | None:
    parsed = urllib.parse.urlsplit(destination)
    if parsed.scheme or destination.startswith("//"):
        return None
    path_text = urllib.parse.unquote(parsed.path)
    if not path_text or path_text.startswith("/"):
        return None
    candidate = (page.parent / path_text).resolve()
    try:
        candidate.relative_to(root.resolve())
    except ValueError as error:
        raise QualityError(f"local media escapes the wiki root: {destination}") from error
    return candidate


def check_media(root: Path, *, verify_local: bool = True) -> list[str]:
    issues: list[str] = []
    for path in markdown_files(root):
        rel = display_path(path, root)
        text = read_utf8(path)
        for match in MARKDOWN_IMAGE_RE.finditer(text):
            line = text.count("\n", 0, match.start()) + 1
            alt = re.sub(r"\s+", " ", match.group(1)).strip()
            destination = split_destination(match.group(2))
            if not alt or alt.casefold() in {"image", "photo", "picture", "screenshot"}:
                issues.append(f"{rel}:{line}: image needs specific, non-empty alt text")
            if destination.startswith("data:"):
                issues.append(f"{rel}:{line}: embedded data-URI images are not allowed")
                continue
            try:
                local = local_media_path(path, root, destination)
            except QualityError as error:
                issues.append(f"{rel}:{line}: {error}")
                continue
            if local is not None and verify_local:
                if not local.is_file():
                    issues.append(f"{rel}:{line}: missing local image {destination!r}")
                elif local.suffix.casefold() not in IMAGE_SUFFIXES:
                    issues.append(f"{rel}:{line}: unsupported image type {local.suffix!r}")

        for match in HTML_IMAGE_RE.finditer(text):
            line = text.count("\n", 0, match.start()) + 1
            attributes = match.group(1)
            alt = re.search(r"\balt\s*=\s*([\"'])(.*?)\1", attributes, re.IGNORECASE)
            if alt is None or not alt.group(2).strip():
                issues.append(f"{rel}:{line}: HTML image needs non-empty alt text")

    for path in root.rglob("*"):
        if path.is_symlink():
            issues.append(f"{display_path(path, root)}: symlinks are not allowed")
        elif path.is_file() and path.suffix.casefold() in IMAGE_SUFFIXES and path.stat().st_size > 10 * 1024 * 1024:
            issues.append(f"{display_path(path, root)}: image exceeds the 10 MiB limit")
    return issues


def safe_manifest_path(root: Path, value: object) -> Path:
    if not isinstance(value, str) or not value:
        raise QualityError("manifest file names must be non-empty strings")
    pure = PurePosixPath(value)
    if pure.is_absolute() or ".." in pure.parts or "\\" in value:
        raise QualityError(f"unsafe manifest path: {value!r}")
    result = root.joinpath(*pure.parts)
    try:
        result.resolve().relative_to(root.resolve())
    except ValueError as error:
        raise QualityError(f"manifest path escapes output: {value!r}") from error
    return result


def check_manifest(root: Path, minimum_pages: int) -> list[str]:
    issues: list[str] = []
    manifest_path = root / "wiki-manifest.json"
    try:
        manifest = json.loads(read_utf8(manifest_path))
    except (FileNotFoundError, json.JSONDecodeError, QualityError) as error:
        return [f"wiki-manifest.json: cannot read manifest ({error})"]
    if manifest.get("format") != 1:
        issues.append("wiki-manifest.json: unsupported or missing format; expected 1")

    pages = manifest.get("pages")
    assets = manifest.get("assets", [])
    if not isinstance(pages, list) or not isinstance(assets, list):
        return issues + ["wiki-manifest.json: pages and assets must be arrays"]
    if len(pages) < minimum_pages:
        issues.append(
            f"wiki-manifest.json: generated {len(pages)} pages; minimum is {minimum_pages}"
        )

    declared: dict[str, str] = {}
    page_names: list[str] = []
    for kind, entries in (("page", pages), ("asset", assets)):
        for index, entry in enumerate(entries):
            if not isinstance(entry, dict):
                issues.append(f"wiki-manifest.json: {kind} entry {index} must be an object")
                continue
            try:
                path = safe_manifest_path(root, entry.get("file"))
            except QualityError as error:
                issues.append(f"wiki-manifest.json: {error}")
                continue
            relative = path.relative_to(root).as_posix()
            if relative in declared:
                issues.append(f"wiki-manifest.json: duplicate file {relative!r}")
                continue
            digest = entry.get("sha256")
            if not isinstance(digest, str) or not re.fullmatch(r"[0-9a-f]{64}", digest):
                issues.append(f"wiki-manifest.json: invalid SHA-256 for {relative}")
                continue
            declared[relative] = digest
            if kind == "page":
                page_names.append(relative)
                if path.parent != root or path.suffix != ".md":
                    issues.append(f"wiki-manifest.json: page must be a top-level .md file: {relative}")
            elif path.suffix.casefold() not in IMAGE_SUFFIXES:
                issues.append(f"wiki-manifest.json: unsupported asset type: {relative}")

    folded = [name.casefold() for name in declared]
    if len(folded) != len(set(folded)):
        issues.append("wiki-manifest.json: case-insensitive file-name collision")
    required = {"Home.md", "_Footer.md", "_Sidebar.md"}
    for name in sorted(required - set(page_names)):
        issues.append(f"wiki-manifest.json: missing required page {name}")

    actual = {
        path.relative_to(root).as_posix()
        for path in root.rglob("*")
        if path.is_file() and path.name != "wiki-manifest.json"
    }
    for extra in sorted(actual - set(declared)):
        issues.append(f"wiki-manifest.json: undeclared output file {extra}")
    for missing in sorted(set(declared) - actual):
        issues.append(f"wiki-manifest.json: declared file is missing: {missing}")
    for relative in sorted(actual & set(declared)):
        digest = hashlib.sha256((root / relative).read_bytes()).hexdigest()
        if digest != declared[relative]:
            issues.append(f"wiki-manifest.json: checksum mismatch for {relative}")
    return issues


def check_wiki_links(root: Path) -> list[str]:
    issues: list[str] = []
    names = {path.stem for path in root.glob("*.md")}
    normalized = {name.casefold().replace(" ", "-") for name in names}
    for path in markdown_files(root):
        text = read_utf8(path)
        for match in WIKI_LINK_RE.finditer(text):
            raw = match.group(1)
            parts = raw.split("|", 1)
            # Gollum commonly uses label|target, while older imported content
            # may use target|label. Accept the link when either unambiguous side
            # names a generated page; the generator's fact check owns style.
            targets = {part.split("#", 1)[0].strip() for part in (parts[-1], parts[0])}
            targets.discard("")
            if not targets:
                continue
            keys = {target.casefold().replace(" ", "-") for target in targets}
            if keys.isdisjoint(normalized):
                line = text.count("\n", 0, match.start()) + 1
                issues.append(f"{display_path(path, root)}:{line}: broken wiki link [[{raw}]]")
    return issues


def print_issues(issues: list[str]) -> None:
    if issues:
        raise QualityError("wiki quality checks failed:\n  - " + "\n  - ".join(issues))


def extract_urls(root: Path) -> dict[str, list[str]]:
    found: dict[str, list[str]] = defaultdict(list)
    for path in markdown_files(root):
        text = read_utf8(path)
        for number, line in enumerate(text.splitlines(), 1):
            for match in URL_RE.finditer(line):
                url = match.group(0).rstrip(".,;:)`")
                # Fragments do not affect whether the remote resource exists.
                parsed = urllib.parse.urlsplit(url)
                clean = urllib.parse.urlunsplit((parsed.scheme, parsed.netloc, parsed.path, parsed.query, ""))
                location = f"{display_path(path, root)}:{number}"
                if location not in found[clean]:
                    found[clean].append(location)
    return dict(found)


def request_url(url: str, method: str) -> int:
    headers = {
        "Accept": "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.1",
        "User-Agent": "AC-Legacy-Wiki-Link-Check/1.0 (+https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod)",
    }
    if method == "GET":
        headers["Range"] = "bytes=0-0"
    request = urllib.request.Request(url, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=20) as response:
        if method == "GET":
            response.read(1)
        return response.status


def check_url(url: str) -> dict[str, object]:
    last_error = ""
    status: int | None = None
    for method in ("HEAD", "GET"):
        try:
            status = request_url(url, method)
            return {"url": url, "state": "ok", "status": status, "detail": ""}
        except urllib.error.HTTPError as error:
            status = error.code
            last_error = str(error.reason)
            error.close()
        except (urllib.error.URLError, TimeoutError, OSError) as error:
            last_error = str(getattr(error, "reason", error))
            if method == "GET":
                return {"url": url, "state": "unverified", "status": None, "detail": last_error}

    if status in {404, 410}:
        state = "broken"
    elif status in {401, 403, 429}:
        state = "restricted"
    else:
        state = "unverified"
    return {"url": url, "state": state, "status": status, "detail": last_error}


def write_link_report(root: Path, report_path: Path, workers: int) -> int:
    locations = extract_urls(root)
    with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as executor:
        results = list(executor.map(check_url, sorted(locations)))
    for result in results:
        result["locations"] = locations[str(result["url"])]
    counts = Counter(str(result["state"]) for result in results)
    payload = {
        "format": 1,
        "summary": {state: counts.get(state, 0) for state in ("ok", "broken", "restricted", "unverified")},
        "links": results,
    }
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8", newline="\n")

    markdown_path = report_path.with_suffix(".md")
    lines = [
        "# External wiki link report",
        "",
        f"Checked **{len(results)}** unique external links: "
        f"{counts.get('ok', 0)} reachable, {counts.get('broken', 0)} broken, "
        f"{counts.get('restricted', 0)} access-restricted, and "
        f"{counts.get('unverified', 0)} inconclusive.",
        "",
        "Access-restricted and inconclusive results are reported for review but are not assumed broken.",
    ]
    noteworthy = [result for result in results if result["state"] != "ok"]
    if noteworthy:
        lines.extend(["", "| State | HTTP | URL | First reference |", "|---|---:|---|---|"])
        for result in noteworthy[:100]:
            status = str(result["status"] or "—")
            url = str(result["url"]).replace("|", "%7C")
            location = str(result["locations"][0]).replace("|", "\\|")
            lines.append(f"| {result['state']} | {status} | <{url}> | `{location}` |")
        if len(noteworthy) > 100:
            lines.extend(["", f"The JSON artifact contains {len(noteworthy) - 100} additional results."])
    markdown_path.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    print(lines[2])
    return counts.get("broken", 0)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)

    sources = commands.add_parser("sources", help="check authored Markdown and media")
    sources.add_argument("root", type=Path)

    quality = commands.add_parser("quality", help="check generated Markdown, media, links, and manifest")
    quality.add_argument("root", type=Path)
    quality.add_argument("--minimum-pages", type=int, default=12)

    links = commands.add_parser("links", help="audit external links and write JSON/Markdown reports")
    links.add_argument("root", type=Path)
    links.add_argument("--report", type=Path, required=True)
    links.add_argument("--workers", type=int, default=8)
    links.add_argument("--soft-fail", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        if args.command == "sources":
            issues = check_markdown(args.root, allow_tokens=True, spellcheck=True)
            # Local media destinations are paths in generated output; the source
            # generator may copy them from elsewhere in the repository. The
            # generated-output check below verifies that each one exists.
            issues.extend(check_media(args.root, verify_local=False))
            print_issues(issues)
            print(f"Validated {len(markdown_files(args.root))} authored Markdown pages.")
        elif args.command == "quality":
            if args.minimum_pages < 1:
                raise QualityError("--minimum-pages must be a positive integer")
            issues = check_manifest(args.root, args.minimum_pages)
            issues.extend(check_markdown(args.root, allow_tokens=False, spellcheck=False))
            issues.extend(check_media(args.root))
            issues.extend(check_wiki_links(args.root))
            print_issues(issues)
            print(f"Validated generated wiki quality in {args.root.resolve()}.")
        else:
            if not 1 <= args.workers <= 32:
                raise QualityError("--workers must be between 1 and 32")
            broken = write_link_report(args.root, args.report, args.workers)
            if broken and not args.soft_fail:
                raise QualityError(f"found {broken} broken external link(s)")
    except QualityError as error:
        print(error, file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
