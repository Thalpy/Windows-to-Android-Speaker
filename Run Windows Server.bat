@echo off
adb start-server
adb devices
for /f "tokens=2" %%i in ('adb devices ^| findstr /r "^.*device$"') do set device=%%i

if \"%device%\"==\"\" (
    echo No device detected. Please connect your Android device via USB.
    pause
    exit /b
)

adb forward tcp:5001 tcp:5001
echo Port forwarded. Launching server...

pip install -r requirements.txt
python WindowsServer.py
pause
