#!/bin/sh
cd "$(dirname "$0")"
(xdg-open START.html || open START.html || python3 -m webbrowser START.html) >/dev/null 2>&1 &
