package com.example.audioclient

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.*
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.net.Socket

class AudioStreamService : Service() {
    private val CHANNEL_ID = "AudioStreamingChannel"
    private val NOTIFICATION_ID = 1
    private var socketThread: Thread? = null
    private var reconnectAttempts = 0

    companion object {
        var isStreamingLive: Boolean = false
        var statusCallback: ((Boolean) -> Unit)? = null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AudioStreamService", "onStartCommand triggered")
        startForeground(NOTIFICATION_ID, buildNotification())
        startStreamingThread()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, StopServiceReceiver::class.java).apply {
            action = "com.example.audioclient.STOP_SERVICE"
        }
        val pendingStopIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audio Streaming Active")
            .setContentText("Streaming audio from PC")
            .setSmallIcon(R.drawable.notif_icon)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.notif_icon, "Stop", pendingStopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Audio Streaming Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for audio streaming service"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun startStreamingThread() {
        socketThread?.interrupt()
        socketThread = Thread {
            var audioTrack: AudioTrack? = null
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(1000) // Prevent racing the PC server boot
                    Log.d("AudioStreamService", "Attempting to connect...")
                    val socket = Socket("127.0.0.1", 5001)
                    Log.d("AudioStreamService", "Connected to server")
                    val input: InputStream = socket.getInputStream()

                    val bufferSize = AudioTrack.getMinBufferSize(
                        44100,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    audioTrack = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        44100,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    )
                    audioTrack.play()

                    isStreamingLive = true
                    statusCallback?.invoke(true)

                    val buffer = ByteArray(4096)
                    while (!Thread.currentThread().isInterrupted) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        audioTrack.write(buffer, 0, read)
                    }

                    isStreamingLive = false
                    statusCallback?.invoke(false)

                    audioTrack.stop()
                    audioTrack.release()
                    socket.close()
                    break

                } catch (e: Exception) {
                    Log.e("AudioStreamService", "Error: ${e.message}", e)
                    isStreamingLive = false
                    statusCallback?.invoke(false)
                    Thread.sleep(2000L.coerceAtMost(1000L * (reconnectAttempts + 1)))
                    reconnectAttempts++
                }
            }
        }
        socketThread?.start()
    }

    override fun onDestroy() {
        socketThread?.interrupt()
        isStreamingLive = false
        statusCallback?.invoke(false)
        super.onDestroy()
    }
}
