@echo off
rem Build + install + launch lab1 app. Run by double-click, or from a normal terminal.
rem Do NOT run inside Android Studio's terminal (IDE child processes are blocked by antivirus on some machines).
setlocal
cd /d "%~dp0"

set "ADB="
if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if not defined ADB if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
if not defined ADB if exist "D:\Android\Sdk\platform-tools\adb.exe" set "ADB=D:\Android\Sdk\platform-tools\adb.exe"
if not defined ADB set "ADB=adb"

echo === 1/3 device ===
"%ADB%" devices
echo.
echo === 2/3 build + install ===
rem Stop the running Gradle daemon first: a daemon born inside Android Studio gets its adb/aapt2 children blocked
call gradlew.bat --stop >nul 2>nul
call gradlew.bat installDebug --console=plain
if errorlevel 1 goto fail
echo.
echo === 3/3 restart app ===
"%ADB%" shell am force-stop com.example.firstlab
"%ADB%" shell am start -n com.example.firstlab/.MainActivity
ping -n 3 127.0.0.1 >nul
echo.
echo App PID:
"%ADB%" shell pidof com.example.firstlab
echo.
echo Done. Logs: "%ADB%" logcat -s Lab1Lifecycle
goto end
:fail
echo BUILD FAILED - see messages above.
:end
echo.
pause
