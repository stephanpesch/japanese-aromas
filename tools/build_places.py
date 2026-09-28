#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Merge the four "100 of Japan" collections into one bundled places.json.

Sources:
- app/src/main/assets/aromas.json  (the aroma snapshot, produced by sync_data.py)
- tools/data/scenery.json          (authored: 36 Special Places of Scenic Beauty)
- tools/data/waters.json           (authored: 100 Remarkable Waters)
- tools/data/sounds.json           (authored: 100 Soundscapes)

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
        if s.get("image"):
            entry["bild"] = s["image"]
            entry["bild_quelle"] = s.get("imageAttribution", "Wikimedia Commons")
        # Season + category (added to the non-aroma collections so the season and
        # per-collection category filters work) + availability (day/time windows
        # for the "now" filter); keys already match the schema.
        for key in ("kategorie", "kategorien", "monate", "ganzjaehrig", "saison_ja", "saison_de", "verfuegbarkeit"):
            if key in s:
                entry[key] = s[key]
        out.append(entry)
    return out


def apply_availability_overlay(places):
    """Merge researched availability windows from data/availability.json by place id.

    Keyed by "<collection>-<number>", this lets us attach opening hours / event
    times to any place — including the generated aroma snapshot, which must not
    be hand-edited. An inline "verfuegbarkeit" on an authored place wins, so the
    overlay only fills places that do not already carry one.
    """
    path = os.path.join(DATA, "availability.json")
    if not os.path.exists(path):
        return 0
    overlay = load(path)
    applied = 0
    for p in places:
        pid = f"{p['collection']}-{p['nummer']}"
        if pid in overlay and "verfuegbarkeit" not in p:
            p["verfuegbarkeit"] = overlay[pid]
            applied += 1
    return applied


def main():
    places = (
        aroma_places()
        + authored_places("scenery.json", "scenery")
        + authored_places("waters.json", "water")
        + authored_places("sounds.json", "sound")
    )
    overlaid = apply_availability_overlay(places)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(places, f, ensure_ascii=False, indent=2)
    by_collection = {}
    for p in places:
        by_collection[p["collection"]] = by_collection.get(p["collection"], 0) + 1
    with_avail = sum(1 for p in places if p.get("verfuegbarkeit"))
    print(f"wrote {len(places)} places to {OUT}")
    print("by collection:", by_collection)
    print(f"availability windows: {with_avail} places ({overlaid} from overlay)")


if __name__ == "__main__":
    main()
