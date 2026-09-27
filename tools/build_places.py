#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Merge the four "100 of Japan" collections into one bundled places.json.

Sources:
- app/src/main/assets/aromas.json  (the aroma snapshot, produced by sync_data.py)
- tools/data/scenery.json          (authored: 36 Special Places of Scenic Beauty)
- tools/data/waters.json           (authored: 100 Remarkable Waters)
- (later) tools/data/sounds.json

Each source is normalised to the German-keyed schema the Place model reads
(@SerialName), tagged with its collection, and written to
app/src/main/assets/places.json (a committed snapshot, so CI needs no sources).

Re-run after changing any collection:

    python3 tools/build_places.py
"""
import json
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "app", "src", "main", "assets")
DATA = os.path.join(REPO, "tools", "data")
OUT = os.path.join(ASSETS, "places.json")


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def aroma_places():
    """The existing aroma snapshot, tagged as the aroma collection."""
    aromas = load(os.path.join(ASSETS, "aromas.json"))
    for a in aromas:
        a["collection"] = "aroma"
    return aromas


def authored_places(filename, collection):
    """An authored collection (scenery/waters/sounds), mapped to the Place schema."""
    out = []
    for s in load(os.path.join(DATA, filename)):
        entry = {
            "collection": collection,
            "nummer": s["number"],
            "titel_ja": s["titleJa"],
            "titel_de": s["titleDe"],
            "praefektur": s["prefecture"],
            "ort": s["city"],
            "lat": s["lat"],
            "lon": s["lon"],
            "beschreibung_ja": s["descriptionJa"],
            "beschreibung_de": s["descriptionDe"],
        }
        if s.get("wikipedia"):
            entry["wikipedia"] = s["wikipedia"]
        out.append(entry)
    return out


def main():
    places = (
        aroma_places()
        + authored_places("scenery.json", "scenery")
        + authored_places("waters.json", "water")
    )
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(places, f, ensure_ascii=False, indent=2)
    by_collection = {}
    for p in places:
        by_collection[p["collection"]] = by_collection.get(p["collection"], 0) + 1
    print(f"wrote {len(places)} places to {OUT}")
    print("by collection:", by_collection)


if __name__ == "__main__":
    main()
