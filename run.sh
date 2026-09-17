#!/usr/bin/env bash
# ==============================================================================
# Hospital Management System (HMS) - Unix/Linux/macOS Launcher
# ==============================================================================

set -e

echo "================================================================================"
echo "               Hospital Management System (HMS) - Unix Launcher"
echo "================================================================================"

# Verify JDK compiler presence
if ! command -v javac &> /dev/null; then
    echo "[ERROR] 'javac' is not installed or not in PATH."
    echo "Please install JDK 17 or higher (e.g. OpenJDK 17/21)."
    exit 1
fi

mkdir -p out

echo "[INFO] Finding Java source files..."
find src -name "*.java" > sources.txt

echo "[INFO] Compiling Core Java application..."
javac -encoding UTF-8 -d out @sources.txt
rm -f sources.txt

echo "[SUCCESS] Compilation successful!"
echo "[INFO] Starting Hospital Management System..."
echo "================================================================================"
java -Dfile.encoding=UTF-8 -cp out com.hms.Main
