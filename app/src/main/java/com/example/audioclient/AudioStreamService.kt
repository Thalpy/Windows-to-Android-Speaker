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

class AudioStreamService : Service() {

    private var running = true
    private var socket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startAudioThread()
    }

    override fun onDestroy() {
        running = false
        socket?.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

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

    private fun startAudioThread() {
        thread {
            while (running) {
                try {
                    socket = Socket("127.0.0.1", 5000)
                    val input: InputStream = socket!!.getInputStream()

                    val bufferSize = AudioTrack.getMinBufferSize(
                        44100,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

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
                    Thread.sleep(2000)
                }
            }
        }
    }
}