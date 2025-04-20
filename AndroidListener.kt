package com.example.audioclient

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.InputStream
import java.net.Socket
import kotlin.concurrent.thread

// Foreground service that receives PCM audio data over a TCP socket (via ADB forward)
// and plays it in real-time using AudioTrack.
class AudioStreamService : Service() {

    private var running = true
    private var socket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService() // Keeps the service alive with a notification
        startAudioThread()       // Starts streaming thread
    }

    override fun onDestroy() {
        running = false
        socket?.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Setup and display foreground notification for Android 8+
    private fun startForegroundService() {
        val channelId = "audio_stream_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Audio Streaming",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("USB Audio Streaming")
            .setContentText("Streaming from PC...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)
    }

    // Establishes socket connection and plays audio from incoming stream using AudioTrack
    private fun startAudioThread() {
        thread {
            while (running) {
                try {
                    socket = Socket("127.0.0.1", 5000) // Connects to local port forwarded by ADB
                    val input: InputStream = socket!!.getInputStream()

                    val bufferSize = AudioTrack.getMinBufferSize(
                        44100,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                    // Setup low-latency AudioTrack in streaming mode
                    val audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(44100)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    audioTrack.play()

                    val buffer = ByteArray(4096)
                    while (running && input.read(buffer) != -1) {
                        audioTrack.write(buffer, 0, buffer.size)
                    }

                    audioTrack.stop()
                    audioTrack.release()
                    input.close()
                    socket?.close()
                } catch (e: Exception) {
                    Thread.sleep(2000) // Retry connection every 2 seconds
                }
            }
        }
    }
} 

// Main activity that starts the audio streaming service on button click
package com.example.audioclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple UI with one button to start streaming service
        val button = Button(this).apply {
            text = "Start Audio Stream"
            setOnClickListener {
                val intent = Intent(this@MainActivity, AudioStreamService::class.java)
                startService(intent)
            }
        }

        setContentView(button)
    }
}
