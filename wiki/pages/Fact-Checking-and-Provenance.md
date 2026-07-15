# Fact Checking and Provenance

This wiki separates current AC-Legacy behavior from historical AdventureCraft material.

## Evidence order

1. Current executable source and registries
2. Current build metadata and localization
3. Repository changelogs for release-specific behavior
4. Historical Fandom pages, clearly labeled and linked

Generated registry and command pages are rebuilt from source on every wiki CI run. Validation fails on broken internal links, parser drift, missing baseline/license metadata, and several known stale claims.

## Fandom baseline audit

The baseline was captured through Fandom's public MediaWiki API on **{{BASELINE_CAPTURED_AT}}**. The API returned **{{BASELINE_SOURCE_PAGE_COUNT}}** namespace-0 pages. This repository retains **{{BASELINE_PAGE_COUNT}}** non-map records after removing **{{BASELINE_EXCLUDED_MAP_COUNT}}** legacy community-map and map-catalog pages. For each retained page it records page and revision IDs, revision timestamp, content size and SHA-1, categories, disposition, and any current replacement. It intentionally does not bulk-copy prose or media.

Disposition meanings:

- `rewritten-current`: a current page replaces the historical topic and is backed by repository evidence.
- `inventory-verified`: the named block, item, or entity exists now, but historical behavioral prose was not assumed current.
- `historical-script`: community script/tutorial material that has not been runtime-tested against this branch.
- `historical-reference`: retained as a link and provenance record, not imported as current documentation.

This is exhaustive page-level accounting for the retained non-map scope, not sentence-level validation of community-authored history. Unverified Fandom prose is never presented here as current fact.

## Corrected baseline claims

- Development builds require Java 25, not Java 21.
- The configured minimum Fabric Loader is {{LOADER_VERSION}}, not 0.16.10.
- F5 toggles third-person view; `/mapedit` controls map-editing mode.
- Historical `/toggledecay`, `/togglemelting`, and `/mobsburn` commands have been replaced by `/gamerule` entries.
- Old numeric block IDs are version-scoped; the current loader contains explicit legacy-ID migration.
- Current script chat output uses `chat.print()`, and the current world wrapper uses methods such as `setBlockID()` rather than the generic examples previously repeated in project prose.

## Licensing

Fandom reports its wiki content as [CC-BY-SA](https://www.fandom.com/licensing). This project links and attributes the baseline while storing metadata and hashes. Any future prose or media copied from Fandom must preserve author attribution, source history, and compatible share-alike licensing; do not assume the repository's MIT license covers imported wiki content.
