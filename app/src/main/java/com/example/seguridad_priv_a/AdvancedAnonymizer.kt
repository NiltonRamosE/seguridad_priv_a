package com.example.seguridad_priv_a

import kotlin.random.Random
import java.util.*

data class PersonalData(val name: String, val age: Int, val city: String)
data class NumericData(val value: Double)
data class AnonymizedData(val anonymizedName: String, val anonymizedAge: Int, val anonymizedCity: String)

enum class MaskingPolicy {
    FULL_MASK,
    PARTIAL_MASK,
    REPLACE_WITH_X
}

class AdvancedAnonymizer {

    /**
     * Implementación de k-anonimity: Grupos de k registros con características similares.
     */
    fun anonymizeWithKAnonymity(data: List<PersonalData>, k: Int): List<AnonymizedData> {
        val groupedData = data.groupBy { it.name to it.city }
        val anonymizedList = mutableListOf<AnonymizedData>()

        groupedData.forEach { (_, group) ->
            // Si el grupo tiene al menos k elementos, podemos anonimizarlo
            if (group.size >= k) {
                val anonymized = group.first()
                anonymizedList.add(
                    AnonymizedData(
                        anonymizedName = anonymizeName(anonymized.name),
                        anonymizedAge = anonymized.age,
                        anonymizedCity = anonymizeCity(anonymized.city)
                    )
                )
            }
        }
        return anonymizedList
    }

    /**
     * Función auxiliar para anonimizar nombres.
     */
    private fun anonymizeName(name: String): String {
        return name.replace(Regex("[A-Za-z]"), "*")  // Reemplazar con asteriscos
    }

    /**
     * Función auxiliar para anonimizar ciudades.
     */
    private fun anonymizeCity(city: String): String {
        return city.take(2) + "***"  // Anonimiza la ciudad dejando solo los primeros dos caracteres
    }

    /**
     * Aplicación de Differential Privacy para datos numéricos.
     */
    fun applyDifferentialPrivacy(data: NumericData, epsilon: Double): NumericData {
        // Agregar ruido aleatorio a los datos con base en el valor de epsilon
        val noise = Random.nextDouble(-epsilon, epsilon)  // Ruido gaussiano o Laplaciano podría ser más complejo
        return NumericData(data.value + noise)
    }

    /**
     * Técnicas de data masking según el tipo de dato.
     * Se puede aplicar masking completo, parcial, o reemplazo con un valor estándar (por ejemplo, X).
     */
    fun maskByDataType(data: Any, maskingPolicy: MaskingPolicy): Any {
        return when (data) {
            is String -> applyStringMasking(data, maskingPolicy)
            is Int -> applyIntegerMasking(data, maskingPolicy)
            is Double -> applyDoubleMasking(data, maskingPolicy)
            else -> data
        }
    }

    /**
     * Enmascaramiento de cadenas (por ejemplo, nombres o direcciones).
     */
    private fun applyStringMasking(data: String, maskingPolicy: MaskingPolicy): String {
        return when (maskingPolicy) {
            MaskingPolicy.FULL_MASK -> "*****"  // Reemplaza toda la cadena
            MaskingPolicy.PARTIAL_MASK -> data.take(3) + "***"  // Enmascara parcialmente
            MaskingPolicy.REPLACE_WITH_X -> "X".repeat(data.length)  // Reemplaza con X
        }
    }

    /**
     * Enmascaramiento de enteros.
     */
    private fun applyIntegerMasking(data: Int, maskingPolicy: MaskingPolicy): Int {
        return when (maskingPolicy) {
            MaskingPolicy.FULL_MASK -> -1  // Valor por defecto (indicado como enmascarado)
            MaskingPolicy.PARTIAL_MASK -> data / 10 * 10  // Enmascara parcialmente dejando solo la decena
            MaskingPolicy.REPLACE_WITH_X -> -1  // Reemplazo por valor fijo
        }
    }

    /**
     * Enmascaramiento de datos numéricos.
     */
    private fun applyDoubleMasking(data: Double, maskingPolicy: MaskingPolicy): Double {
        return when (maskingPolicy) {
            MaskingPolicy.FULL_MASK -> -1.0  // Indica que el dato ha sido enmascarado
            MaskingPolicy.PARTIAL_MASK -> data * 0.5  // Enmascara reduciendo el valor
            MaskingPolicy.REPLACE_WITH_X -> -1.0  // Reemplaza con valor ficticio
        }
    }

    /**
     * Políticas de retención configurables:
     * Este método puede ser utilizado para determinar cuándo eliminar datos de acuerdo con la configuración
     * de retención definida por el usuario.
     */
    fun applyRetentionPolicy(data: PersonalData, retentionPeriodInDays: Int, currentDate: Date): Boolean {
        // Supongamos que guardamos una fecha de creación en cada registro de datos.
        // Aquí implementamos la lógica para determinar si el dato debe eliminarse.
        val retentionThresholdDate = Date(currentDate.time - (retentionPeriodInDays * 24 * 60 * 60 * 1000))  // Retención en días

        return currentDate.after(retentionThresholdDate)  // Si los datos no han pasado el periodo de retención, se mantienen
    }
}
