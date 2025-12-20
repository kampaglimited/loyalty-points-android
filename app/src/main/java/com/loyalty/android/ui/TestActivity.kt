package com.loyalty.android.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import android.util.Log

/**
 * An empty activity used for hosting isolated Compose tests.
 * Includes window flags to prevent the screen from locking or turning off
 * during automated test execution.
 */
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TestActivity", "onCreate: Launching TestActivity surface")
        
        // Keep screen on, turn on, and dismiss keyguard for more reliable UI testing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
    }
}
