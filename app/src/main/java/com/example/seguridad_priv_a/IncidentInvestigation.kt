package com.example.seguridad_priv_a

class IncidentInvestigation {

    fun analyzeIncident(evidenceId: String) {
        // Buscar evidencia relacionada con el incidente
        val evidence = ChainOfCustodyManager().getEvidence(evidenceId)
        evidence?.let {
            println("Analizando incidente con evidencia: ${it.id}")
            // Verificar si hubo transferencia no autorizada o si la evidencia ha sido manipulada
            val isTampered = !Blockchain().validateChain()
            if (isTampered) {
                println("La evidencia ha sido manipulada.")
            } else {
                println("Evidencia intacta, no se detectaron manipulaciones.")
            }
        } ?: run {
            println("Evidencia no encontrada.")
        }
    }
}
