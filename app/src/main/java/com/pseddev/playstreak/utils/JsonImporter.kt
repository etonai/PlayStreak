package com.pseddev.playstreak.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.models.*
import java.io.Reader
import java.text.SimpleDateFormat
import java.util.*

object JsonImporter {
    
    private val gson = Gson()
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    // Alternative date formats to support
    private val alternateDateFormatters = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    ).onEach { it.timeZone = TimeZone.getTimeZone("UTC") }
    
    /**
     * Validates JSON content before import
     */
    fun validateJson(reader: Reader, pieceLimit: Int, activityLimit: Int, achievementLimit: Int = 20): JsonValidationResult {
        Log.d("JsonImporter", "Starting JSON validation")
        
        try {
            val content = reader.readText()
            val exportData = gson.fromJson(content, ExportData::class.java)
            
            val errors = mutableListOf<String>()
            
            // Validate basic structure
            if (exportData.exportInfo.version.isBlank()) {
                errors.add("Missing or empty version in export info")
            }
            
            if (exportData.pieces.isEmpty() && exportData.activities.isEmpty()) {
                errors.add("Export contains no pieces or activities")
            }
            
            // Check limits
            if (exportData.pieces.size > pieceLimit) {
                errors.add("Export contains ${exportData.pieces.size} pieces, but limit is $pieceLimit")
            }
            
            if (exportData.activities.size > activityLimit) {
                errors.add("Export contains ${exportData.activities.size} activities, but limit is $activityLimit")
            }
            
            // Validate achievements (optional section)
            val achievementCount = exportData.achievements?.size ?: 0
            if (achievementCount > achievementLimit) {
                errors.add("Export contains $achievementCount achievements, but limit is $achievementLimit")
            }
            
            // Validate piece names for uniqueness and non-empty
            val pieceNames = mutableSetOf<String>()
            exportData.pieces.forEach { piece ->
                if (piece.name.isBlank()) {
                    errors.add("Piece with ID ${piece.id} has blank name")
                } else if (piece.name.lowercase() in pieceNames) {
                    errors.add("Duplicate piece name found: '${piece.name}'")
                } else {
                    pieceNames.add(piece.name.lowercase())
                }
            }
            
            // Validate relationships
            val pieceIds = exportData.pieces.map { it.id }.toSet()
            exportData.activities.forEach { activity ->
                if (activity.pieceId !in pieceIds) {
                    errors.add("Activity ${activity.id} references non-existent piece ID ${activity.pieceId}")
                }
            }
            
            // Validate date formats
            exportData.pieces.forEach { piece ->
                validateDateField(piece.statistics.lastPracticeDate, "lastPracticeDate for piece ${piece.name}", errors)
                validateDateField(piece.statistics.secondLastPracticeDate, "secondLastPracticeDate for piece ${piece.name}", errors)
                validateDateField(piece.statistics.thirdLastPracticeDate, "thirdLastPracticeDate for piece ${piece.name}", errors)
                validateDateField(piece.statistics.lastPerformanceDate, "lastPerformanceDate for piece ${piece.name}", errors)
                validateDateField(piece.statistics.secondLastPerformanceDate, "secondLastPerformanceDate for piece ${piece.name}", errors)
                validateDateField(piece.statistics.thirdLastPerformanceDate, "thirdLastPerformanceDate for piece ${piece.name}", errors)
                validateDateField(piece.statistics.lastSatisfactoryPractice, "lastSatisfactoryPractice for piece ${piece.name}", errors)
                validateDateField(piece.statistics.lastSatisfactoryPerformance, "lastSatisfactoryPerformance for piece ${piece.name}", errors)
                validateDateField(piece.statistics.dateCreated, "dateCreated for piece ${piece.name}", errors)
                validateDateField(piece.statistics.lastUpdated, "lastUpdated for piece ${piece.name}", errors)
            }
            
            exportData.activities.forEach { activity ->
                validateDateField(activity.timestamp, "timestamp for activity ${activity.id}", errors)
            }
            
            // Validate achievements date fields
            exportData.achievements?.forEach { achievement ->
                validateDateField(achievement.unlockedAt, "unlockedAt for achievement ${achievement.type}", errors)
                validateDateField(achievement.dateCreated, "dateCreated for achievement ${achievement.type}", errors)
                
                // Validate achievement data consistency
                if (achievement.title.isBlank()) {
                    errors.add("Achievement ${achievement.type} has blank title")
                }
                if (achievement.description.isBlank()) {
                    errors.add("Achievement ${achievement.type} has blank description")
                }
                if (achievement.isUnlocked && achievement.unlockedAt.isNullOrBlank()) {
                    errors.add("Achievement ${achievement.type} is marked unlocked but has no unlock date")
                }
            }
            
            return JsonValidationResult(
                isValid = errors.isEmpty(),
                pieceCount = exportData.pieces.size,
                activityCount = exportData.activities.size,
                achievementCount = achievementCount,
                errors = errors,
                formatVersion = exportData.exportInfo.version
            )
            
        } catch (e: JsonSyntaxException) {
            Log.e("JsonImporter", "JSON syntax error during validation", e)
            return JsonValidationResult(
                isValid = false,
                pieceCount = 0,
                activityCount = 0,
                achievementCount = 0,
                errors = listOf("Invalid JSON format: ${e.message}"),
                formatVersion = null
            )
        } catch (e: Exception) {
            Log.e("JsonImporter", "Error during JSON validation", e)
            return JsonValidationResult(
                isValid = false,
                pieceCount = 0,
                activityCount = 0,
                achievementCount = 0,
                errors = listOf("Validation error: ${e.message}"),
                formatVersion = null
            )
        }
    }
    
    /**
     * Imports JSON data and converts to entities
     */
    fun importFromJson(reader: Reader): JsonImportResult {
        Log.d("JsonImporter", "Starting JSON import")
        
        try {
            val content = reader.readText()
            val exportData = gson.fromJson(content, ExportData::class.java)
            
            val pieces = mutableListOf<PieceOrTechnique>()
            val activities = mutableListOf<Activity>()
            val achievements = mutableListOf<Achievement>()
            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()
            
            // Build original piece ID mapping and convert pieces
            val originalPieceIds = mutableMapOf<String, Long>()
            exportData.pieces.forEach { exportPiece ->
                try {
                    val piece = convertPieceFromExport(exportPiece)
                    pieces.add(piece)
                    originalPieceIds[exportPiece.name] = exportPiece.id
                } catch (e: Exception) {
                    errors.add("Failed to convert piece '${exportPiece.name}': ${e.message}")
                }
            }
            
            // Convert activities
            exportData.activities.forEach { exportActivity ->
                try {
                    val activity = convertActivityFromExport(exportActivity)
                    activities.add(activity)
                } catch (e: Exception) {
                    errors.add("Failed to convert activity ${exportActivity.id}: ${e.message}")
                }
            }
            
            // Convert achievements (optional section)
            exportData.achievements?.forEach { exportAchievement ->
                try {
                    val achievement = convertAchievementFromExport(exportAchievement)
                    achievements.add(achievement)
                } catch (e: Exception) {
                    errors.add("Failed to convert achievement ${exportAchievement.type}: ${e.message}")
                }
            }
            
            Log.d("JsonImporter", "JSON import completed: ${pieces.size} pieces, ${activities.size} activities, ${achievements.size} achievements, ${errors.size} errors")
            
            return JsonImportResult(
                success = errors.isEmpty(),
                piecesImported = pieces.size,
                activitiesImported = activities.size,
                achievementsImported = achievements.size,
                errors = errors,
                warnings = warnings
            ).also {
                // Store converted data for repository access
                lastImportedPieces = pieces
                lastImportedActivities = activities
                lastImportedAchievements = achievements
                lastOriginalPieceIds = originalPieceIds
                lastImportedLifetimeCount = exportData.exportInfo.lifetimeActivityCount
            }
            
        } catch (e: JsonSyntaxException) {
            Log.e("JsonImporter", "JSON syntax error during import", e)
            return JsonImportResult(
                success = false,
                piecesImported = 0,
                activitiesImported = 0,
                achievementsImported = 0,
                errors = listOf("Invalid JSON format: ${e.message}"),
                warnings = emptyList()
            )
        } catch (e: Exception) {
            Log.e("JsonImporter", "Error during JSON import", e)
            return JsonImportResult(
                success = false,
                piecesImported = 0,
                activitiesImported = 0,
                achievementsImported = 0,
                errors = listOf("Import error: ${e.message}"),
                warnings = emptyList()
            )
        }
    }
    
    // Store last imported data for repository access
    private var lastImportedPieces: List<PieceOrTechnique> = emptyList()
    private var lastImportedActivities: List<Activity> = emptyList()
    private var lastImportedAchievements: List<Achievement> = emptyList()
    private var lastOriginalPieceIds: Map<String, Long> = emptyMap() // piece name -> original ID
    private var lastImportedLifetimeCount: Int? = null
    
    /**
     * Gets the pieces from the last successful import
     */
    fun getLastImportedPieces(): List<PieceOrTechnique> = lastImportedPieces
    
    /**
     * Gets the activities from the last successful import
     */
    fun getLastImportedActivities(): List<Activity> = lastImportedActivities
    
    /**
     * Gets the achievements from the last successful import
     */
    fun getLastImportedAchievements(): List<Achievement> = lastImportedAchievements
    
    /**
     * Gets the mapping of piece names to their original IDs from the export
     */
    fun getLastOriginalPieceIds(): Map<String, Long> = lastOriginalPieceIds
    
    /**
     * Gets the lifetime activity count from the last import, or null if not provided
     */
    fun getLastImportedLifetimeCount(): Int? = lastImportedLifetimeCount
    
    /**
     * Converts an ExportPiece to a PieceOrTechnique entity
     */
    private fun convertPieceFromExport(exportPiece: ExportPiece): PieceOrTechnique {
        return PieceOrTechnique(
            id = 0, // Will be auto-generated during insert
            name = exportPiece.name.trim(),
            type = exportPiece.type,
            isFavorite = exportPiece.isFavorite,
            dateCreated = parseTimestamp(exportPiece.statistics.dateCreated) ?: System.currentTimeMillis(),
            practiceCount = exportPiece.statistics.practiceCount,
            performanceCount = exportPiece.statistics.performanceCount,
            lastPracticeDate = parseTimestamp(exportPiece.statistics.lastPracticeDate),
            secondLastPracticeDate = parseTimestamp(exportPiece.statistics.secondLastPracticeDate),
            thirdLastPracticeDate = parseTimestamp(exportPiece.statistics.thirdLastPracticeDate),
            lastPerformanceDate = parseTimestamp(exportPiece.statistics.lastPerformanceDate),
            secondLastPerformanceDate = parseTimestamp(exportPiece.statistics.secondLastPerformanceDate),
            thirdLastPerformanceDate = parseTimestamp(exportPiece.statistics.thirdLastPerformanceDate),
            lastSatisfactoryPractice = parseTimestamp(exportPiece.statistics.lastSatisfactoryPractice),
            lastSatisfactoryPerformance = parseTimestamp(exportPiece.statistics.lastSatisfactoryPerformance),
            lastUpdated = parseTimestamp(exportPiece.statistics.lastUpdated) ?: System.currentTimeMillis(),
            key = exportPiece.key,
            artist = exportPiece.artist,
            notes = exportPiece.notes
        )
    }
    
    /**
     * Converts an ExportActivity to an Activity entity
     */
    private fun convertActivityFromExport(exportActivity: ExportActivity): Activity {
        return Activity(
            id = 0, // Will be auto-generated during insert
            timestamp = parseTimestamp(exportActivity.timestamp) ?: System.currentTimeMillis(),
            pieceOrTechniqueId = exportActivity.pieceId, // Preserve original piece ID for mapping
            activityType = exportActivity.activityType,
            level = exportActivity.level,
            performanceType = exportActivity.performanceType,
            minutes = exportActivity.minutes,
            notes = exportActivity.notes
        )
    }
    
    /**
     * Converts an ExportAchievement to an Achievement entity
     */
    private fun convertAchievementFromExport(exportAchievement: ExportAchievement): Achievement {
        return Achievement(
            type = exportAchievement.type,
            title = exportAchievement.title,
            description = exportAchievement.description,
            iconEmoji = exportAchievement.iconEmoji,
            isUnlocked = exportAchievement.isUnlocked,
            unlockedAt = parseTimestamp(exportAchievement.unlockedAt),
            dateCreated = parseTimestamp(exportAchievement.dateCreated) ?: System.currentTimeMillis()
        )
    }
    
    /**
     * Parses a timestamp string to Long, trying multiple formats
     */
    private fun parseTimestamp(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        
        // Try main format first
        try {
            return dateFormatter.parse(dateString)?.time
        } catch (e: Exception) {
            // Try alternative formats
            for (formatter in alternateDateFormatters) {
                try {
                    return formatter.parse(dateString)?.time
                } catch (e: Exception) {
                    // Continue to next format
                }
            }
        }
        
        // If all formats fail, try parsing as long (timestamp)
        try {
            return dateString.toLong()
        } catch (e: Exception) {
            Log.w("JsonImporter", "Failed to parse date: $dateString")
            return null
        }
    }
    
    /**
     * Validates a date field format
     */
    private fun validateDateField(dateString: String?, fieldName: String, errors: MutableList<String>) {
        if (dateString != null && dateString.isNotBlank()) {
            if (parseTimestamp(dateString) == null) {
                errors.add("Invalid date format in $fieldName: '$dateString'")
            }
        }
    }
}