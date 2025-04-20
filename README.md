# USB Audio Streaming from Windows to Android

This project allows streaming of uncompressed audio from a Windows machine to an Android phone via USB-C using a simple TCP connection. With the Android device acting as a playback endpoint, it's useful for scenarios where direct cable-based audio output is needed, presumably for higher clarity, stability, and low-latency performance.

I personally use it to stream audio from my PC to an Android phone, which I then connect to a Bluetooth speaker or headphones. Windows bluetooth drivers are notoriously unreliable, and this setup allows me to bypass them entirely. The Android device acts as a USB audio sink, receiving the audio stream over a TCP connection and playing it back using the native AudioTrack API.

The approach is lightweight and avoids heavy protocol overhead, making it a practical option for anyone interested in routing desktop audio directly to mobile devices over a USB connection. 

---

## Requirements

### Windows
- Python 3.9–3.11
- [VB-Audio Virtual Cable](https://vb-audio.com/Cable/) (to capture system audio output)
- Windows

### Android
- Android 6.0+ device
- USB-C cable with developer mode enabled
- Android Studio (to build the app if installing manually)

---

## Windows Setup

1. Install [VB-Audio Virtual Cable](https://vb-audio.com/Cable/).
2. Set "CABLE Input (VB-Audio Virtual Cable)" as your system’s default audio output.
3. Install Python dependencies:
   ```bash
   pip install pyaudio
   ```
   or
    ```bash
    pip install -r requirements.txt
    ```
4. Connect your Android phone via USB.
5. Forward a local port to the Android device:
   ```bash
   adb forward tcp:5001 tcp:5001
   ```
6. Start the Python streaming script:
   ```bash
   python stream_to_android.py
   ```
   Or use the `run.bat` script for convenience (step 3 onwards).

---

## Android Setup

### Option 1: Use the Provided Android App
1. Open the Android Studio project (`android_audio_client`).
2. Build and install the app on your Android device.
3. Tap "Start Audio Stream" to begin playback.

What the app does:
- Connects to the localhost port forwarded over USB
- Reads raw PCM data and plays it using the low-latency AudioTrack API
- Handles dropouts and attempts automatic reconnection
- Stays active with a foreground service to maintain consistent playback

### Option 2: Manual APK Installation
- Export an APK from Android Studio.
- Install it using:
  ```bash
  adb install your_app.apk
  ```

---

## Notes
- The stream uses raw PCM at 44.1kHz, 16-bit stereo.
- Practical end-to-end latency is typically below 20 milliseconds.
- Works well for real-time playback, testing audio setups, or repurposing older Android phones as external audio output devices.
- Modular and adaptable to add DSP, filters, or audio analysis features.

---

## Troubleshooting
| Issue            | Solution                                                                                                 |
| ---------------- | -------------------------------------------------------------------------------------------------------- |
| No audio         | Ensure VB-Audio Cable is active and selected, and that the Android app is running                        |
| Audio lag        | Try reducing buffer sizes in the Python script and Android AudioTrack config                             |
| ADB not detected | Install or update [Android platform tools](https://developer.android.com/studio/releases/platform-tools) |
| Connection loss  | Check cable, USB mode, and ensure the app is set to reconnect automatically                              |
| Python not found | Ensure Python is added to your system PATH or use the full path to the Python executable in the script   |

## Credits / Appreciation
If you like this, go check out [whimsylabs.ai](https://whimsylabs.ai) or support their patreon at [patreon.com/whimsylabs](https://www.patreon.com/whimsylabs). (I make whimsylabs)
