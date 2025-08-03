package com.example.seguridad_priv_a.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DataProtectionManager(private val context: Context) {

    private lateinit var encryptedPrefs: SharedPreferences
    private lateinit var accessLogPrefs: SharedPreferences

    private val LAST_ROTATION_DATE_KEY = "last_rotation_date"
    private val SALT_KEY = "user_salt_key"

    fun initialize() {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Crear SharedPreferences encriptado para datos sensibles
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // SharedPreferences para logs de acceso
            accessLogPrefs = context.getSharedPreferences("access_logs", Context.MODE_PRIVATE)

            // Verificar si es necesario rotar la clave
            rotateEncryptionKey()

            // Verificar la integridad de los datos
            verifyDataIntegrity("some_key") // Asegúrate de proporcionar la clave correcta en tu implementación

        } catch (e: Exception) {
            encryptedPrefs = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
            accessLogPrefs = context.getSharedPreferences("access_logs", Context.MODE_PRIVATE)
        }
    }

    fun storeSecureData(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
        logAccess("DATA_STORAGE", "Dato almacenado de forma segura: $key")
    }

    fun getSecureData(key: String): String? {
        val data = encryptedPrefs.getString(key, null)
        if (data != null) {
            logAccess("DATA_ACCESS", "Dato accedido: $key")
        }
        return data
    }

    fun logAccess(category: String, action: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$timestamp - $category: $action"

        val existingLogs = accessLogPrefs.getString("logs", "") ?: ""
        val newLogs = if (existingLogs.isEmpty()) logEntry else "$existingLogs\n$logEntry"

        accessLogPrefs.edit().putString("logs", newLogs).apply()

        val logLines = newLogs.split("\n")
        if (logLines.size > 100) {
            val trimmedLogs = logLines.takeLast(100).joinToString("\n")
            accessLogPrefs.edit().putString("logs", trimmedLogs).apply()
        }
    }

    fun getAccessLogs(): List<String> {
        val logsString = accessLogPrefs.getString("logs", "") ?: ""
        return if (logsString.isEmpty()) emptyList() else logsString.split("\n").reversed()
    }

    fun clearAllData() {
        encryptedPrefs.edit().clear().apply()
        accessLogPrefs.edit().clear().apply()
        logAccess("DATA_MANAGEMENT", "Todos los datos han sido borrados de forma segura")
    }

    fun getDataProtectionInfo(): Map<String, String> {
        return mapOf(
            "Encriptación" to "AES-256-GCM",
            "Almacenamiento" to "Local encriptado",
            "Logs de acceso" to "${getAccessLogs().size} entradas",
            "Última limpieza" to (getSecureData("last_cleanup") ?: "Nunca"),
            "Estado de seguridad" to "Activo"
        )
    }

    fun anonymizeData(data: String): String {
        return data.replace(Regex("[0-9]"), "*").replace(Regex("[A-Za-z]{3,}"), "***")
    }

    /**
     * 1. Rotación Automática de Claves Maestras Cada 30 Días
     */
    fun rotateEncryptionKey(): Boolean {
        val lastRotationDate = getSecureData(LAST_ROTATION_DATE_KEY)
        val currentDate = System.currentTimeMillis()

        if (lastRotationDate == null || currentDate - lastRotationDate.toLong() > 30L * 24 * 60 * 60 * 1000) {
            return try {
                val newMasterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                storeSecureData(LAST_ROTATION_DATE_KEY, currentDate.toString())

                encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs",
                    newMasterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                logAccess("KEY_ROTATION", "Clave maestra rotada exitosamente")
                true
            } catch (e: Exception) {
                logAccess("KEY_ROTATION", "Error al rotar la clave maestra: ${e.message}")
                false
            }
        }
        return false
    }

    /**
     * 2. Verificación de Integridad de Datos Encriptados Usando HMAC
     */
    fun verifyDataIntegrity(key: String): Boolean {
        val storedData = getSecureData(key)
        val storedHmac = getSecureData("${key}_hmac")

        if (storedData == null || storedHmac == null) {
            return false
        }

        return try {
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(getMasterKeyForHmac(), "HmacSHA256")
            hmac.init(secretKey)
            val calculatedHmac = hmac.doFinal(storedData.toByteArray())

            storedHmac == calculatedHmac.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            logAccess("DATA_INTEGRITY", "Error en la verificación de la integridad: ${e.message}")
            false
        }
    }

    private fun getMasterKeyForHmac(): ByteArray {
        val key = getSecureData("master_key_for_hmac")
        return key?.toByteArray() ?: ByteArray(32)  // Si no está disponible, usar un valor predeterminado
    }

    fun storeDataWithHmac(key: String, value: String) {
        storeSecureData(key, value)

        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(getMasterKeyForHmac(), "HmacSHA256")
        hmac.init(secretKey)
        val calculatedHmac = hmac.doFinal(value.toByteArray())

        storeSecureData("${key}_hmac", calculatedHmac.joinToString("") { "%02x".format(it) })
    }

    /**
     * 3. Implementación de Key Derivation con Salt Único por Usuario
     */
    fun deriveKeyForUser(username: String): ByteArray {
        val salt = getSaltForUser(username)

        val keySpec = PBEKeySpec(username.toCharArray(), salt, 10000, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return secretKeyFactory.generateSecret(keySpec).encoded
    }

    fun getSaltForUser(username: String): ByteArray {
        val saltString = getSecureData("$username$SALT_KEY") ?: generateSaltForUser(username)
        return saltString.toByteArray()
    }

    fun generateSaltForUser(username: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        storeSecureData("$username$SALT_KEY", salt.joinToString("") { "%02x".format(it) })
        return salt.joinToString("") { "%02x".format(it) }
    }
}
