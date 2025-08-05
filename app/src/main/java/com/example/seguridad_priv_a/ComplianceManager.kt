package com.example.seguridad_priv_a

import java.text.SimpleDateFormat
import java.util.*

data class UserData(val userId: String, val consentGiven: Boolean, val dataCollected: String, val dataRetentionPeriod: Int)

class ComplianceManager {

    private val users = mutableListOf<UserData>()

    // Registrar consentimiento
    fun registerUserConsent(userId: String, consentGiven: Boolean) {
        val user = users.find { it.userId == userId } ?: return
        generateComplianceReport(user)
    }

    // Eliminar datos personales si el usuario lo solicita
    fun deleteUserData(userId: String) {
        val user = users.find { it.userId == userId } ?: return
        users.remove(user)
        generateComplianceReport(user, action = "Data Deletion")
    }

    // Generar reporte de compliance
    fun generateComplianceReport(user: UserData, action: String = "Consent Registration") {
        val report = """
            Compliance Report:
            - User ID: ${user.userId}
            - Action: $action
            - Consent Given: ${user.consentGiven}
            - Data Collected: ${user.dataCollected}
            - Retention Period: ${user.dataRetentionPeriod} days
            - Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}
        """.trimIndent()
        println(report)
    }
}
