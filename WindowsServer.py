# Windows-side Python script to stream raw PCM audio over TCP (via ADB-forwarded port)
# Requires: pip install pyaudio - see the requirements.txt file 

import socket
import pyaudio
import threading # For future use, if needed

# Audio capture configuration
SAMPLE_RATE = 44100
CHANNELS = 2
FORMAT = pyaudio.paInt16
CHUNK = 1024
TCP_HOST = '127.0.0.1'
TCP_PORT = 5000

# Start TCP socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((TCP_HOST, TCP_PORT))

# Initialize PyAudio
p = pyaudio.PyAudio()
stream = p.open(format=FORMAT,
                channels=CHANNELS,
                rate=SAMPLE_RATE,
                input=True,
                frames_per_buffer=CHUNK)

print("[INFO] Streaming audio to Android device via TCP (port 5000)...")

try:
    while True:
        data = stream.read(CHUNK, exception_on_overflow=False)
        client_socket.sendall(data)
except KeyboardInterrupt:
    print("[INFO] Stopping stream...")
finally:
    stream.stop_stream()
    stream.close()
    p.terminate()
    client_socket.close()
