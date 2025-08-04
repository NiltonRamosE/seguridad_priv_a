package com.example.seguridad_priv_a.data

import android.content.Context
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject

class SecurityAuditManager(private val context: Context) {

    private val requestLog = ConcurrentHashMap<String, MutableList<Long>>()
    private val RATE_LIMIT_WINDOW_MS = 60_000L // 1 minuto
    private val MAX_REQUESTS_PER_WINDOW = 5 // Máximo de 5 solicitudes por minuto
    private val ALERT_THRESHOLD = 3 // Número de intentos fallidos para generar alerta

    // Simularemos una clave privada para firmar los logs
    private val privateKey: PrivateKey = generatePrivateKey()

    // Método para detectar múltiples solicitudes en un corto período de tiempo
    fun detectSuspiciousActivity(userId: String): Boolean {
        val currentTime = System.currentTimeMillis()

        // Registra la solicitud actual
        requestLog.putIfAbsent(userId, mutableListOf())
        requestLog[userId]?.add(currentTime)

        // Eliminar las solicitudes anteriores a la ventana de rate limiting
        requestLog[userId]?.removeIf { it < currentTime - RATE_LIMIT_WINDOW_MS }

        // Verifica si se excede el límite de solicitudes
        if (requestLog[userId]?.size ?: 0 > MAX_REQUESTS_PER_WINDOW) {
            generateAlert(userId, "Rate limit exceeded")
            return true
        }
        return false
    }

    // Método para detectar patrones anómalos y generar alertas
    fun generateAlert(userId: String, alertType: String) {
        val alertMessage = "Alerta: $alertType detectada para el usuario $userId"
        logAccess("ALERT", alertMessage)

        // Aquí se puede agregar una lógica para enviar una alerta real, como un correo o un SMS
        println("ALERTA: $alertMessage")
    }

    // Método para exportar logs en formato JSON firmado digitalmente
    fun exportLogs(logs: List<String>): String {
        val logsJson = JSONObject().apply {
            put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("logs", logs)
        }

        // Firmar los logs JSON
        val signature = signData(logsJson.toString())
        logsJson.put("signature", signature)

        return logsJson.toString()
    }

    // Método para firmar los logs usando una clave privada
    private fun signData(data: String): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray())
        return signature.sign().joinToString("") { "%02x".format(it) }
    }

    // Método para generar una clave privada RSA para la firma digital (usado solo para ejemplo)
    private fun generatePrivateKey(): PrivateKey {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)
        return keyPairGen.genKeyPair().private
    }

    // Método para registrar accesos y eventos
    fun logAccess(category: String, action: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$timestamp - $category: $action"

        // Simular almacenamiento de logs en un archivo o base de datos
        println("Log registrado: $logEntry")
    }

    // Método para verificar patrones anómalos (en este caso múltiples intentos fallidos)
    fun detectFailedLoginAttempts(userId: String, success: Boolean) {
        if (!success) {
            val failedAttempts = getFailedAttempts(userId)
            if (failedAttempts >= ALERT_THRESHOLD) {
                generateAlert(userId, "Multiple failed login attempts")
            }
        }
    }

    // Método para obtener el número de intentos fallidos del usuario
    private fun getFailedAttempts(userId: String): Int {
        // Este método debería acceder a un registro de intentos fallidos, aquí lo simulamos
        // Simplemente incrementamos el contador cada vez que se registre un intento fallido
        // y lo almacenamos en el mapa de log
        return 3  // Simulamos que el usuario tiene 3 intentos fallidos
    }
}
