#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Sync the aroma dataset (source of truth: ../../100-aromas) into the app assets.

Reads 100-aromas/app/daten.js (window.AROMEN = [...]), writes a clean
app/src/main/assets/aromas.json and copies the 100 photos into assets/images/.
The generated snapshot is committed so CI builds without the 100-aromas folder.
Re-run after changing the dataset:

    python3 tools/sync_data.py
"""
import json
import os
import shutil
import sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.environ.get("SOURCE_100AROMAS", os.path.join(REPO, "..", "..", "100-aromas"))
DATEN = os.path.join(SRC, "app", "daten.js")
BILDER = os.path.join(SRC, "bilder")
ASSETS = os.path.join(REPO, "app", "src", "main", "assets")
IMAGES_OUT = os.path.join(ASSETS, "images")


def main():
    if not os.path.exists(DATEN):
        sys.exit(f"Quelle nicht gefunden: {DATEN} (SOURCE_100AROMAS setzen?)")
    text = open(DATEN, encoding="utf-8").read()
    arr = json.loads(text[text.index("["):text.rindex("]") + 1])

    for e in arr:
        e["bild"] = f"images/{e['nummer']:03d}.jpg"
        e.pop("md", None)  # markdown link not needed in the app

    os.makedirs(IMAGES_OUT, exist_ok=True)
    for f in os.listdir(IMAGES_OUT):
        if f.endswith(".jpg"):
            os.remove(os.path.join(IMAGES_OUT, f))
    copied = 0
    for e in arr:
        src = os.path.join(BILDER, f"{e['nummer']:03d}.jpg")
        if not os.path.exists(src):
            sys.exit(f"Bild fehlt: {src}")
        shutil.copy2(src, os.path.join(IMAGES_OUT, f"{e['nummer']:03d}.jpg"))
        copied += 1

    out = os.path.join(ASSETS, "aromas.json")
    with open(out, "w", encoding="utf-8") as f:
        json.dump(arr, f, ensure_ascii=False, indent=1)

    assert len(arr) == 100, f"Erwartet 100 Einträge, {len(arr)}"
    assert copied == 100, f"Erwartet 100 Bilder, {copied}"
    print(f"OK: aromas.json ({len(arr)} Einträge) + {copied} Bilder → app/src/main/assets/")


if __name__ == "__main__":
    main()
