# Auf einem Windows-PC mit Python:
#   pip install pyinstaller
#   powershell -ExecutionPolicy Bypass -File windows\build_exe.ps1
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
python -m pip install pyinstaller -q
pyinstaller --noconfirm --onefile --name OrgonerKitLauncher --add-data "START.html;." kits/box/launcher.py
Write-Host "Fertig: dist\OrgonerKitLauncher.exe"
