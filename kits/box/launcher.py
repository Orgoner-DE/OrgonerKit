#!/usr/bin/env python3
"""Oeffnet den lokalen Kit-Launcher im Browser."""
from __future__ import annotations
import os, webbrowser
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
html = os.path.join(ROOT, "START.html")
if os.path.isfile(html):
    webbrowser.open("file://" + html.replace("\\", "/"))
    print("Launcher geoeffnet:", html)
else:
    raise SystemExit("START.html nicht gefunden")
