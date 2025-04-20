@echo off
REM Setup ADB port forwarding for USB audio stream
adb forward tcp:5000 tcp:5000

REM pip install the required libraries if not already installed
pip install -r requirements.txt

REM Launch the Python-based audio streamer (make sure virtual audio cable is default output)
python WindowsServer.py

pause
