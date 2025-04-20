package com.example.audioclient

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class StopServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("StopServiceReceiver", "Stop button pressed in notification")

        val stopIntent = Intent(context, AudioStreamService::class.java)
        val stopped = context.stopService(stopIntent)

        Log.d("StopServiceReceiver", "Service stop request result: $stopped")

        // Optional: Notify user for confirmation
        Toast.makeText(context, "Audio stream stopped", Toast.LENGTH_SHORT).show()
    }
}
