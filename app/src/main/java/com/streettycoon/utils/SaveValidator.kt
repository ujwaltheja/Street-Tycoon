package com.streettycoon.utils

import org.json.JSONObject
import java.security.MessageDigest

/**
 * Utility for validating game save files
 * Implements checksum validation and JSON structure verification
 */
object SaveValidator {

    /**
     * Calculate SHA-256 checksum for a JSON string
     */
    fun calculateChecksum(json: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(json.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Validate checksum matches the stored checksum
     */
    fun validateChecksum(json: String, checksum: String): Boolean {
        return calculateChecksum(json) == checksum
    }

    /**
     * Validate JSON structure has all required fields
     * This prevents loading corrupted or incomplete save files
     */
    fun validateJsonStructure(json: String): Boolean {
        return try {
            val obj = JSONObject(json)
            // Verify required fields exist
            obj.has("playerCash") &&
            obj.has("currentDay") &&
            obj.has("zones") &&
            obj.has("stalls") &&
            obj.has("helpers") &&
            obj.has("upgrades") &&
            obj.has("family")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate snapshot is complete and not corrupted
     */
    fun validateSnapshot(snapshotJson: String, checksum: String): Boolean {
        return validateChecksum(snapshotJson, checksum) &&
               validateJsonStructure(snapshotJson)
    }
}
