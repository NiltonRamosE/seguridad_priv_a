package com.example.seguridad_priv_a

import java.security.MessageDigest
import java.util.*

data class Block(val index: Int, val previousHash: String, val timestamp: Long, val data: String, val hash: String)

class Blockchain {

    private val chain = mutableListOf<Block>()
    private var currentIndex = 0

    init {
        // Crear el bloque génesis
        addBlock("Blockchain initialized")
    }

    private fun addBlock(data: String) {
        val timestamp = System.currentTimeMillis()
        val previousHash = if (chain.isEmpty()) "0" else chain.last().hash
        val hash = generateHash(timestamp, previousHash, data)

        val block = Block(currentIndex, previousHash, timestamp, data, hash)
        chain.add(block)
        currentIndex++
    }

    private fun generateHash(timestamp: Long, previousHash: String, data: String): String {
        val content = "$timestamp$previousHash$data"
        return hashString(content)
    }

    private fun hashString(content: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun addLogEntry(data: String) {
        addBlock(data)
    }

    fun validateChain(): Boolean {
        for (i in 1 until chain.size) {
            val currentBlock = chain[i]
            val previousBlock = chain[i - 1]

            if (currentBlock.hash != generateHash(currentBlock.timestamp, previousBlock.hash, currentBlock.data)) {
                return false // El hash del bloque no coincide, se ha alterado
            }
        }
        return true
    }
}
