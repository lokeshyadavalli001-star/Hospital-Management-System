#!/usr/bin/env bash
# ==============================================================================
# HMS Localhost Web Server Launcher (Unix/Linux/macOS)
# ==============================================================================

set -e

echo "================================================================================"
echo "      Launching Hospital Management System (HMS) Localhost Web Server..."
echo "================================================================================"

mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm -f sources.txt

echo "[SUCCESS] Build complete!"
echo "[INFO] Starting Localhost HTTP Server on http://localhost:8080 ..."

# Attempt to open default browser
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:8080 &
elif command -v open &> /dev/null; then
    open http://localhost:8080 &
fi

java -Dfile.encoding=UTF-8 -cp out com.hms.ui.LocalHostServer
