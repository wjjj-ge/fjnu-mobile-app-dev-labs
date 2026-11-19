@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

REM 1) locate adb (PATH first, then common SDK locations)
set "ADB=adb"
where adb >nul 2>&1
if errorlevel 1 (
  set "ADB="
  if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
  if not defined ADB if exist "D:\Android\Sdk\platform-tools\adb.exe" set "ADB=D:\Android\Sdk\platform-tools\adb.exe"
  if not defined ADB if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
  if not defined ADB if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
)
if not defined ADB (
  echo [ERROR] adb not found. Add platform-tools to PATH, or set ANDROID_HOME.
  exit /b 1
)

echo [0/4] adb = %ADB%
"%ADB%" devices
echo.

echo [1/4] stopping gradle daemon ...
call gradlew.bat --stop

echo [2/4] building debug apk ...
call gradlew.bat assembleDebug --console=plain
if errorlevel 1 (
  echo [ERROR] BUILD FAILED. Read the errors above; the old apk was NOT replaced.
  exit /b 1
)

echo [3/4] installing ...
"%ADB%" install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
  echo [ERROR] install failed. Check that a device or emulator is listed above.
  exit /b 1
)

echo [4/4] launching ...
"%ADB%" shell am force-stop com.example.midtermnotepad
"%ADB%" shell am start -n com.example.midtermnotepad/.NotesListActivity

echo.
echo Done. If the list looks unchanged, check the apk timestamp:
echo   app\build\outputs\apk\debug\app-debug.apk
endlocal
