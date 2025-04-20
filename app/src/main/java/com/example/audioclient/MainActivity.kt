package com.example.audioclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity


class MainActivity : AppCompatActivity() {

    private lateinit var toggleButton: Button
    private var isStreaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toggleButton = Button(this).apply {
            text = "Start Audio Stream"
            setOnClickListener {
                val intent = Intent(this@MainActivity, AudioStreamService::class.java)
                Log.d("MainActivity", "Start button pressed. isStreaming=$isStreaming")

                if (isStreaming) {
                    stopService(intent)
                    text = "Start Audio Stream"
                } else {
                    ContextCompat.startForegroundService(this@MainActivity, intent)
                    text = "Stop Audio Stream"
                }
                isStreaming = !isStreaming
            }
        }
        val statusText = TextView(this).apply {
            text = "Not streaming"
            textSize = 18f
            setPadding(0, 50, 0, 0)
        }

        AudioStreamService.statusCallback = {
            runOnUiThread {
                statusText.text = if (it) "Streaming audio from PC" else "Not streaming"
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(toggleButton)
            addView(statusText)
        }

        setContentView(layout)

    }

}