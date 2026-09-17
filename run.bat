@echo off
setlocal enabledelayedexpansion

echo ================================================================================
echo               Hospital Management System (HMS) - Windows Launcher
echo ================================================================================

:: Check for Java compiler
where javac >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'javac' is not found in your system PATH.
    echo Please ensure JDK 17 or higher is installed and added to PATH.
    pause
    exit /b 1
)

:: Create output directory
if not exist out mkdir out

echo [INFO] Collecting source files...
dir /s /b src\*.java > sources.txt

echo [INFO] Compiling Core Java application...
javac -encoding UTF-8 -d out @sources.txt
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

echo [SUCCESS] Compilation successful!
echo [INFO] Launching Hospital Management System...
echo ================================================================================
java -Dfile.encoding=UTF-8 -cp out com.hms.Main

pause
