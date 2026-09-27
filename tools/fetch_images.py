#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fetch a representative Wikimedia Commons image + attribution for each place
in the authored collections (scenery/waters/sounds) and write it back in place.

For each entry it looks up the lead image of the matching Japanese Wikipedia
article (pageimages), then the image's author + licence on Commons (imageinfo).
The result is stored as `image` (a Commons thumbnail URL) and `imageAttribution`
(a short credit line); entries without a usable image are left unchanged. The
images are loaded on demand (Coil), not bundled, so the APK does not grow.
Queries are batched and rate-limited to stay well within Wikimedia etiquette.

    python3 tools/fetch_images.py
"""
import html
import json
import os
import re
import time
import urllib.error
import urllib.parse
import urllib.request

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(REPO, "tools", "data")
FILES = ["scenery.json", "waters.json", "sounds.json"]
JA_API = "https://ja.wikipedia.org/w/api.php"
COMMONS_API = "https://commons.wikimedia.org/w/api.php"
UA = {"User-Agent": "japanese-aromas-app/1.0 (educational; github.com/stephanpesch/japanese-aromas)"}
THUMB_SIZE = 1200  # requested size; Wikipedia snaps to the next standard bucket
BATCH = 40
PAUSE = 1.0
# Skip infobox locator/topographic maps and other non-photo lead images.
MAP_MARKERS = ("svg", "topograph", "olocalis", "locator", "location_map")


def api_get(base, params):
    url = base + "?" + urllib.parse.urlencode({**params, "format": "json"})
    for attempt in range(5):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=30) as r:
                return json.load(r)
        except urllib.error.HTTPError as e:
            if e.code == 429:
                time.sleep(5 * (attempt + 1))
                continue
            raise
    raise RuntimeError("giving up after repeated 429s")


def article_title(entry):
    wiki = entry.get("wikipedia")
    if wiki:
        return urllib.parse.unquote(wiki.rstrip("/").split("/wiki/")[-1])
    return entry["titleJa"]


def chunks(items, size):
    for i in range(0, len(items), size):
        yield items[i:i + size]


def clean(value):
    text = re.sub("<[^>]+>", "", html.unescape(value or "")).strip()
    return re.sub(r"\s+", " ", text)[:80]


def lead_images(titles):
    """{query_title: (thumb_url, 'File:name')} for each title that has a lead image."""
    result = {}
    for group in chunks(titles, BATCH):
        data = api_get(JA_API, {
            "action": "query", "prop": "pageimages", "piprop": "thumbnail|name",
            "pithumbsize": THUMB_SIZE, "titles": "|".join(group), "redirects": 1,
        })
        query = data.get("query", {})
        resolve = {}
        for m in query.get("normalized", []) + query.get("redirects", []):
            resolve[m["from"]] = m["to"]

        def final(t):
            seen = set()
            while t in resolve and t not in seen:
                seen.add(t)
                t = resolve[t]
            return t

        # If two titles resolve to the same article, only one keeps the image
        # (harmless today — no two places share an article).
        final_to_query = {final(qt): qt for qt in group}
        for page in query.get("pages", {}).values():
            qt = final_to_query.get(page.get("title"))
            thumb = page.get("thumbnail", {}).get("source")
            name = page.get("pageimage")
            if qt and thumb and name and not is_map(name):
                result[qt] = (thumb.split("?")[0], "File:" + name)
        time.sleep(PAUSE)
    return result


def is_map(name):
    lower = name.lower()
    return any(marker in lower for marker in MAP_MARKERS)


def attributions(file_titles):
    """{'File:name': 'Author · Licence / Wikimedia Commons'} for each file."""
    result = {}
    for group in chunks(sorted(set(file_titles)), BATCH):
        data = api_get(COMMONS_API, {
            "action": "query", "prop": "imageinfo", "iiprop": "extmetadata", "titles": "|".join(group),
        })
        query = data.get("query", {})
        normalized = {m["to"]: m["from"] for m in query.get("normalized", [])}
        for page in query.get("pages", {}).values():
            title = page.get("title")
            key = normalized.get(title, title)
            info = page.get("imageinfo")
            if not info:
                continue
            meta = info[0].get("extmetadata", {})
            parts = [clean(meta.get(k, {}).get("value")) for k in ("Artist", "LicenseShortName")]
            parts = [p for p in parts if p]
            result[key] = (" · ".join(parts) + " / Wikimedia Commons") if parts else "Wikimedia Commons"
        time.sleep(PAUSE)
    return result


def main():
    for filename in FILES:
        path = os.path.join(DATA, filename)
        with open(path, encoding="utf-8") as f:
            entries = json.load(f)
        titles = [article_title(e) for e in entries]
        images = lead_images(titles)
        attrib = attributions([f for _, f in images.values()])
        found = 0
        for entry, title in zip(entries, titles):
            hit = images.get(title)
            if hit:
                thumb, file_title = hit
                entry["image"] = thumb
                entry["imageAttribution"] = attrib.get(file_title, "Wikimedia Commons")
                found += 1
        with open(path, "w", encoding="utf-8") as f:
            json.dump(entries, f, ensure_ascii=False, indent=2)
        print(f"{filename}: {found}/{len(entries)} entries got an image")


if __name__ == "__main__":
    main()
