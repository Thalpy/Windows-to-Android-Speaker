# Windows-side Python script to stream raw PCM audio over TCP (via ADB-forwarded port) (https://vb-audio.com/Cable/)
# Requires: pip install pyaudio - see the requirements.txt file 

import socket
import pyaudio
import time
import signal
import sys

# Audio capture configuration
CHUNK = 1024
FORMAT = pyaudio.paInt16
CHANNELS = 2
RATE = 44100
# Server port for TCP connection
TCP_HOST = '0.0.0.0'
TCP_PORT = 5001

running = True

def signal_handler(sig, frame):
    global running
    print("\n[INFO] Termination signal received. Shutting down...")
    running = False

signal.signal(signal.SIGINT, signal_handler)

p = pyaudio.PyAudio()

stream = p.open(format=FORMAT,
                channels=CHANNELS,
                rate=RATE,
                input=True,
                frames_per_buffer=CHUNK)

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server_socket.bind((TCP_HOST, TCP_PORT))
server_socket.listen(1)

print(f"[INFO] Waiting for Android device to connect via TCP (port {TCP_PORT})...")

while running:
    try:
        server_socket.settimeout(1.0)
        try:
            client_socket, addr = server_socket.accept()
        except socket.timeout:
            continue

        print("[INFO] Connected to Android device. Streaming audio...")

        while running:
            try:
                data = stream.read(CHUNK, exception_on_overflow=False)
                client_socket.sendall(data)
            except (BrokenPipeError, ConnectionResetError):
                print("[WARN] Android disconnected unexpectedly.")
                break
            except Exception as e:
                print(f"[ERROR] Unexpected send error: {e}")
                break

        client_socket.close()
        print("[INFO] Closed connection. Waiting for reconnect...")

    except Exception as e:
        print(f"[ERROR] Top-level server exception: {e}")

stream.stop_stream()
stream.close()
p.terminate()
server_socket.close()
print("[INFO] Server shut down.")
