# OrgonerKit 2.0

Eigenstaendige Kit-Programme. Kein Chatbot. Jedes Kit laeuft allein.

Repo: https://github.com/Orgoner-DE/OrgonerKit
Live-PWA (separat): https://orgoner-de.github.io/

## Kits

| Kit | Start | Was es macht |
|---|---|---|
| **PrintKit** | `python3 kits/print/werkzeug.py` | STL erzeugen, reparieren, optimieren, skalieren |
| **FreqKit** | `python3 kits/freq/wavgen.py` oder `kits/freq/freq.html` | Sinus/Rechteck/Duty als WAV (Audio-Experiment) |
| **CadKit** | OpenSCAD-Dateien in `kits/cad/` | Propeller, Felge, Spulenwickler, Hobby-Chassis |
| **ScaleKit** | `python3 kits/rc/scale_calc.py` oder `kits/rc/scale.html` | Massstab 1:18->1:14 = 192.2 %, Body-Fit |
| **CatalogKit** | `kits/catalog/index.html` | Offline-Katalog + Suchlinks Printables/Thingiverse/Cults |
| **NotesKit** | `kits/notes/notes.html` | lokale Aufgabenliste im Browser |
| **Launcher** | `START.html` / `START.bat` / `START.sh` | alle Programme oeffnen |

## Installation PrintKit

```bash
python3 -m pip install -r requirements.txt
chmod +x kits/print/werkzeug.py kits/print/batch_repair.sh kits/freq/wavgen.py START.sh
```

Python 3.10+, Pakete: numpy, trimesh, fast_simplification.

## PrintKit

```bash
python3 kits/print/werkzeug.py create cube --length 20 --width 10 --height 5 -o out/wuerfel.stl
python3 kits/print/werkzeug.py create sphere --radius 15 -o out/kugel.stl
python3 kits/print/werkzeug.py create cylinder --radius 8 --height 30 -o out/zylinder.stl
python3 kits/print/werkzeug.py create torus --major 12 --minor 3 -o out/torus.stl
python3 kits/print/werkzeug.py repair datei.stl -o out/
python3 kits/print/werkzeug.py optimize datei.stl --operation full --ratio 0.4
python3 kits/print/werkzeug.py performance datei.stl --ratio 0.25
python3 kits/print/werkzeug.py scale datei.stl --factor 1.922 -o out/s15_114.stl
./kits/print/batch_repair.sh ./downloads ./repariert
```

Einzelprogramme unter `kits/print/` funktionieren ohne werkzeug.

## FreqKit (nur Audio)

Kein Diagnose- und kein Therapiegeraet. Nur Ton / Experiment / Cymatics.

```bash
python3 kits/freq/wavgen.py --hz 35 --seconds 30 --wave sine -o 35Hz_sine.wav
python3 kits/freq/wavgen.py --hz 432 --seconds 10 --wave square --duty 25 -o 432_duty25.wav
```

Oder `kits/freq/freq.html` im Browser (Live-Ton + WAV-Download).

## CadKit

In OpenSCAD oeffnen, F5 Vorschau, F6 rendern, STL exportieren.
Oder https://openscad.cloud

## Was bewusst NICHT drin ist

- Klone von DJI Fly, PotensicPro, RX Drone, iTorus, Tuya, Todoist
- E-Bike-Tuning / Entfernen gesetzlicher Geschwindigkeitsgrenzen
- Medizinische Frequenzlisten als Behandlung

Prusa CORE One L: 300 x 300 x 330 mm. Pipeline: Download -> repair -> ggf. performance -> PrusaSlicer 0.20 mm, 3 Waende, 15-20 % Gyroid.
