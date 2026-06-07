#!/usr/bin/env python3
"""Validate MenuFramework YAML against docs/menu.schema.json.

Checks two sources so documentation and shipped menus never drift from the schema:
  1. Full-menu ```yaml blocks in AGENTS.md and README.md (blocks with a top-level `rows`).
  2. Every shipped menu under example-plugin/src/main/resources/menus.

Exits non-zero if any document is invalid. Run locally with:
    pip install jsonschema pyyaml
    python scripts/validate_docs_yaml.py
"""

import json
import re
import sys
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parent.parent
SCHEMA = json.loads((ROOT / "docs" / "menu.schema.json").read_text(encoding="utf-8"))
FENCE = re.compile(r"```yaml\n(.*?)```", re.DOTALL)


def markdown_menus(path):
    """Yield (label, data, error) for each full-menu yaml block in a markdown file."""
    for index, body in enumerate(FENCE.findall(path.read_text(encoding="utf-8")), start=1):
        label = f"{path.name} yaml block #{index}"
        try:
            data = yaml.safe_load(body)
        except yaml.YAMLError as error:
            yield label, None, f"invalid YAML: {error}"
            continue
        if isinstance(data, dict) and "rows" in data:  # a full menu, not a partial snippet
            yield label, data, None


def shipped_menus():
    """Yield (label, data, error) for each shipped menu resource."""
    menus_dir = ROOT / "example-plugin" / "src" / "main" / "resources" / "menus"
    for path in sorted(menus_dir.glob("*.yml")):
        label = str(path.relative_to(ROOT)).replace("\\", "/")
        try:
            yield label, yaml.safe_load(path.read_text(encoding="utf-8")), None
        except yaml.YAMLError as error:
            yield label, None, f"invalid YAML: {error}"


def collect():
    for name in ("AGENTS.md", "README.md"):
        yield from markdown_menus(ROOT / name)
    yield from shipped_menus()


def main():
    Draft202012Validator.check_schema(SCHEMA)
    validator = Draft202012Validator(SCHEMA)

    checked = 0
    failures = 0
    for label, data, error in collect():
        if error:
            failures += 1
            print(f"FAIL  {label}: {error}")
            continue
        errors = sorted(validator.iter_errors(data), key=lambda e: list(e.path))
        if errors:
            failures += 1
            print(f"FAIL  {label}")
            for e in errors:
                location = "/".join(str(part) for part in e.path) or "(root)"
                print(f"        at {location}: {e.message}")
        else:
            checked += 1
            print(f"OK    {label}")

    print(f"\n{checked} valid, {failures} failed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
