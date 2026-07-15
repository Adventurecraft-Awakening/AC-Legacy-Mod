#!/usr/bin/env python3
"""Generate deterministic GitHub Wiki pages for the Java scripting API.

The extractor intentionally uses only the Python standard library.  It is not a
complete Java parser; it is a small, brace-aware scanner for public top-level
types and their directly declared public API.  Keeping it independent of the
game's build makes the generated reference usable in lightweight wiki CI jobs.
"""

from __future__ import annotations

import argparse
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Sequence


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SOURCE_DIR = ROOT / "src/main/java/dev/adventurecraft/awakening/script"
DEFAULT_SOURCE_BASE_URL = (
    "https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/"
)

TYPE_PATTERN = re.compile(
    r"\bpublic\s+"
    r"(?P<modifiers>(?:(?:abstract|final|sealed|non-sealed|static|strictfp)\s+)*)"
    r"(?P<kind>class|interface|record|enum)\s+"
    r"(?P<name>[A-Za-z_$][\w$]*)\b"
)
ANNOTATION_PATTERN = re.compile(
    r"@[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*(?:\s*\([^)]*\))?\s*",
    re.DOTALL,
)


class ScriptApiError(RuntimeError):
    """Raised when the scripting source cannot be parsed safely."""


@dataclass(frozen=True)
class ApiMember:
    category: str
    name: str
    signature: str
    line: int
    deprecated: bool = False


@dataclass(frozen=True)
class ApiType:
    name: str
    kind: str
    signature: str
    line: int
    source_path: str
    extends: tuple[str, ...]
    implements: tuple[str, ...]
    members: tuple[ApiMember, ...]


def mask_java(source: str) -> str:
    """Mask comments and literals while preserving offsets and newlines."""

    chars = list(source)
    index = 0
    length = len(chars)
    while index < length:
        if source.startswith("//", index):
            end = source.find("\n", index + 2)
            if end == -1:
                end = length
            for pos in range(index, end):
                chars[pos] = " "
            index = end
            continue
        if source.startswith("/*", index):
            end = source.find("*/", index + 2)
            end = length if end == -1 else end + 2
            for pos in range(index, end):
                if chars[pos] not in "\r\n":
                    chars[pos] = " "
            index = end
            continue
        if source.startswith('"""', index):
            end = source.find('"""', index + 3)
            end = length if end == -1 else end + 3
            for pos in range(index, end):
                if chars[pos] not in "\r\n":
                    chars[pos] = " "
            index = end
            continue
        if source[index] in {'"', "'"}:
            quote = source[index]
            pos = index + 1
            while pos < length:
                if source[pos] == "\\":
                    pos += 2
                    continue
                if source[pos] == quote:
                    pos += 1
                    break
                pos += 1
            for masked_pos in range(index, min(pos, length)):
                if chars[masked_pos] not in "\r\n":
                    chars[masked_pos] = " "
            index = pos
            continue
        index += 1
    return "".join(chars)


def normalize_signature(value: str) -> str:
    """Collapse source formatting without otherwise rewriting Java syntax."""

    return re.sub(r"\s+", " ", value).strip().rstrip(";").strip()


