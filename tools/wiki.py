#!/usr/bin/env python3
"""Build, validate, and refresh the repository-managed GitHub wiki."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.parse
import urllib.request
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
WIKI_DIR = ROOT / "wiki"
SOURCE_DIR = WIKI_DIR / "pages"
BASELINE_PATH = WIKI_DIR / "fandom-baseline.json"
PAGE_METADATA_PATH = WIKI_DIR / "page-metadata.json"
FEATURE_DETAILS_PATH = WIKI_DIR / "feature-details.json"
DEFAULT_OUTPUT = ROOT / "build" / "wiki"

FANDOM_API = "https://adventurecraftmod.fandom.com/api.php"
FANDOM_WIKI = "https://adventurecraftmod.fandom.com/wiki/"
USER_AGENT = "AC-Legacy-Wiki-Audit/1.0 (+https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod)"

REPLACEMENTS = {
    "AdventureCraft Wiki": "Home",
    "Main Page": "Home",
    "Installing AdventureCraft": "Getting-Started",
    "Gamemodes": "Controls-and-Game-Modes",
    "Commands": "Current-Commands",
    "Version History": "Release-History",
    "Script Guide": "Scripting",
    "Available Scripts": "Scripting",
    "Mobs": "Current-Entities",
    "NPC": "Current-Entities",
}

DISPOSITION_LABELS = {
    "rewritten-current": "Rewritten from current source",
    "inventory-verified": "Current existence verified; legacy behavior not imported",
    "historical-script": "Historical community script; not runtime-verified",
    "historical-reference": "Historical reference; not imported as current documentation",
}

LEGACY_MAP_CATEGORIES = {
    "adventure",
    "horror",
    "puzzle",
    "survival",
    "canceled maps",
    "map download site unavailable",
    "missing map link",
    "unreleased map",
    "timed",
    "fighting",
}

LEGACY_MAP_CATALOG_PAGES = {
    "AC Maps",
    "AdventureMap",
    "Download Maps",
    "Download Maps (Outdated)",
    "Download maps bonus",
    "Downloading and Adding Maps",
    "Maps",
    "Sorted Map List",
}

COMMAND_DOCS = {
    "config": ("/config", "Opens the current world's configuration screen.", "World required."),
    "test": ("/test", "Opens the map-editing HUD.", "This is an internal/test-facing command in the current registry."),
    "scriptstats": ("/scriptstats", "Opens the per-script timing statistics screen.", "World required."),
    "textureatlas": ("/textureatlas", "Opens the texture-atlas inspection screen.", "Client-side inspection command."),
    "day": ("/day", "Sets the current world's time of day to 0.", "The value is source-derived, not inherited from historical documentation."),
    "night": ("/night", "Sets the current world's time of day to 14000.", "Historical Fandom documentation stated 12000; that is not the current implementation."),
    "removemobs": ("/removemobs", "Removes every loaded Mob except the player and reports the count.", "Already-removed entities are counted separately from newly removed entities."),
    "cameraclear": ("/cameraclear", "Clears every point from the camera block currently being edited.", "Fails with an in-game message when no cutscene camera is active."),
    "fullbright": ("/fullbright", "Sets all 16 world brightness-ramp entries to full brightness and rebuilds renderers.", "This command sets brightness; it is not implemented as a reversible boolean toggle."),
    "scriptstatreset": ("/scriptstatreset", "Clears collected timing statistics for all loaded map scripts.", "World and script handler required."),
    "health": ("/health [amount]", "Sets both health and maximum health; the omitted amount defaults to 12.", "The amount must be an integer of at least 1. Player heart-piece progress is reset."),
    "undo": ("/undo [amount]", "Undoes up to the requested number of recorded edit actions.", "The omitted amount defaults to 1."),
    "redo": ("/redo [amount]", "Redoes up to the requested number of undone edit actions.", "The omitted amount defaults to 1."),
    "mapedit": ("/mapedit [true|false]", "Sets or toggles level-editing mode.", "With no value, the current state is inverted."),
    "renderpaths": ("/renderpaths [true|false]", "Sets or toggles entity-path debug rendering.", "With no value, the current state is inverted."),
    "renderfov": ("/renderfov [true|false]", "Sets or toggles field-of-view debug rendering.", "With no value, the current state is inverted."),
    "rendercollisions": ("/rendercollisions [true|false]", "Sets or toggles collision debug rendering.", "With no value, the current state is inverted."),
    "renderrays": ("/renderrays [true|false]", "Sets or toggles raycast debug rendering.", "With no value, the current state is inverted."),
    "fluidcollision": ("/fluidcollision [true|false]", "Sets or toggles whether debug targeting can hit fluids.", "With no value, the current state is inverted."),
    "fly": ("/fly [true|false] or /fly <speed>", "Sets/toggles flying or sets flight speed.", "Flight speed accepts a floating-point value from 0 through 1000."),
    "noclip": ("/noclip [true|false]", "Sets or toggles collision-free movement.", "Enabling noclip also enables flying."),
    "cameraadd": ("/cameraadd <time>", "Adds a quadratic-blend camera point at the executing entity's view position and rotation.", "A camera block must currently be active for editing."),
    "help": ("/help [page|command]", "Lists command help pages or describes a command path.", "Page numbers begin at 1; a command path may include subcommands."),
    "gamerule": ("/gamerule <rule> [value]", "Reads or sets a registered per-world AdventureCraft game rule.", "Omitting the value reports the current rule value."),
    "undostack": ("/undostack [clear]", "Reports undo/redo stack sizes or clears both stacks.", "The `clear` subcommand is destructive to the current edit history."),
}


class WikiError(RuntimeError):
    pass


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except FileNotFoundError as exc:
        raise WikiError(f"required file is missing: {path.relative_to(ROOT)}") from exc


def read_properties() -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in read_text(ROOT / "gradle.properties").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def git_revision() -> str:
    env_revision = __import__("os").environ.get("GITHUB_SHA")
    if env_revision:
        return env_revision
    try:
        return subprocess.check_output(
            ["git", "rev-parse", "HEAD"], cwd=ROOT, text=True, stderr=subprocess.DEVNULL
        ).strip()
    except (OSError, subprocess.CalledProcessError):
        return "unknown"


def load_page_metadata() -> dict[str, dict[str, object]]:
    try:
        data = json.loads(read_text(PAGE_METADATA_PATH))
    except json.JSONDecodeError as exc:
        raise WikiError(f"invalid JSON in {PAGE_METADATA_PATH.relative_to(ROOT)}: {exc}") from exc
    if data.get("format") != 1 or not isinstance(data.get("pages"), dict):
        raise WikiError("wiki/page-metadata.json must use format 1 and contain a pages object")
    source_names = source_page_names()
    metadata_names = set(data["pages"])
    if source_names != metadata_names:
        missing = source_names - metadata_names
        stale = metadata_names - source_names
        details = []
        if missing:
            details.append(f"missing metadata: {', '.join(sorted(missing))}")
        if stale:
            details.append(f"stale metadata: {', '.join(sorted(stale))}")
        raise WikiError("authored wiki metadata mismatch (" + "; ".join(details) + ")")
    for page, metadata in data["pages"].items():
        evidence = metadata.get("evidence")
        if not metadata.get("status") or not isinstance(evidence, list) or not evidence:
            raise WikiError(f"{page} must have a status and non-empty evidence list")
        for relative_path in evidence:
            if not (ROOT / relative_path).is_file():
                raise WikiError(f"{page} evidence does not exist: {relative_path}")
    return data["pages"]


def read_lang() -> dict[str, str]:
    values: dict[str, str] = {}
    path = ROOT / "src/main/resources/assets/adventurecraft/lang/en_US.lang"
    for raw_line in read_text(path).splitlines():
        if not raw_line or raw_line.lstrip().startswith("#") or "=" not in raw_line:
            continue
        key, value = raw_line.split("=", 1)
        values[key] = value
    return values


def source_url(path: str) -> str:
    return f"https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/{path}"


def markdown_escape(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ")


def page_slug(value: str) -> str:
    slug = re.sub(r"[^A-Za-z0-9]+", "-", value).strip("-")
    if not slug:
        raise WikiError(f"cannot create a GitHub wiki slug from {value!r}")
    return slug


def feature_page_name(kind: str, name: str) -> str:
    return f"{kind}-{page_slug(name)}"


def parse_blocks(lang: dict[str, str]) -> list[dict[str, object]]:
    path = ROOT / "src/main/java/dev/adventurecraft/awakening/tile/AC_Blocks.java"
    entries: list[dict[str, object]] = []
    for line_no, line in enumerate(read_text(path).splitlines(), 1):
        if "public static final" not in line or "new AC_Block" not in line:
            continue
        field_match = re.search(r"public static final .*?\s+(\w+)\s*=", line)
        ctor_match = re.search(r"new (AC_Block\w+)\((\d+)", line)
        desc_match = re.search(r'\.setDescriptionId\("([^"]+)"\)', line)
        if not (field_match and ctor_match and desc_match):
            raise WikiError(f"cannot parse block registration at {path.relative_to(ROOT)}:{line_no}")
        description_id = desc_match.group(1)
        entries.append(
            {
                "field": field_match.group(1),
                "class": ctor_match.group(1),
                "id": int(ctor_match.group(2)),
                "description_id": description_id,
                "name": lang.get(f"tile.{description_id}.name", description_id),
                "line": line_no,
            }
        )
    if len(entries) < 60:
        raise WikiError(f"parsed only {len(entries)} AdventureCraft blocks; the source format may have changed")
    return entries


def parse_items(lang: dict[str, str]) -> list[dict[str, object]]:
    path = ROOT / "src/main/java/dev/adventurecraft/awakening/item/AC_Items.java"
    entries: list[dict[str, object]] = []
    for line_no, line in enumerate(read_text(path).splitlines(), 1):
        if "public static" not in line or "new " not in line:
            continue
        field_match = re.search(r"public static .*?\s+(\w+)\s*=", line)
        ctor_match = re.search(r"new (\w+)\((\d+)", line)
        desc_match = re.search(r'\.setDescriptionId\("([^"]+)"\)', line)
        if not (field_match and ctor_match and desc_match):
            raise WikiError(f"cannot parse item registration at {path.relative_to(ROOT)}:{line_no}")
        description_id = desc_match.group(1)
        entries.append(
            {
                "field": field_match.group(1),
                "class": ctor_match.group(1),
                "id": int(ctor_match.group(2)),
                "description_id": description_id,
                "name": lang.get(f"item.{description_id}.name", description_id),
                "line": line_no,
            }
        )
    if len(entries) < 30:
        raise WikiError(f"parsed only {len(entries)} AdventureCraft items; the source format may have changed")
    return entries


def parse_entities() -> list[dict[str, object]]:
    path = ROOT / "src/main/java/dev/adventurecraft/awakening/mixin/entity/MixinEntityRegistry.java"
    entries: list[dict[str, object]] = []
    pattern = re.compile(r'setId\((AC_\w+)\.class,\s*"([^"]+)",\s*(\d+)\);')
    for line_no, line in enumerate(read_text(path).splitlines(), 1):
        match = pattern.search(line)
        if match:
            entries.append({"class": match.group(1), "name": match.group(2), "id": int(match.group(3)), "line": line_no})
    if len(entries) < 10:
        raise WikiError(f"parsed only {len(entries)} AdventureCraft entities; the source format may have changed")
    return entries


def load_feature_details(
    blocks: list[dict[str, object]],
    items: list[dict[str, object]],
    entities: list[dict[str, object]],
) -> dict[str, dict[str, dict[str, object]]]:
    try:
        data = json.loads(read_text(FEATURE_DETAILS_PATH))
    except json.JSONDecodeError as exc:
        raise WikiError(f"invalid JSON in {FEATURE_DETAILS_PATH.relative_to(ROOT)}: {exc}") from exc
    if data.get("schema_version") != 1:
        raise WikiError("wiki/feature-details.json must use schema_version 1")
    expected = {
        "blocks": {str(entry["field"]) for entry in blocks},
        "items": {str(entry["field"]) for entry in items},
        "entities": {str(entry["class"]) for entry in entities},
    }
    required_fields = {"purpose", "behavior", "mode_notes", "interactions", "evidence"}
    result: dict[str, dict[str, dict[str, object]]] = {}
    for kind, keys in expected.items():
        entries = data.get(kind)
        if not isinstance(entries, dict) or set(entries) != keys:
            missing = keys - set(entries or {})
            extra = set(entries or {}) - keys
            raise WikiError(
                f"{kind} feature detail coverage mismatch; missing={sorted(missing)}, extra={sorted(extra)}"
            )
        for key, entry in entries.items():
            if not isinstance(entry, dict) or not required_fields <= set(entry):
                raise WikiError(f"{kind}.{key} is missing required feature-detail fields")
            if not isinstance(entry["interactions"], list) or not isinstance(entry["evidence"], list) or not entry["evidence"]:
                raise WikiError(f"{kind}.{key} interactions/evidence must be lists and evidence must not be empty")
            for relative_path in entry["evidence"]:
                if not (ROOT / relative_path).is_file():
                    raise WikiError(f"{kind}.{key} evidence does not exist: {relative_path}")
        result[kind] = entries
    return result


def render_feature_pages(
    blocks: list[dict[str, object]],
    items: list[dict[str, object]],
    entities: list[dict[str, object]],
    details: dict[str, dict[str, dict[str, object]]],
) -> tuple[dict[str, str], dict[str, list[str]]]:
    pages: dict[str, str] = {}
    evidence_by_page: dict[str, list[str]] = {}

    def render_group(kind: str, label: str, group: list[dict[str, object]], detail_key: str) -> None:
        name = str(group[0]["name"])
        page_name = feature_page_name(label, name)
        lines = [
            f"# {name}",
            "",
            f"> Current {kind} reference generated from the AC-Legacy registry and source-backed feature audit.",
            "",
            "## Registry variants",
            "",
        ]
        if kind == "entity":
            lines.extend(["| Registry name | Numeric ID | Implementation |", "| --- | ---: | --- |"])
            for entry in group:
                lines.append(f"| `{entry['name']}` | {entry['id']} | `{entry['class']}` |")
        else:
            lines.extend(["| Field | Numeric ID | Implementation |", "| --- | ---: | --- |"])
            for entry in group:
                lines.append(f"| `{entry['field']}` | {entry['id']} | `{entry['class']}` |")

        collected_evidence: set[str] = set()
        for entry in group:
            key = str(entry[detail_key])
            detail_collection = {"block": "blocks", "item": "items", "entity": "entities"}[kind]
            detail = details[detail_collection][key]
            heading = key if len(group) > 1 else "Behavior"
            lines.extend(["", f"## {heading}", "", str(detail["purpose"]), "", str(detail["behavior"])])
            if detail["mode_notes"]:
                lines.extend(["", f"**Mode notes:** {detail['mode_notes']}"])
            if detail["interactions"]:
                lines.extend(["", "**Interactions:**", ""])
                lines.extend(f"- {value}" for value in detail["interactions"])
            if detail.get("unknowns"):
                lines.extend(["", "**Not established by current evidence:**", ""])
                lines.extend(f"- {value}" for value in detail["unknowns"])
            collected_evidence.update(str(path) for path in detail["evidence"])

        lines.extend(["", "## Evidence", ""])
        lines.extend(f"- [`{path}`]({source_url(path)})" for path in sorted(collected_evidence))
        index_page = {"block": "Current Blocks", "item": "Current Items", "entity": "Current Entities"}[kind]
        lines.extend(["", f"Back to [[{index_page}]].", ""])
        pages[f"{page_name}.md"] = "\n".join(lines)
        evidence_by_page[page_name] = sorted(collected_evidence)

    for kind, label, entries, detail_key in (
        ("block", "Block", blocks, "field"),
        ("item", "Item", items, "field"),
        ("entity", "Entity", entities, "class"),
    ):
        groups: dict[str, list[dict[str, object]]] = {}
        for entry in entries:
            groups.setdefault(str(entry["name"]), []).append(entry)
        for name in sorted(groups, key=str.casefold):
            render_group(kind, label, groups[name], detail_key)
    return pages, evidence_by_page


def parse_commands() -> tuple[list[str], list[dict[str, str]]]:
    commands_path = ROOT / "src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java"
    command_source = read_text(commands_path).split("public static int cmdConfig", 1)[0]
    commands = list(dict.fromkeys(re.findall(r'literal\("([a-z]+)"\)', command_source)))
    commands = [name for name in commands if name != "clear"]
    if len(commands) < 20:
        raise WikiError(f"parsed only {len(commands)} top-level commands; the source format may have changed")

    rules_path = ROOT / "src/main/java/dev/adventurecraft/awakening/world/GameRules.java"
    rule_pattern = re.compile(r'register\("ac:([a-z_]+)",\s*([^,;)]+)(?:,\s*([^,]+),\s*([^)]+))?\)')
    rules: list[dict[str, str]] = []
    for match in rule_pattern.finditer(read_text(rules_path)):
        rules.append(
            {
                "name": match.group(1),
                "default": match.group(2).strip(),
                "range": f"{match.group(3).strip()}–{match.group(4).strip()}" if match.group(3) else "boolean",
            }
        )
    if len(rules) < 8:
        raise WikiError(f"parsed only {len(rules)} game rules; the source format may have changed")
    return commands, rules


def load_baseline() -> dict[str, object]:
    try:
        data = json.loads(read_text(BASELINE_PATH))
    except json.JSONDecodeError as exc:
        raise WikiError(f"invalid JSON in {BASELINE_PATH.relative_to(ROOT)}: {exc}") from exc
    if data.get("license", {}).get("name") != "CC-BY-SA":
        raise WikiError("Fandom baseline must retain its CC-BY-SA attribution metadata")
    pages = data.get("pages")
    if not isinstance(pages, list) or not pages:
        raise WikiError("Fandom baseline contains no pages")
    titles = [page.get("title") for page in pages]
    if any(not isinstance(title, str) or not title for title in titles):
        raise WikiError("Fandom baseline contains an invalid page title")
    if len(titles) != len(set(titles)):
        raise WikiError("Fandom baseline contains duplicate page titles")
    legacy_map_titles = sorted(title for title in titles if "map" in title.casefold())
    if legacy_map_titles:
        raise WikiError(
            "Fandom baseline contains excluded legacy map page(s): "
            + ", ".join(legacy_map_titles)
        )
    source_count = data.get("source_page_count")
    excluded_count = data.get("excluded_legacy_map_page_count")
    if (
        not isinstance(source_count, int)
        or not isinstance(excluded_count, int)
        or source_count != len(pages) + excluded_count
    ):
        raise WikiError("Fandom baseline source and legacy-map exclusion counts are inconsistent")
    return data


def source_page_names() -> set[str]:
    return {path.stem for path in SOURCE_DIR.glob("*.md")}


def wiki_link_targets(markdown: str) -> set[str]:
    return {match.group(1).strip().replace(" ", "-") for match in re.finditer(r"\[\[([^\]|#]+)", markdown)}


def validate_current_evidence() -> None:
    """Keep hand-written current claims coupled to executable/config sources."""
    expected_snippets = {
        "build.gradle": ["JavaVersion.VERSION_25"],
        "src/main/java/dev/adventurecraft/awakening/ACMainThread.java": ['new File(getWorkingDirectory(), "../maps")'],
        "src/main/java/dev/adventurecraft/awakening/mixin/client/MixinMinecraft.java": [
            "eventKey == Keyboard.KEY_F4",
            "AC_DebugMode.active = !AC_DebugMode.active",
            "eventKey == Keyboard.KEY_F5",
            "this.options.thirdPersonView = !this.options.thirdPersonView",
            "eventKey == Keyboard.KEY_F6",
            "eventKey == Keyboard.KEY_F7",
            "eventKey == Keyboard.KEY_Z",
            "eventKey == Keyboard.KEY_Y",
        ],
        "src/main/java/dev/adventurecraft/awakening/common/AC_JScriptHandler.java": [
            'new File(var2, "scripts")',
            "Files.walk(this.scriptDir.toPath(), 1)",
            'name.endsWith(".js")',
        ],
        "src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java": [
            "exWorld.setTimeOfDay(0L)",
            "exWorld.setTimeOfDay(14000L)",
        ],
    }
    for relative_path, snippets in expected_snippets.items():
        source = read_text(ROOT / relative_path)
        for snippet in snippets:
            if snippet not in source:
                raise WikiError(f"current wiki evidence changed: {snippet!r} is missing from {relative_path}")

    script_source = read_text(ROOT / "src/main/java/dev/adventurecraft/awakening/script/Script.java")
    installed_globals = set(re.findall(r'this\.addObject\("([^"]+)"', script_source))
    required_globals = {"time", "world", "chat", "weather", "effect", "particle", "sound", "ui", "screen", "script", "keyboard", "renderer"}
    if missing := required_globals - installed_globals:
        raise WikiError(f"current scripting globals changed; missing: {', '.join(sorted(missing))}")

    commands, _ = parse_commands()
    obsolete = {"toggledecay", "togglemelting", "mobsburn"} & set(commands)
    if obsolete:
        raise WikiError(f"historical commands were registered again; review the wiki: {', '.join(sorted(obsolete))}")


def render_blocks(entries: list[dict[str, object]]) -> str:
    lines = [
        "# Current Blocks",
        "",
        "> Generated from the current Java registry and English localization. IDs are current AC-Legacy IDs; historical Fandom ID charts are version-specific.",
        "",
        "| Name | ID | Registry field | Implementation |",
        "| --- | ---: | --- | --- |",
    ]
    for entry in entries:
        link = f"{source_url('src/main/java/dev/adventurecraft/awakening/tile/AC_Blocks.java')}#L{entry['line']}"
        target = feature_page_name("Block", str(entry["name"]))
        lines.append(f"| [[{target}|{markdown_escape(str(entry['name']))}]] | {entry['id']} | `{entry['field']}` | [`{entry['class']}`]({link}) |")
    lines.extend(["", "See [[Fact-Checking-and-Provenance]] for how legacy block pages are handled.", ""])
    return "\n".join(lines)


def render_items(entries: list[dict[str, object]]) -> str:
    lines = [
        "# Current Items",
        "",
        "> Generated from the current Java registry and English localization.",
        "",
        "| Name | ID | Registry field | Implementation |",
        "| --- | ---: | --- | --- |",
    ]
    for entry in entries:
        link = f"{source_url('src/main/java/dev/adventurecraft/awakening/item/AC_Items.java')}#L{entry['line']}"
        target = feature_page_name("Item", str(entry["name"]))
        lines.append(f"| [[{target}|{markdown_escape(str(entry['name']))}]] | {entry['id']} | `{entry['field']}` | [`{entry['class']}`]({link}) |")
    lines.append("")
    return "\n".join(lines)


def render_entities(entries: list[dict[str, object]]) -> str:
    lines = [
        "# Current Entities",
        "",
        "> Generated from the current entity registry. This lists AdventureCraft-owned entity registrations, not vanilla Minecraft entities.",
        "",
        "| Registry name | Numeric ID | Implementation |",
        "| --- | ---: | --- |",
    ]
    for entry in entries:
        link = f"{source_url('src/main/java/dev/adventurecraft/awakening/mixin/entity/MixinEntityRegistry.java')}#L{entry['line']}"
        target = feature_page_name("Entity", str(entry["name"]))
        lines.append(f"| [[{target}|{markdown_escape(str(entry['name']))}]] | {entry['id']} | [`{entry['class']}`]({link}) |")
    lines.append("")
    return "\n".join(lines)


def render_commands(commands: list[str], rules: list[dict[str, str]]) -> str:
    if missing := set(commands) - set(COMMAND_DOCS):
        raise WikiError(f"commands are missing documentation: {', '.join(sorted(missing))}")
    if stale := set(COMMAND_DOCS) - set(commands):
        raise WikiError(f"documented commands are no longer registered: {', '.join(sorted(stale))}")
    old_commands = ["toggledecay", "togglemelting", "mobsburn"]
    lines = [
        "# Current Commands",
        "",
        "> Generated from `ServerCommands.java` and `GameRules.java`. Each command links to a source-backed detail page.",
        "",
        "In single-player, command chat is normally opened while debug mode is active. Use `/help <command>` in game for the runtime command tree.",
        "",
        "| Command syntax | Current behavior |",
        "| --- | --- |",
    ]
    for name in commands:
        syntax, description, _ = COMMAND_DOCS[name]
        lines.append(f"| [[Command {name}|{syntax}]] | {description} |")
    lines.extend(
        [
            "",
            "## Game rules",
            "",
            "| Rule | Default | Type/range |",
            "| --- | --- | --- |",
        ]
    )
    lines.extend(f"| `{rule['name']}` | `{rule['default']}` | {rule['range']} |" for rule in rules)
    lines.extend(
        [
            "",
            "## Historical command correction",
            "",
            "The current `/day` command sets time of day to `0`; `/night` sets it to `14000`, not the historical-wiki value of `12000`.",
            "",
            f"The Fandom-era commands {', '.join(f'`/{name}`' for name in old_commands)} are not registered by the current source. Their behavior is represented by `/gamerule` entries above.",
            "",
        ]
    )
    return "\n".join(lines)


def render_command_pages(commands: list[str], rules: list[dict[str, str]]) -> dict[str, str]:
    path = "src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java"
    lines = read_text(ROOT / path).splitlines()
    output: dict[str, str] = {}
    for name in commands:
        syntax, description, notes = COMMAND_DOCS[name]
        line_no = next((index for index, line in enumerate(lines, 1) if f'literal("{name}")' in line), 1)
        page = [
            f"# /{name}",
            "",
            f"`{syntax}`",
            "",
            description,
            "",
            "## Usage notes",
            "",
            notes,
        ]
        if name == "gamerule":
            page.extend(
                [
                    "",
                    "## Registered rules",
                    "",
                    "| Rule | Default | Type/range |",
                    "| --- | --- | --- |",
                    *(f"| `{rule['name']}` | `{rule['default']}` | {rule['range']} |" for rule in rules),
                ]
            )
        page.extend(
            [
                "",
                "## Evidence",
                "",
                f"[`ServerCommands.java` line {line_no}]({source_url(path)}#L{line_no})",
                "",
                "Back to [[Current Commands]].",
                "",
            ]
        )
        output[f"Command-{name}.md"] = "\n".join(page)
    return output


def fandom_page_url(title: str) -> str:
    return FANDOM_WIKI + urllib.parse.quote(title.replace(" ", "_"), safe="()'!,:-")


def render_baseline(data: dict[str, object]) -> str:
    pages = data["pages"]
    counts = Counter(page["disposition"] for page in pages)
    lines = [
        "# Fandom Baseline Index",
        "",
        f"> Inventory captured {data['captured_at']} from the [AdventureCraft Fandom wiki]({data['source']}). It contains **{len(pages)}** non-map main-namespace pages from **{data['source_page_count']}** source pages. **{data['excluded_legacy_map_page_count']}** legacy community-map and map-catalog pages were removed by policy.",
        "",
        "This is a page-level provenance and triage record, not a claim that every historical sentence is valid for AC-Legacy. Only rewritten current pages and generated registries are published as current behavior.",
        "",
        "## Dispositions",
        "",
        "| Disposition | Pages | Meaning |",
        "| --- | ---: | --- |",
    ]
    for key in DISPOSITION_LABELS:
        lines.append(f"| `{key}` | {counts.get(key, 0)} | {DISPOSITION_LABELS[key]} |")
    lines.extend(
        [
            "",
            "## Complete page inventory",
            "",
            "| Fandom page | Last revision | Disposition | Current replacement |",
            "| --- | --- | --- | --- |",
        ]
    )
    for page in pages:
        replacement = f"[[{page['replacement']}]]" if page.get("replacement") else "—"
        timestamp = str(page["revision_timestamp"]).split("T", 1)[0]
        revision_url = f"{fandom_page_url(page['title'])}?oldid={page['revision_id']}"
        lines.append(f"| [{markdown_escape(page['title'])}]({revision_url}) | {timestamp} | `{page['disposition']}` | {replacement} |")
    lines.extend(
        [
            "",
            f"Baseline text remains available under [{data['license']['name']}]({data['license']['url']}) on Fandom. This repository stores metadata and hashes, not a bulk copy of Fandom prose or media.",
            "",
        ]
    )
    return "\n".join(lines)


def render_alias_pages(
    blocks: list[dict[str, object]],
    items: list[dict[str, object]],
    entities: list[dict[str, object]],
) -> tuple[dict[str, str], dict[str, list[str]]]:
    candidates: dict[str, list[tuple[str, str]]] = {}
    for label, entries in (("Block", blocks), ("Item", items), ("Entity", entities)):
        for name in sorted({str(entry["name"]) for entry in entries}, key=str.casefold):
            candidates.setdefault(name, []).append((label, feature_page_name(label, name)))

    manual = {
        "AdventureCraft Wiki": [("Wiki home", "Home")],
        "Effects Block": [("Block", feature_page_name("Block", "Effect Block"))],
        "Lightbulb": [("Block", feature_page_name("Block", "Light Bulb"))],
        "Main Page": [("Wiki home", "Home")],
        "Paintbrush": [("Item", feature_page_name("Item", "Paint Brush"))],
        "Pant Bucket": [("Item", feature_page_name("Item", "Paint Bucket"))],
        "Script": [("Scripting overview", "Scripting")],
    }
    candidates.update(manual)

    pages: dict[str, str] = {}
    evidence: dict[str, list[str]] = {}
    index_lines = [
        "# Aliases",
        "",
        "Current terminology aliases replace historical spelling variants and provide unambiguous entry points for generated feature pages. Community-map aliases are deliberately excluded.",
        "",
        "| Alias | Current target |",
        "| --- | --- |",
    ]
    reserved = source_page_names() | {
        "Current-Blocks", "Current-Items", "Current-Entities", "Current-Commands",
        "Fandom-Baseline-Index", "Scripting-API", "Verification-Status", "Aliases",
        "_Sidebar", "_Footer",
    }
    for display_name in sorted(candidates, key=str.casefold):
        alias_name = page_slug(display_name)
        if alias_name in reserved:
            continue
        targets = list(dict.fromkeys(candidates[display_name]))
        lines = [f"# {display_name}", ""]
        if len(targets) == 1:
            label, target = targets[0]
            lines.extend([f"This term refers to [[{target}|{label}: {display_name}]].", ""])
            target_text = f"[[{target}|{display_name}]]"
        else:
            lines.extend(["This name is used by more than one current registry entry:", ""])
            lines.extend(f"- [[{target}|{label}: {display_name}]]" for label, target in targets)
            lines.append("")
            target_text = ", ".join(f"[[{target}|{label}]]" for label, target in targets)
        lines.extend(["This alias adds no behavior claims. See the target page for source evidence.", ""])
        pages[f"{alias_name}.md"] = "\n".join(lines)
        evidence[alias_name] = ["wiki/feature-details.json", "wiki/fandom-baseline.json"]
        index_lines.append(f"| [[{alias_name}|{markdown_escape(display_name)}]] | {target_text} |")
    index_lines.append("")
    pages["Aliases.md"] = "\n".join(index_lines)
    evidence["Aliases"] = ["wiki/feature-details.json", "wiki/fandom-baseline.json"]
    return pages, evidence


def render_sidebar() -> str:
    return """**AC-Legacy Wiki**

