package com.example.audioclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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