def find_matching_brace(masked: str, opening: int) -> int:
    depth = 0
    for index in range(opening, len(masked)):
        char = masked[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return index
    raise ScriptApiError(f"unclosed Java block at offset {opening}")


def split_type_list(value: str) -> tuple[str, ...]:
    """Split a comma-separated inheritance list without splitting generics."""

    result: list[str] = []
    start = 0
    depth = 0
    for index, char in enumerate(value):
        if char == "<":
            depth += 1
        elif char == ">":
            depth = max(0, depth - 1)
        elif char == "," and depth == 0:
            item = normalize_signature(value[start:index])
            if item:
                result.append(item)
            start = index + 1
    item = normalize_signature(value[start:])
    if item:
        result.append(item)
    return tuple(result)


def parse_inheritance(header: str, kind: str) -> tuple[tuple[str, ...], tuple[str, ...]]:
    extends: tuple[str, ...] = ()
    implements: tuple[str, ...] = ()
    extends_match = re.search(r"\bextends\s+(.+?)(?=\bimplements\b|$)", header)
    if extends_match:
        extends = split_type_list(extends_match.group(1))
    implements_match = re.search(r"\bimplements\s+(.+)$", header)
    if implements_match:
        implements = split_type_list(implements_match.group(1))
    if kind == "record" and extends:
        # A record component named "extends" is legal text but not inheritance.
        extends = ()
    return extends, implements


def strip_leading_annotations(raw_header: str) -> tuple[str, int, bool]:
    """Return the declaration, its offset in ``raw_header``, and deprecation."""

    offset = len(raw_header) - len(raw_header.lstrip())
    deprecated = False
    while True:
        match = ANNOTATION_PATTERN.match(raw_header, offset)
        if not match:
            break
        annotation = raw_header[offset : match.end()]
        deprecated = deprecated or bool(re.match(r"@(?:[\w$]+\.)*Deprecated\b", annotation))
        offset = match.end()
        while offset < len(raw_header) and raw_header[offset].isspace():
            offset += 1
    return raw_header[offset:].strip(), offset, deprecated


def parameter_text(declaration: str) -> str:
    opening = declaration.find("(")
    if opening == -1:
        return ""
    depth = 0
    for index in range(opening, len(declaration)):
        if declaration[index] == "(":
            depth += 1
        elif declaration[index] == ")":
            depth -= 1
            if depth == 0:
                return declaration[opening + 1 : index].strip()
    return declaration[opening + 1 :].strip()


def classify_member(
    raw_header: str,
    *,
    owner_name: str,
    owner_kind: str,
    source: str,
    absolute_offset: int,
) -> ApiMember | None:
    declaration, declaration_offset, deprecated = strip_leading_annotations(raw_header)
    declaration = normalize_signature(declaration)
    if not declaration:
        return None
    if re.search(r"\b(class|interface|record|enum)\b", declaration.split("(", 1)[0]):
        return None

    explicitly_public = bool(re.match(r"public\b", declaration))
    implicitly_public = owner_kind == "interface" and not re.match(
        r"(?:private|protected)\b", declaration
    )
    if not (explicitly_public or implicitly_public):
        return None
    if implicitly_public and not explicitly_public:
        declaration = f"public {declaration}"

    # Initializer calls do not turn fields into methods.
    declarator = declaration.split("=", 1)[0].strip()
    opening = declarator.find("(")
    if opening != -1:
        name_match = re.search(r"([A-Za-z_$][\w$]*)\s*$", declarator[:opening])
        if not name_match:
            return None
        name = name_match.group(1)
        if name == owner_name:
            category = "constructors"
        elif name.startswith("set"):
            category = "setters"
        elif (name.startswith("get") or name.startswith("is")) and not parameter_text(declaration):
            category = "getters"
        else:
            category = "methods"
        signature = declaration
    else:
        name_match = re.search(r"([A-Za-z_$][\w$]*)\s*(?:\[\s*\])?\s*$", declarator)
        if not name_match:
            return None
        name = name_match.group(1)
        category = "fields"
        signature = declarator + ";"

    declaration_start = absolute_offset + declaration_offset
    line = source.count("\n", 0, declaration_start) + 1
    return ApiMember(category, name, signature, line, deprecated)


def record_members(type_name: str, header: str, line: int) -> list[ApiMember]:
    """Expose the canonical constructor and accessors declared by a record."""

    name_index = header.find(type_name)
    opening = header.find("(", name_index + len(type_name))
    closing = header.rfind(")")
    if opening == -1 or closing < opening:
        return []
    components = split_type_list(header[opening + 1 : closing])
    if not components:
        return []
    constructor = ApiMember(
        "constructors",
        type_name,
        f"public {type_name}({', '.join(components)})",
        line,
    )
    result = [constructor]
    for component in components:
        match = re.match(r"(.+?)\s+([A-Za-z_$][\w$]*)$", component)
        if match:
            result.append(
                ApiMember("getters", match.group(2), f"public {match.group(1)} {match.group(2)}()", line)
            )
    return result


def scan_members(
    source: str,
    masked: str,
    body_open: int,
    body_close: int,
    owner_name: str,
    owner_kind: str,
) -> tuple[ApiMember, ...]:
    members: list[ApiMember] = []
    index = body_open + 1
    while index < body_close:
        while index < body_close and (masked[index].isspace() or masked[index] == ";"):
            index += 1
        if index >= body_close:
            break
        start = index
        scan = index
        equals_seen = False
        paren_depth = 0
        while scan < body_close:
            char = masked[scan]
            if char == "(":
                paren_depth += 1
            elif char == ")":
                paren_depth = max(0, paren_depth - 1)
            elif char == "=" and paren_depth == 0:
                equals_seen = True
            elif char == ";" and paren_depth == 0:
                header = masked[start:scan]
                member = classify_member(
                    header,
                    owner_name=owner_name,
                    owner_kind=owner_kind,
                    source=source,
                    absolute_offset=start,
                )
                if member:
                    members.append(member)
                index = scan + 1
                break
            elif char == "{" and paren_depth == 0:
                closing = find_matching_brace(masked, scan)
                if equals_seen:
                    # Array/anonymous-class/lambda field initializer; its declaration
                    # ends at a following semicolon, so continue scanning there.
                    scan = closing + 1
                    continue
                header = masked[start:scan]
                member = classify_member(
                    header,
                    owner_name=owner_name,
                    owner_kind=owner_kind,
                    source=source,
                    absolute_offset=start,
                )
                if member:
                    members.append(member)
                index = closing + 1
                break
            scan += 1
        else:
            break
    return tuple(members)


def parse_java_source(source: str, source_path: str) -> ApiType:
    masked = mask_java(source)
    type_match = TYPE_PATTERN.search(masked)
    if not type_match:
        raise ScriptApiError(f"no public top-level Java type found in {source_path}")
    body_open = masked.find("{", type_match.end())
    if body_open == -1:
        raise ScriptApiError(f"public type has no body in {source_path}")
    body_close = find_matching_brace(masked, body_open)

    kind = type_match.group("kind")
    name = type_match.group("name")
    header = normalize_signature(masked[type_match.start() : body_open])
    extends, implements = parse_inheritance(header, kind)
    line = source.count("\n", 0, type_match.start()) + 1
    members = list(scan_members(source, masked, body_open, body_close, name, kind))
    if kind == "record":
        members = record_members(name, header, line) + members
    return ApiType(
        name=name,
        kind=kind,
        signature=header,
        line=line,
        source_path=source_path.replace("\\", "/"),
        extends=extends,
        implements=implements,
        members=tuple(members),
    )


def repository_relative_path(path: Path, source_dir: Path) -> str:
    try:
        return path.resolve().relative_to(ROOT.resolve()).as_posix()
    except ValueError:
        return path.relative_to(source_dir).as_posix()


def extract_api(source_dir: Path = DEFAULT_SOURCE_DIR) -> tuple[ApiType, ...]:
    source_dir = Path(source_dir)
    paths = sorted(source_dir.glob("*.java"), key=lambda path: path.name.casefold())
    if not paths:
        raise ScriptApiError(f"no Java sources found in {source_dir}")
    types = [
        parse_java_source(
            path.read_text(encoding="utf-8"),
            repository_relative_path(path, source_dir),
        )
        for path in paths
    ]
    names = [api_type.name for api_type in types]
    if len(names) != len(set(names)):
        raise ScriptApiError("duplicate public scripting type names")
    return tuple(sorted(types, key=lambda api_type: api_type.name.casefold()))


def wiki_page_name(type_name: str) -> str:
    return f"Scripting-API-{type_name}"


def markdown_table_link(target: str, label: str) -> str:
    """Render an internal link without Gollum's table-breaking pipe syntax."""
    escaped = label.replace("|", "\\|")
    return f"[{escaped}]({target})"


def source_url(api_type: ApiType, base_url: str, line: int | None = None) -> str:
    url = f"{base_url.rstrip('/')}/{api_type.source_path}"
    return f"{url}#L{line}" if line else url


def type_reference(value: str, local_names: set[str], *, in_table: bool = False) -> str:
    simple_name_match = re.match(r"(?:[\w$.]+\.)?([A-Za-z_$][\w$]*)", value)
    if simple_name_match and simple_name_match.group(1) in local_names:
        name = simple_name_match.group(1)
        if in_table:
            return markdown_table_link(wiki_page_name(name), f"`{value}`")
        return f"[[{wiki_page_name(name)}|`{value}`]]"
    return f"`{value}`"


def render_type_page(
    api_type: ApiType,
    all_types: Sequence[ApiType],
    source_base_url: str = DEFAULT_SOURCE_BASE_URL,
) -> str:
    local_names = {item.name for item in all_types}
    type_index = list(all_types).index(api_type)
    lines = [
        f"# Script API: {api_type.name}",
        "",
        "> Generated from the current Java source. Do not edit this page by hand.",
        "",
        "[[Scripting-API|← Script API index]]",
        "",
        f"**Declaration:** `{api_type.signature}`",
        f"**Source:** [{api_type.source_path}]({source_url(api_type, source_base_url, api_type.line)})",
    ]
    if api_type.extends:
        rendered = ", ".join(type_reference(value, local_names) for value in api_type.extends)
        lines.extend([f"**Extends:** {rendered}"])
    if api_type.implements:
        rendered = ", ".join(type_reference(value, local_names) for value in api_type.implements)
        lines.extend([f"**Implements:** {rendered}"])

    headings = (
        ("constructors", "Constructors"),
        ("fields", "Fields"),
        ("getters", "Getters"),
        ("setters", "Setters"),
        ("methods", "Other methods"),
    )
    for category, heading in headings:
        members = [member for member in api_type.members if member.category == category]
        if not members:
            continue
        lines.extend(
            [
                "",
                f"## {heading}",
                "",
                "| Signature | Status | Source |",
                "| --- | --- | --- |",
            ]
        )
        for member in members:
            signature = member.signature.replace("|", "\\|")
            status = "**Deprecated**" if member.deprecated else "Current"
            member_source = source_url(api_type, source_base_url, member.line)
            lines.append(f"| `{signature}` | {status} | [line {member.line}]({member_source}) |")

    if not api_type.members:
        lines.extend(["", "_No directly declared public members._"])

    navigation: list[str] = []
    if type_index > 0:
        previous = all_types[type_index - 1]
        navigation.append(f"[[{wiki_page_name(previous.name)}|← {previous.name}]]")
    navigation.append("[[Scripting-API|API index]]")
    if type_index + 1 < len(all_types):
        following = all_types[type_index + 1]
        navigation.append(f"[[{wiki_page_name(following.name)}|{following.name} →]]")
    lines.extend(["", "---", "", " · ".join(navigation), ""])
    return "\n".join(lines)


def render_index(
    api_types: Sequence[ApiType],
    source_base_url: str = DEFAULT_SOURCE_BASE_URL,
) -> str:
    del source_base_url  # Kept symmetrical with render_type_page for callers.
    lines = [
        "# Scripting API",
        "",
        "> Generated from the current `script` Java package. Signatures marked deprecated remain in source but should not be used in new scripts.",
        "",
        f"This reference contains **{len(api_types)} types**. Members are directly declared by each type; follow the inheritance links for inherited API.",
        "",
        "| Type | Kind | Inheritance | Declared members |",
        "| --- | --- | --- | ---: |",
    ]
    local_names = {item.name for item in api_types}
    for api_type in api_types:
        inheritance_values = api_type.extends + api_type.implements
        inheritance = ", ".join(
            type_reference(value, local_names, in_table=True) for value in inheritance_values
        ) or "—"
        type_link = markdown_table_link(wiki_page_name(api_type.name), api_type.name)
        lines.append(
            f"| {type_link} | {api_type.kind} | "
            f"{inheritance} | {len(api_type.members)} |"
        )
    lines.append("")
    return "\n".join(lines)


def render_reference(
    api_types: Sequence[ApiType],
    source_base_url: str = DEFAULT_SOURCE_BASE_URL,
) -> dict[str, str]:
    ordered = tuple(sorted(api_types, key=lambda api_type: api_type.name.casefold()))
    pages = {"Scripting-API.md": render_index(ordered, source_base_url)}
    for api_type in ordered:
        pages[f"{wiki_page_name(api_type.name)}.md"] = render_type_page(
            api_type, ordered, source_base_url
        )
    return dict(sorted(pages.items()))


def write_reference(
    output_dir: Path,
    source_dir: Path = DEFAULT_SOURCE_DIR,
    source_base_url: str = DEFAULT_SOURCE_BASE_URL,
) -> tuple[Path, ...]:
    """Extract and write the reference, removing only stale owned pages."""

    output_dir = Path(output_dir)
    pages = render_reference(extract_api(Path(source_dir)), source_base_url)
    output_dir.mkdir(parents=True, exist_ok=True)
    expected = set(pages)
    for stale in output_dir.glob("Scripting-API*.md"):
        if stale.name not in expected:
            stale.unlink()
    written: list[Path] = []
    for filename, content in pages.items():
        path = output_dir / filename
        path.write_text(content, encoding="utf-8", newline="\n")
        written.append(path)
    return tuple(written)


def build_argument_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--output",
        "-o",
        type=Path,
        required=True,
        help="directory in which to write GitHub Wiki Markdown pages",
    )
    parser.add_argument(
        "--source-dir",
        type=Path,
        default=DEFAULT_SOURCE_DIR,
        help="directory containing the scripting Java sources",
    )
    parser.add_argument(
        "--source-base-url",
        default=DEFAULT_SOURCE_BASE_URL,
        help="repository blob URL used for source links",
    )
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    args = build_argument_parser().parse_args(argv)
    try:
        written = write_reference(args.output, args.source_dir, args.source_base_url)
    except (OSError, ScriptApiError) as exc:
        raise SystemExit(f"wiki scripting API generation failed: {exc}") from exc
    print(f"generated {len(written)} scripting API pages in {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
