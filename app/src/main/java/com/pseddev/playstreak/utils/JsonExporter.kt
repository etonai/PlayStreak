package com.pseddev.playstreak.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pseddev.playstreak.BuildConfig
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.models.*
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

object JsonExporter {
    
    private const val EXPORT_FORMAT_VERSION = "1.0"
    private const val EXPORT_FORMAT_TYPE = "combined_data"
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    /**
     * Exports pieces, activities, and achievements to JSON format
     */
    fun exportToJson(
        writer: Writer,
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>,
        achievements: List<Achievement>? = null,
        lifetimeActivityCount: Int? = null
    ) {
        Log.d("JsonExporter", "Starting JSON export with ${pieces.size} pieces, ${activities.size} activities, and ${achievements?.size ?: 0} achievements")
        
        try {
            val exportData = createExportData(pieces, activities, achievements, lifetimeActivityCount)
            val jsonString = gson.toJson(exportData)
            
            writer.write(jsonString)
            writer.flush()
            
            Log.d("JsonExporter", "JSON export completed successfully")
        } catch (e: Exception) {
            Log.e("JsonExporter", "Error during JSON export", e)
            throw e
        }
    }
    
    /**
     * Creates the export data structure
     */
    private fun createExportData(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>,
        achievements: List<Achievement>? = null,
        lifetimeActivityCount: Int? = null
    ): ExportData {
        val exportInfo = ExportInfo(
            version = EXPORT_FORMAT_VERSION,
            exportDate = dateFormatter.format(Date()),
            format = EXPORT_FORMAT_TYPE,
            appVersion = BuildConfig.VERSION_NAME,
            lifetimeActivityCount = lifetimeActivityCount
        )
        
        val exportPieces = pieces.map { piece ->
            convertPieceToExport(piece)
        }
        
        val exportActivities = activities.map { activity ->
            convertActivityToExport(activity)
        }
        
        val exportAchievements = achievements?.map { achievement ->
            convertAchievementToExport(achievement)
        }
        
        return ExportData(
            exportInfo = exportInfo,
            pieces = exportPieces,
            activities = exportActivities,
            achievements = exportAchievements
        )
    }
    
    /**
     * Converts a PieceOrTechnique entity to export format
     */
    private fun convertPieceToExport(piece: PieceOrTechnique): ExportPiece {
        val statistics = PieceStatistics(
            practiceCount = piece.practiceCount,
            performanceCount = piece.performanceCount,
            lastPracticeDate = formatTimestamp(piece.lastPracticeDate),
            secondLastPracticeDate = formatTimestamp(piece.secondLastPracticeDate),
            thirdLastPracticeDate = formatTimestamp(piece.thirdLastPracticeDate),
            lastPerformanceDate = formatTimestamp(piece.lastPerformanceDate),
            secondLastPerformanceDate = formatTimestamp(piece.secondLastPerformanceDate),
            thirdLastPerformanceDate = formatTimestamp(piece.thirdLastPerformanceDate),
            lastSatisfactoryPractice = formatTimestamp(piece.lastSatisfactoryPractice),
            lastSatisfactoryPerformance = formatTimestamp(piece.lastSatisfactoryPerformance),
            dateCreated = formatTimestamp(piece.dateCreated) ?: dateFormatter.format(Date()),
            lastUpdated = formatTimestamp(piece.lastUpdated) ?: dateFormatter.format(Date())
        )
        
        return ExportPiece(
            id = piece.id,
            name = piece.name,
            type = piece.type,
            isFavorite = piece.isFavorite,
            key = piece.key,
            artist = piece.artist,
            notes = piece.notes,
            statistics = statistics
        )
    }
    
    /**
     * Converts an Activity entity to export format
     */
    private fun convertActivityToExport(activity: Activity): ExportActivity {
        return ExportActivity(
            id = activity.id,
            pieceId = activity.pieceOrTechniqueId,
            timestamp = formatTimestamp(activity.timestamp) ?: dateFormatter.format(Date()),
            activityType = activity.activityType,
            level = activity.level,
            performanceType = activity.performanceType,
            minutes = activity.minutes,
            notes = activity.notes
        )
    }
    
    /**
     * Converts an Achievement entity to export format
     */
    private fun convertAchievementToExport(achievement: Achievement): ExportAchievement {
        return ExportAchievement(
            type = achievement.type,
            title = achievement.title,
            description = achievement.description,
            iconEmoji = achievement.iconEmoji,
            isUnlocked = achievement.isUnlocked,
            unlockedAt = formatTimestamp(achievement.unlockedAt),
            dateCreated = formatTimestamp(achievement.dateCreated) ?: dateFormatter.format(Date())
        )
    }
    
    /**
     * Formats a timestamp to ISO 8601 format, returns null for null/zero timestamps
     */
    private fun formatTimestamp(timestamp: Long?): String? {
        return if (timestamp != null && timestamp > 0) {
            dateFormatter.format(Date(timestamp))
        } else {
            null
        }
    }
    
    /**
     * Validates that export data is properly formatted
     */
    fun validateExportData(exportData: ExportData): List<String> {
        val errors = mutableListOf<String>()
        
        // Validate export info
        if (exportData.exportInfo.version.isBlank()) {
            errors.add("Export version is missing")
        }
        
        if (exportData.exportInfo.exportDate.isBlank()) {
            errors.add("Export date is missing")
        }
        
        // Validate pieces
        exportData.pieces.forEach { piece ->
            if (piece.name.isBlank()) {
                errors.add("Piece with ID ${piece.id} has blank name")
            }
            
            if (piece.statistics.practiceCount < 0) {
                errors.add("Piece '${piece.name}' has negative practice count")
            }
            
            if (piece.statistics.performanceCount < 0) {
                errors.add("Piece '${piece.name}' has negative performance count")
            }
        }
        
        // Validate activities
        exportData.activities.forEach { activity ->
            if (activity.level < 1) {
                errors.add("Activity with ID ${activity.id} has invalid level: ${activity.level}")
            }
            
            if (activity.minutes < -1) {
                errors.add("Activity with ID ${activity.id} has invalid minutes: ${activity.minutes}")
            }
        }
        
        // Validate achievements (optional section)
        exportData.achievements?.forEach { achievement ->
            if (achievement.title.isBlank()) {
                errors.add("Achievement ${achievement.type} has blank title")
            }
            
            if (achievement.description.isBlank()) {
                errors.add("Achievement ${achievement.type} has blank description")
            }
            
            if (achievement.isUnlocked && achievement.unlockedAt.isNullOrBlank()) {
                errors.add("Achievement ${achievement.type} is marked as unlocked but has no unlock date")
            }
        }
        
        // Validate relationships
        val pieceIds = exportData.pieces.map { it.id }.toSet()
        exportData.activities.forEach { activity ->
            if (activity.pieceId !in pieceIds) {
                errors.add("Activity with ID ${activity.id} references non-existent piece ID ${activity.pieceId}")
            }
        }
        
        return errors
    }
}