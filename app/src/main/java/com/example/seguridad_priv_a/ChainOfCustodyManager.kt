package com.example.seguridad_priv_a

data class Evidence(val id: String, val description: String, val timestamp: Long, var custodian: String)

class ChainOfCustodyManager {

    private val evidenceList = mutableListOf<Evidence>()

    // Agregar evidencia
    fun addEvidence(id: String, description: String, timestamp: Long, custodian: String) {
        val evidence = Evidence(id, description, timestamp, custodian)
        evidenceList.add(evidence)
        logCustody("EVIDENCE_ADDED", evidence)
    }

    // Transferir evidencia a otro custodio
    fun transferEvidence(id: String, newCustodian: String) {
        val evidence = evidenceList.find { it.id == id }
        evidence?.let {
            it.custodian = newCustodian
            logCustody("EVIDENCE_TRANSFERRED", it)
        }
    }

    // Obtener evidencia
    fun getEvidence(id: String): Evidence? {
        return evidenceList.find { it.id == id }
    }

    // Log de acciones sobre las evidencias
    private fun logCustody(action: String, evidence: Evidence) {
        println("$action: ${evidence.id} - ${evidence.description} - Custodian: ${evidence.custodian}")
    }
}
