package com.pseddev.playstreak.data.models

import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.AchievementType
import com.google.gson.annotations.SerializedName

/**
 * Root container for JSON export data
 */
data class ExportData(
    @SerializedName("exportInfo")
    val exportInfo: ExportInfo,
    
    @SerializedName("pieces")
    val pieces: List<ExportPiece>,
    
    @SerializedName("activities") 
    val activities: List<ExportActivity>,
    
    @SerializedName("achievements")
    val achievements: List<ExportAchievement>? = null // Optional for backward compatibility
)

/**
 * Metadata about the export
 */
data class ExportInfo(
    @SerializedName("version")
    val version: String,
    
    @SerializedName("exportDate")
    val exportDate: String, // ISO 8601 format
    
    @SerializedName("format")
    val format: String,
    
    @SerializedName("appVersion")
    val appVersion: String,
    
    @SerializedName("lifetimeActivityCount")
    val lifetimeActivityCount: Int? = null // Total activities ever created
)

/**
 * Piece data for JSON export including statistics
 */
data class ExportPiece(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: ItemType,

    @SerializedName("isFavorite")
    val isFavorite: Boolean,

    @SerializedName("key")
    val key: String? = null,

    @SerializedName("artist")
    val artist: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("statistics")
    val statistics: PieceStatistics
)

/**
 * Piece statistics for JSON export
 */
data class PieceStatistics(
    @SerializedName("practiceCount")
    val practiceCount: Int,
    
    @SerializedName("performanceCount")
    val performanceCount: Int,
    
    @SerializedName("lastPracticeDate")
    val lastPracticeDate: String?, // ISO 8601 format or null
    
    @SerializedName("secondLastPracticeDate")
    val secondLastPracticeDate: String?, // ISO 8601 format or null
    
    @SerializedName("thirdLastPracticeDate")
    val thirdLastPracticeDate: String?, // ISO 8601 format or null
    
    @SerializedName("lastPerformanceDate")
    val lastPerformanceDate: String?, // ISO 8601 format or null
    
    @SerializedName("secondLastPerformanceDate")
    val secondLastPerformanceDate: String?, // ISO 8601 format or null
    
    @SerializedName("thirdLastPerformanceDate")
    val thirdLastPerformanceDate: String?, // ISO 8601 format or null
    
    @SerializedName("lastSatisfactoryPractice")
    val lastSatisfactoryPractice: String?, // ISO 8601 format or null
    
    @SerializedName("lastSatisfactoryPerformance")
    val lastSatisfactoryPerformance: String?, // ISO 8601 format or null
    
    @SerializedName("dateCreated")
    val dateCreated: String, // ISO 8601 format
    
    @SerializedName("lastUpdated")
    val lastUpdated: String // ISO 8601 format
)

/**
 * Activity data for JSON export
 */
data class ExportActivity(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("pieceId")
    val pieceId: Long,
    
    @SerializedName("timestamp")
    val timestamp: String, // ISO 8601 format
    
    @SerializedName("activityType")
    val activityType: ActivityType,
    
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("performanceType")
    val performanceType: String,
    
    @SerializedName("minutes")
    val minutes: Int,
    
    @SerializedName("notes")
    val notes: String
)

/**
 * Achievement data for JSON export
 */
data class ExportAchievement(
    @SerializedName("type")
    val type: AchievementType,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("iconEmoji")
    val iconEmoji: String,
    
    @SerializedName("isUnlocked")
    val isUnlocked: Boolean,
    
    @SerializedName("unlockedAt")
    val unlockedAt: String?, // ISO 8601 format or null
    
    @SerializedName("dateCreated")
    val dateCreated: String // ISO 8601 format
)

/**
 * Result of JSON import operation
 */
data class JsonImportResult(
    val success: Boolean,
    val piecesImported: Int,
    val activitiesImported: Int,
    val achievementsImported: Int, // Added achievements count
    val errors: List<String>,
    val warnings: List<String>
)

/**
 * Validation result for JSON import
 */
data class JsonValidationResult(
    val isValid: Boolean,
    val pieceCount: Int,
    val activityCount: Int,
    val achievementCount: Int, // Added achievements count
    val errors: List<String>,
    val formatVersion: String?
)