* [[Home]]

**Use and authoring**

* [[Getting Started]]
* [[Controls and Game Modes]]
* [[Scripting]]
* [[Glossary]]

**Generated reference**

* [[Current Commands]]
* [[Current Blocks]]
* [[Current Items]]
* [[Current Entities]]
* [[Scripting API]]

**Project and provenance**

* [[Release History]]
* [[Verification Status]]
* [[Media Provenance]]
* [[Fact Checking and Provenance]]
* [[Fandom Baseline Index]]
* [[Aliases]]
* [[Contributing to the Wiki]]
"""


def render_footer(properties: dict[str, str], revision: str) -> str:
    short_revision = revision[:12] if revision != "unknown" else revision
    return f"""---

Applies to AC-Legacy **{properties['mod_version']}** at source revision **`{short_revision}`**. Current documentation is generated from the [AC-Legacy-Mod source](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod). Historical baseline links point to the [AdventureCraft Fandom wiki](https://adventurecraftmod.fandom.com/wiki/AdventureCraft_Wiki), whose text is reported as [CC-BY-SA](https://www.fandom.com/licensing). See [[Fact Checking and Provenance]] and [[Verification Status]].
"""


def render_verification_status(entries: list[dict[str, object]], properties: dict[str, str], revision: str) -> str:
    short_revision = revision[:12] if revision != "unknown" else revision
    lines = [
        "# Verification Status",
        "",
        f"> Generated for AC-Legacy **{properties['mod_version']}** at source revision **`{short_revision}`**.",
        "",
        "Every canonical page has a status and repository evidence. Generated reference pages are re-derived from their evidence on each build.",
        "",
        "| Page | Status | Evidence |",
        "| --- | --- | --- |",
    ]
    for entry in sorted(entries, key=lambda value: str(value["page"]).casefold()):
        evidence_links = []
        for path in entry["evidence"]:
            evidence_links.append(f"[`{Path(path).name}`]({source_url(path)})")
        lines.append(f"| [[{entry['page']}]] | `{entry['status']}` | {', '.join(evidence_links)} |")
    lines.extend(
        [
            "",
            "Statuses describe evidence quality, not gameplay stability. `verified` means the documented current claim is backed by the listed source; `generated` is rebuilt from a registry/API source; `historical-index` only inventories retained historical references; `alias` redirects terminology without adding behavior claims.",
            "",
        ]
    )
    return "\n".join(lines)


def expand_tokens(markdown: str, properties: dict[str, str], baseline: dict[str, object]) -> str:
    tokens = {
        "MOD_VERSION": properties["mod_version"],
        "MINECRAFT_VERSION": properties["minecraft_version"],
        "LOADER_VERSION": properties["loader_version"],
        "RHINO_VERSION": properties["rhino_version"],
        "BASELINE_CAPTURED_AT": str(baseline["captured_at"]),
        "BASELINE_PAGE_COUNT": str(len(baseline["pages"])),
        "BASELINE_SOURCE_PAGE_COUNT": str(baseline["source_page_count"]),
        "BASELINE_EXCLUDED_MAP_COUNT": str(baseline["excluded_legacy_map_page_count"]),
    }
    for name, value in tokens.items():
        markdown = markdown.replace("{{" + name + "}}", value)
    unresolved = re.findall(r"\{\{[A-Z0-9_]+\}\}", markdown)
    if unresolved:
        raise WikiError(f"unresolved wiki template token(s): {', '.join(sorted(set(unresolved)))}")
    return markdown


def build(output: Path) -> list[Path]:
    properties = read_properties()
    lang = read_lang()
    baseline = load_baseline()
    revision = git_revision()
    authored_metadata = load_page_metadata()
    blocks = parse_blocks(lang)
    items = parse_items(lang)
    entities = parse_entities()
    commands, rules = parse_commands()
    feature_details = load_feature_details(blocks, items, entities)
    feature_pages, feature_evidence = render_feature_pages(blocks, items, entities, feature_details)
    alias_pages, alias_evidence = render_alias_pages(blocks, items, entities)

    import wiki_script_api

    script_source_dir = ROOT / "src/main/java/dev/adventurecraft/awakening/script"
    script_types = wiki_script_api.extract_api(script_source_dir)
    script_base_url = source_url("")
    script_pages = wiki_script_api.render_reference(script_types, script_base_url)
    generated = {
        "Current-Blocks.md": render_blocks(blocks),
        "Current-Items.md": render_items(items),
        "Current-Entities.md": render_entities(entities),
        "Current-Commands.md": render_commands(commands, rules),
        "Fandom-Baseline-Index.md": render_baseline(baseline),
        "_Sidebar.md": render_sidebar(),
        "_Footer.md": render_footer(properties, revision),
    }
    generated.update(render_command_pages(commands, rules))
    generated.update(feature_pages)
    generated.update(alias_pages)
    generated.update(script_pages)

    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)

    for source in sorted(SOURCE_DIR.glob("*.md")):
        content = expand_tokens(read_text(source), properties, baseline)
        (output / source.name).write_text(content.rstrip() + "\n", encoding="utf-8", newline="\n")
    for name, content in generated.items():
        (output / name).write_text(content.rstrip() + "\n", encoding="utf-8", newline="\n")

    media_suffixes = {".avif", ".gif", ".jpeg", ".jpg", ".png", ".svg", ".webp"}
    for source in sorted((WIKI_DIR / "assets").rglob("*")):
        if source.is_file() and source.suffix.casefold() in media_suffixes:
            destination = output / "assets" / source.relative_to(WIKI_DIR / "assets")
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, destination)
    current_assets = ROOT / "src/main/resources/assets/adventurecraft"
    for filename in ("acLogo.png", "icon.png"):
        destination = output / "assets/current" / filename
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(current_assets / filename, destination)

    verification: dict[str, dict[str, object]] = {}
    for page, metadata in authored_metadata.items():
        verification[page] = {"status": metadata["status"], "evidence": metadata["evidence"]}
    fixed_generated = {
        "Current-Blocks": ("generated", ["src/main/java/dev/adventurecraft/awakening/tile/AC_Blocks.java", "src/main/resources/assets/adventurecraft/lang/en_US.lang"]),
        "Current-Items": ("generated", ["src/main/java/dev/adventurecraft/awakening/item/AC_Items.java", "src/main/resources/assets/adventurecraft/lang/en_US.lang"]),
        "Current-Entities": ("generated", ["src/main/java/dev/adventurecraft/awakening/mixin/entity/MixinEntityRegistry.java"]),
        "Current-Commands": ("generated", ["src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java", "src/main/java/dev/adventurecraft/awakening/world/GameRules.java"]),
        "Fandom-Baseline-Index": ("historical-index", ["wiki/fandom-baseline.json"]),
        "_Sidebar": ("navigation", ["tools/wiki.py"]),
        "_Footer": ("navigation", ["tools/wiki.py"]),
    }
    for page, (status, evidence) in fixed_generated.items():
        verification[page] = {"status": status, "evidence": evidence}
    for page, evidence in feature_evidence.items():
        verification[page] = {"status": "generated", "evidence": evidence}
    for page, evidence in alias_evidence.items():
        verification[page] = {"status": "alias", "evidence": evidence}
    command_evidence = ["src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java"]
    for name in commands:
        verification[f"Command-{name}"] = {"status": "generated", "evidence": command_evidence}
    verification["Scripting-API"] = {
        "status": "generated",
        "evidence": ["tools/wiki_script_api.py", "src/main/java/dev/adventurecraft/awakening/script/Script.java"],
    }
    for api_type in script_types:
        verification[f"Scripting-API-{api_type.name}"] = {
            "status": "generated",
            "evidence": [
                "tools/wiki_script_api.py",
                api_type.source_path,
            ],
        }
    verification["Verification-Status"] = {
        "status": "process",
        "evidence": ["tools/wiki.py", "wiki/page-metadata.json", "wiki/feature-details.json"],
    }
    verification_entries = [
        {
            "page": page,
            "status": metadata["status"],
            "applies_to": properties["mod_version"],
            "verified_at_commit": revision,
            "evidence": metadata["evidence"],
        }
        for page, metadata in verification.items()
    ]
    verification_page = render_verification_status(verification_entries, properties, revision)
    (output / "Verification-Status.md").write_text(
        verification_page.rstrip() + "\n", encoding="utf-8", newline="\n"
    )

    files = sorted(output.glob("*.md"))
    page_names = {path.stem for path in files}
    if page_names != set(verification):
        missing = page_names - set(verification)
        stale = set(verification) - page_names
        raise WikiError(f"verification coverage mismatch; missing={sorted(missing)}, stale={sorted(stale)}")
    verification_by_page = {entry["page"]: entry for entry in verification_entries}
    asset_files = sorted(path for path in (output / "assets").rglob("*") if path.is_file())
    manifest = {
        "format": 1,
        "pages": [
            {
                "file": path.name,
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
                **{key: value for key, value in verification_by_page[path.stem].items() if key != "page"},
            }
            for path in files
        ],
        "assets": [
            {
                "file": path.relative_to(output).as_posix(),
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
            }
            for path in asset_files
        ],
    }
    (output / "wiki-manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8", newline="\n")
    return files


def validate() -> None:
    validate_current_evidence()
    baseline = load_baseline()
    expected_dispositions = set(DISPOSITION_LABELS)
    unknown = {page.get("disposition") for page in baseline["pages"]} - expected_dispositions
    if unknown:
        raise WikiError(f"unknown Fandom baseline disposition(s): {', '.join(sorted(unknown))}")

    with tempfile.TemporaryDirectory(prefix="ac-wiki-") as directory:
        output = Path(directory) / "first"
        pages = build(output)
        page_names = {path.stem for path in pages}
        errors: list[str] = []
        required = {"Home", "_Sidebar", "_Footer", "Fact-Checking-and-Provenance"}
        for missing in sorted(required - page_names):
            errors.append(f"missing required wiki page {missing}.md")
        invalid_title = re.compile(r'[\\/:*?"<>|]')
        for name in page_names:
            if invalid_title.search(name):
                errors.append(f"invalid GitHub wiki page title: {name}")
        folded_names = [name.casefold() for name in page_names]
        if len(folded_names) != len(set(folded_names)):
            errors.append("case-insensitive wiki page filename collision")
        for path in pages:
            content = read_text(path)
            for target in wiki_link_targets(content):
                if target not in page_names:
                    errors.append(f"{path.name}: broken wiki link [[{target}]]")
        forbidden = {
            "JDK **21**": "current build requires Java 25",
            "Java **21**": "current build requires Java 25",
            "Java 21+": "current build requires Java 25",
            "Fabric Loader 0.16": "current minimum is read from gradle.properties",
            "F5 toggles level editing": "F5 toggles third-person view; /mapedit controls map editing",
            'player.sendMessage("Hello': "current chat output API is chat.print",
            'world.setBlock(x': "current API uses setBlockID/setBlockIDAndMetadata",
        }
        for path in pages:
            content = read_text(path)
            for text, reason in forbidden.items():
                if text in content:
                    errors.append(f"{path.name}: stale claim {text!r} ({reason})")

        second = Path(directory) / "second"
        build(second)
        first_files = {path.relative_to(output): path.read_bytes() for path in output.rglob("*") if path.is_file()}
        second_files = {path.relative_to(second): path.read_bytes() for path in second.rglob("*") if path.is_file()}
        if first_files != second_files:
            errors.append("wiki output is not deterministic")
        if errors:
            raise WikiError("wiki validation failed:\n  - " + "\n  - ".join(errors))
        print(f"Validated {len(pages)} generated wiki pages and {len(baseline['pages'])} Fandom baseline records.")


def api_request(parameters: dict[str, str]) -> dict[str, object]:
    query = urllib.parse.urlencode({**parameters, "format": "json", "formatversion": "2"})
    request = urllib.request.Request(f"{FANDOM_API}?{query}", headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def classify_page(title: str, categories: list[str], current_names: set[str]) -> tuple[str, str | None]:
    replacement = REPLACEMENTS.get(title)
    if replacement:
        return "rewritten-current", replacement
    category_names = {category.casefold() for category in categories}
    category_text = " ".join(category_names)
    if title.casefold() in current_names:
        if "item" in category_text:
            return "inventory-verified", "Current-Items"
        if "mob" in category_text or "npc" in title.casefold():
            return "inventory-verified", "Current-Entities"
        return "inventory-verified", "Current-Blocks"
    if category_names & LEGACY_MAP_CATEGORIES:
        return "historical-map", None
    if "scripts" in category_names:
        return "historical-script", None
    return "historical-reference", None


def fetch_legacy_map_catalog_links() -> set[str]:
    """Collect map targets from Fandom's legacy catalog pages."""
    titles = set(LEGACY_MAP_CATALOG_PAGES)
    parameters = {
        "action": "query",
        "prop": "links",
        "plnamespace": "0",
        "pllimit": "max",
        "titles": "|".join(sorted(LEGACY_MAP_CATALOG_PAGES)),
    }
    while True:
        response = api_request(parameters)
        for page in response["query"]["pages"]:
            titles.update(link["title"] for link in page.get("links", []))
        continuation = response.get("continue")
        if not continuation:
            return titles
        parameters.update({key: str(value) for key, value in continuation.items()})


def sync_baseline(check_only: bool) -> None:
    all_pages = api_request({"action": "query", "list": "allpages", "apnamespace": "0", "aplimit": "max"})
    page_index = all_pages["query"]["allpages"]
    if all_pages.get("continue"):
        raise WikiError("Fandom returned more than 500 main-namespace pages; add API continuation support")

    lang = read_lang()
    current_names = {
        value.casefold()
        for key, value in lang.items()
        if (key.startswith("tile.") or key.startswith("item.")) and key.endswith(".name")
    }
    current_names.update(entry["name"].casefold() for entry in parse_entities())

    records: list[dict[str, object]] = []
    for offset in range(0, len(page_index), 20):
        titles = "|".join(page["title"] for page in page_index[offset : offset + 20])
        response = api_request(
            {
                "action": "query",
                "prop": "revisions",
                "rvprop": "ids|timestamp|size|sha1|content",
                "rvslots": "main",
                "titles": titles,
            }
        )
        for page in response["query"]["pages"]:
            revision = page["revisions"][0]
            content = revision["slots"]["main"].get("content", "")
            categories = sorted(
                {match.strip() for match in re.findall(r"\[\[Category:([^\]|]+)", content, flags=re.IGNORECASE)}
            )
            redirect_match = re.match(r"\s*#REDIRECT\s*\[\[([^\]|#]+)", content, flags=re.IGNORECASE)
            disposition, replacement = classify_page(page["title"], categories, current_names)
            records.append(
                {
                    "title": page["title"],
                    "page_id": page["pageid"],
                    "revision_id": revision["revid"],
                    "revision_timestamp": revision["timestamp"],
                    "content_bytes": revision["size"],
                    "content_sha1": revision["sha1"],
                    "is_redirect": content.lstrip().upper().startswith("#REDIRECT"),
                    "redirect_target": redirect_match.group(1).strip() if redirect_match else None,
                    "categories": categories,
                    "disposition": disposition,
                    "replacement": replacement,
                }
            )

    source_page_count = len(records)
    catalog_links = fetch_legacy_map_catalog_links()
    legacy_map_records = [
        page
        for page in records
        if page["disposition"] == "historical-map"
        or page["title"] in catalog_links
        or "map" in page["title"].casefold()
    ]
    records = [page for page in records if page not in legacy_map_records]

    site = api_request({"action": "query", "meta": "siteinfo", "siprop": "general|rightsinfo"})["query"]
    records.sort(key=lambda page: page["title"].casefold())
    snapshot = {
        "format": 1,
        "source": site["general"]["base"],
        "api": FANDOM_API,
        "captured_at": site["general"]["time"],
        "scope": "Non-map namespace-0 pages returned by list=allpages; legacy community maps and catalogs are excluded; content is hashed but not copied.",
        "source_page_count": source_page_count,
        "excluded_legacy_map_page_count": len(legacy_map_records),
        "license": {"name": site["rightsinfo"]["text"], "url": site["rightsinfo"]["url"]},
        "pages": records,
    }
    serialized = json.dumps(snapshot, indent=2, ensure_ascii=False) + "\n"
    if check_only:
        existing_data = json.loads(read_text(BASELINE_PATH))
        snapshot["captured_at"] = existing_data.get("captured_at")
        serialized = json.dumps(snapshot, indent=2, ensure_ascii=False) + "\n"
        if read_text(BASELINE_PATH) != serialized:
            raise WikiError("the committed Fandom baseline differs from the live API; run `python tools/wiki.py sync-baseline`")
        print(f"Fandom baseline is current ({len(records)} pages).")
        return
    BASELINE_PATH.write_text(serialized, encoding="utf-8", newline="\n")
    print(f"Wrote {len(records)} Fandom page records to {BASELINE_PATH.relative_to(ROOT)}.")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)
    build_parser = subparsers.add_parser("build", help="generate GitHub wiki files")
    build_parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    subparsers.add_parser("check", help="validate source facts, generated pages, and links")
    sync_parser = subparsers.add_parser("sync-baseline", help="refresh Fandom page metadata and content hashes")
    sync_parser.add_argument("--check", action="store_true", help="fail instead of writing when the live baseline changed")
    args = parser.parse_args()

    try:
        if args.command == "build":
            files = build(args.output.resolve())
            print(f"Generated {len(files)} wiki pages in {args.output.resolve()}.")
        elif args.command == "check":
            validate()
        elif args.command == "sync-baseline":
            sync_baseline(args.check)
    except (WikiError, KeyError, OSError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
