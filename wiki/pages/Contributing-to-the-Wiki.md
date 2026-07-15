# Contributing to the Wiki

Authored wiki source lives in `wiki/pages`. Generated reference pages must not be edited by hand; they are rebuilt from Java registries, source-backed feature details, current command registrations, the Java scripting API, and the retained non-map Fandom audit snapshot.

Run the same validation as CI:

```shell
python tools/wiki.py check
python tools/wiki.py build --output build/wiki
python tools/wiki_quality.py sources wiki/pages
python tools/wiki_quality.py quality build/wiki --minimum-pages 250
python -m unittest tools/test_wiki_quality.py tools/tests/test_wiki_script_api.py
```

The generated native-wiki checkout is written to `build/wiki`.

To refresh the page-level Fandom inventory deliberately:

```shell
python tools/wiki.py sync-baseline
python tools/wiki.py check
```

Review baseline changes before committing them. A changed Fandom page remains historical by default; moving information into a current page requires evidence in this repository.

Community-built map pages, download pages, and legacy map catalogs are outside this wiki's scope. The baseline synchronizer removes them before writing the snapshot. Do not recreate them as authored pages, aliases, screenshots, or imported media.

When adding or changing an authored page, update `wiki/page-metadata.json` with its evidence. Current feature documentation belongs in `wiki/feature-details.json`. Record any accepted screenshot in `wiki/assets/media.json`, including the source commit and confirmation that no community map artwork is present.

See [`wiki/README.md`](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/wiki/README.md) for deployment setup and ownership rules.
