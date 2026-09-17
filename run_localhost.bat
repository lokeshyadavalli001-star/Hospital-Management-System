@echo off
echo ================================================================================
echo       Launching Hospital Management System (HMS) Localhost Web Server...
echo ================================================================================

where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'javac' is not found in your system PATH.
    echo Please ensure JDK 17 or higher is installed and added to PATH.
    pause
    exit /b 1
)

if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt

echo [SUCCESS] Build complete!
echo [INFO] Starting Localhost HTTP Server on http://localhost:8080 ...
echo [INFO] Opening browser...
start http://localhost:8080

java -Dfile.encoding=UTF-8 -cp out com.hms.ui.LocalHostServer
pause
