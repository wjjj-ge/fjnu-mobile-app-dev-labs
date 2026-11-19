@echo off
setlocal
cd /d "%~dp0"

echo [1/3] 停止旧的 Gradle 守护进程...
call gradlew.bat --stop

echo [2/3] 编译 Debug 包...
call gradlew.bat assembleDebug --console=plain
if errorlevel 1 (
    echo 编译失败，请查看上面的报错。
    exit /b 1
)

echo [3/3] 安装并启动...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo 安装失败，检查模拟器/真机是否已连接。
    exit /b 1
)

adb shell am force-stop com.example.midtermnotepad
adb shell am start -n com.example.midtermnotepad/.NotesListActivity

echo 完成。
endlocal
