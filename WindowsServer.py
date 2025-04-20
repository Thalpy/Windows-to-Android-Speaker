# Windows-side Python script to stream raw PCM audio over TCP (via ADB-forwarded port) (https://vb-audio.com/Cable/)
# Requires: pip install pyaudio - see the requirements.txt file 

import socket
import pyaudio
import threading # For future use, if needed
import time

# Audio capture configuration
SAMPLE_RATE = 44100
CHANNELS = 2
FORMAT = pyaudio.paInt16
CHUNK = 1024
TCP_HOST = '127.0.0.1'
TCP_PORT = 5000

# Initialize PyAudio
p = pyaudio.PyAudio()
stream = p.open(format=FORMAT,
                channels=CHANNELS,
                rate=SAMPLE_RATE,
                input=True,
                frames_per_buffer=CHUNK)

print("[INFO] Waiting for Android device to connect via TCP (port 5000)...")

while True:
    try:
        # Attempt to connect to Android device
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((TCP_HOST, TCP_PORT))
        print("[INFO] Connected to Android device. Streaming audio...")

        while True:
            data = stream.read(CHUNK, exception_on_overflow=False)
            client_socket.sendall(data)

    except (ConnectionAbortedError, ConnectionResetError, BrokenPipeError, socket.error) as e:
        print(f"[WARN] Connection lost or refused: {e}. Retrying in 2 seconds...")
        time.sleep(2)
    finally:
        try:
            client_socket.close()
        except:
            pass
