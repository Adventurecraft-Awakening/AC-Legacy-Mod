# GitHub wiki pipeline

This directory is the reviewed source for the repository's native GitHub wiki. `tools/wiki.py` combines the authored pages in `wiki/pages`, current Java registries and build properties, source-derived feature details, the Java scripting API, and the retained non-map records in `fandom-baseline.json` into GitHub-wiki Markdown.

## Local commands

```shell
python tools/wiki.py check
python tools/wiki.py build --output build/wiki
python tools/wiki_quality.py sources wiki/pages
python tools/wiki_quality.py quality build/wiki --minimum-pages 250
python -m unittest tools/test_wiki_quality.py tools/tests/test_wiki_script_api.py
python tools/wiki.py sync-baseline       # intentional network refresh only
python tools/wiki.py sync-baseline --check
```

Do not edit `build/wiki`; it is deterministic generated output and is ignored with the rest of `build/`.

## One-time repository setup

1. Enable **Wikis** in the repository settings.
2. Create the initial wiki page in GitHub's UI. GitHub does not expose `<repository>.wiki.git` until the wiki has at least one page.
3. Keep **Restrict editing to collaborators only** enabled so the reviewed source remains authoritative.
4. Ensure Actions can write repository contents under **Settings → Actions → General → Workflow permissions**. The publisher uses the job-scoped `GITHUB_TOKEN`; it does not need a long-lived personal access token.
5. Create a `github-wiki` Actions environment restricted to `main`; optionally require a reviewer.
6. Optionally set the repository variable `WIKI_MIN_PAGES` to a deletion-safety floor. It defaults to 250.

The workflow validates and uploads a preview for pull requests. It publishes only after a successful build on `main` (or a manual run from `main`). Pull requests, including forks, never receive a write token or run the deployment job.

## Ownership and deletion

The generated output owns all files in the native wiki repository. Deployment uses a normal, non-force commit and removes remote files that are no longer generated. Manual edits made directly in GitHub's wiki UI will therefore be overwritten or deleted; make edits in `wiki/pages` through a pull request instead.

Fandom content is CC-BY-SA. The committed baseline stores attribution metadata and content hashes, not a bulk copy. Community-built map pages and map-catalog pages are excluded during synchronization and must not be added to the native wiki. Review licensing and provenance before importing any other prose or media.
