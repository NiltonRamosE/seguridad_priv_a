package com.example.seguridad_priv_a

import android.os.Build
import android.util.Log
import android.content.Context
import android.os.Debug
import android.widget.Toast

class AntiTamperingManager(private val context: Context) {

    fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    fun isEmulator(): Boolean {
        val isEmulator = (Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86"))
        if (isEmulator) {
            Toast.makeText(context, "Emulador detectado", Toast.LENGTH_SHORT).show()
        }
        return isEmulator
    }

    // Llamar a estos métodos en tu aplicación
    fun checkForTampering(): Boolean {
        if (isDebuggerConnected()) {
            Log.e("AntiTampering", "Debugging detectado!")
            Toast.makeText(context, "Debugging detectado, la aplicación no funcionará.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (isEmulator()) {
            Log.e("AntiTampering", "Emulador detectado!")
            Toast.makeText(context, "Emulador detectado, la aplicación no funcionará.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
