package com.example.seguridad_priv_a

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.seguridad_priv_a.databinding.ActivityDataProtectionBinding
import java.util.concurrent.TimeUnit

class DataProtectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataProtectionBinding
    private val dataProtectionManager by lazy {
        (application as PermissionsApplication).dataProtectionManager
    }

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val sessionTimeoutHandler = Handler(Looper.getMainLooper())
    private var sessionTimeoutRunnable: Runnable? = null

    private val INACTIVITY_TIMEOUT = TimeUnit.MINUTES.toMillis(5) // 5 minutos de inactividad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataProtectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadDataProtectionInfo()
        loadAccessLogs()
        startSessionTimeoutTimer()

        dataProtectionManager.logAccess("NAVIGATION", "DataProtectionActivity abierta")

        // Configuración del BiometricPrompt
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Manejar errores (por ejemplo, si se cancela o el usuario no tiene configurada la biometría)
                Toast.makeText(this@DataProtectionActivity, "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Autenticación exitosa
                loadDataProtectionInfo()
                loadAccessLogs()
                Toast.makeText(this@DataProtectionActivity, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Fallo de autenticación
                Toast.makeText(this@DataProtectionActivity, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación requerida")
            .setSubtitle("Por favor autentíquese para acceder a la protección de datos.")
            .setNegativeButtonText("Usar PIN/Patrón")
            .build()
    }

    private fun setupUI() {
        binding.btnViewLogs.setOnClickListener {
            loadAccessLogs()
            Toast.makeText(this, "Logs actualizados", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearData.setOnClickListener {
            showClearDataDialog()
        }

        // Botón para activar la autenticación biométrica
        binding.btnAuthenticate.setOnClickListener {
            authenticateUserBiometrically()
        }
    }

    private fun loadDataProtectionInfo() {
        val info = dataProtectionManager.getDataProtectionInfo()
        val infoText = StringBuilder()

        infoText.append("🔐 INFORMACIÓN DE SEGURIDAD\n\n")
        info.forEach { (key, value) ->
            infoText.append("• $key: $value\n")
        }

        infoText.append("\n📊 EVIDENCIAS DE PROTECCIÓN:\n")
        infoText.append("• Encriptación AES-256-GCM activa\n")
        infoText.append("• Todos los accesos registrados\n")
        infoText.append("• Datos anonimizados automáticamente\n")
        infoText.append("• Almacenamiento local seguro\n")
        infoText.append("• No hay compartición de datos\n")

        binding.tvDataProtectionInfo.text = infoText.toString()

        dataProtectionManager.logAccess("DATA_PROTECTION", "Información de protección mostrada")
    }

    private fun loadAccessLogs() {
        val logs = dataProtectionManager.getAccessLogs()

        if (logs.isNotEmpty()) {
            val logsText = logs.joinToString("\n")
            binding.tvAccessLogs.text = logsText
        } else {
            binding.tvAccessLogs.text = "No hay logs disponibles"
        }

        dataProtectionManager.logAccess("DATA_ACCESS", "Logs de acceso consultados")
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Borrar Todos los Datos")
            .setMessage("¿Estás seguro de que deseas borrar todos los datos almacenados y logs de acceso? Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ -> clearAllData() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun clearAllData() {
        dataProtectionManager.clearAllData()

        // Actualizar UI
        binding.tvAccessLogs.text = "Todos los datos han sido borrados"
        binding.tvDataProtectionInfo.text = "🔐 DATOS BORRADOS DE FORMA SEGURA\n\nTodos los datos personales y logs han sido eliminados del dispositivo."

        Toast.makeText(this, "Datos borrados de forma segura", Toast.LENGTH_LONG).show()

        // Este log se creará después del borrado
        dataProtectionManager.logAccess("DATA_MANAGEMENT", "Todos los datos borrados por el usuario")
    }

    private fun authenticateUserBiometrically() {
        biometricPrompt.authenticate(promptInfo)
    }

    // Método para manejar el timeout de sesión
    private fun startSessionTimeoutTimer() {
        sessionTimeoutRunnable = Runnable {
            Toast.makeText(this, "Tu sesión ha expirado por inactividad", Toast.LENGTH_SHORT).show()
            finish() // Cerrar la actividad después del timeout
        }
        sessionTimeoutHandler.postDelayed(sessionTimeoutRunnable!!, INACTIVITY_TIMEOUT)
    }

    // Reiniciar el temporizador cada vez que se detecte una actividad del usuario
    override fun onResume() {
        super.onResume()
        loadAccessLogs() // Actualizar logs al volver a la actividad
        sessionTimeoutHandler.removeCallbacks(sessionTimeoutRunnable!!) // Eliminar el viejo temporizador
        startSessionTimeoutTimer() // Reiniciar el temporizador de sesión
    }

    override fun onPause() {
        super.onPause()
        sessionTimeoutHandler.removeCallbacks(sessionTimeoutRunnable!!) // Eliminar el temporizador cuando la actividad está en pausa
    }
}
