#!/usr/bin/env python3
"""Apply the TRUMA SHOW 1.1.0 overlay to the original, verified Android source."""
from pathlib import Path
import shutil
import sys

root = Path(sys.argv[1])
over = Path(__file__).parent / "overlay"
pkg = root / "app/src/main/java/de/orgoner/trumashow"
tests = root / "app/src/test/java/de/orgoner/trumashow"
for name in ("SpecialMenuActivity.java", "AuditRules.java"):
    shutil.copy2(over / name, pkg / name)
shutil.copy2(over / "AuditRulesTest.java", tests / "AuditRulesTest.java")

def swap(p, old, new):
    x = p.read_text(encoding="utf-8")
    assert old in x, f"Patch target missing in {p}: {old[:70]}"
    p.write_text(x.replace(old, new), encoding="utf-8")

main = pkg / "MainActivity.java"
swap(main, "private final AtomicBoolean cancel = new AtomicBoolean(false);",
    "public static final AtomicBoolean EMERGENCY_CANCEL = new AtomicBoolean(false);\n    private final AtomicBoolean cancel = EMERGENCY_CANCEL;")
swap(main, "colorBg = Color.rgb(10,15,27), colorPanel = Color.rgb(22,31,49)",
    "colorBg = Color.rgb(8,11,10), colorPanel = Color.rgb(23,28,25)")
swap(main, "colorInk = Color.rgb(235,242,252), colorMuted = Color.rgb(154,173,198)",
    "colorInk = Color.rgb(232,239,220), colorMuted = Color.rgb(159,174,151)")
swap(main, "colorAccent = Color.rgb(19,194,186)",
    "colorAccent = Color.rgb(255,163,46)")
swap(main, "NET SECURITY  ·  FIRMENEDITION",
    "TRUMA://FLIPPER   /   SECURITY")
swap(main, 'text("TRUMA SHOW", 30, colorInk, true)',
    'text("TRUMA SHOW // F", 27, colorInk, true)')
swap(main, "WLAN-Inventur · TCP-Portprüfung · Telegram-Warnungen",
    "LAN DISCOVERY · PORT AUDIT · ALERTS")
swap(main, 'content.addView(text("Version 1.0.0  ·  Keine heimliche Fernsteuerung", 12, colorMuted, false));',
    'content.addView(text("Version 1.1.0 · autorisierte Diagnose", 12, colorMuted, false));\n'
    '        button(content, "BLACK LAB  /  SPEZIALMENÜ", true, v ->\n'
    '                startActivity(new Intent(this, SpecialMenuActivity.class)));')
swap(main, 'if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);',
    'v.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);')
swap(main, 'b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);',
    'b.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);')
swap(main, 'e.setHintTextColor(colorMuted); e.setHint(hint); e.setInputType(type);',
    'e.setHintTextColor(colorMuted); e.setHint(hint); e.setInputType(type); e.setTypeface(Typeface.MONOSPACE);')
swap(main, 'bg.setColor(colorPanel); bg.setCornerRadius(dp(16));',
    'bg.setColor(colorPanel); bg.setCornerRadius(dp(7)); bg.setStroke(dp(1),Color.rgb(79,69,45));')
swap(main, 'Color.rgb(35,47,69)', 'Color.rgb(32,38,33)')
swap(main, 'Color.rgb(47,64,87)', 'Color.rgb(43,48,40)')
swap(main,
    'if (resultsView != null) resultsView.setText(ScanStore.summary(this));',
    'if (resultsView != null) resultsView.setText(ScanStore.summary(this));\n'
    '        if (portsView != null && ScanStore.prefs(this).getBoolean("profile_just_set", false)) {\n'
    '            portsView.setText(ScanStore.prefs(this).getString("ports","80,443"));\n'
    '            ScanStore.prefs(this).edit().putBoolean("profile_just_set",false).apply();\n'
    '        }')
manifest = root / "app/src/main/AndroidManifest.xml"
swap(manifest, '<activity android:name=".MainActivity" android:exported="true">',
    '<activity android:name=".SpecialMenuActivity" android:exported="false" />\n'
    '        <activity android:name=".MainActivity" android:exported="true">')
gradle = root / "app/build.gradle"
swap(gradle, "versionCode 1", "versionCode 2")
swap(gradle, "versionName '1.0.0'", "versionName '1.1.0'")
scan_store = pkg / "ScanStore.java"
swap(scan_store, "TRUMA SHOW 1.0 |", "TRUMA SHOW 1.1 |")
print("TRUMA SHOW Flipper BLACK LAB overlay applied.")
