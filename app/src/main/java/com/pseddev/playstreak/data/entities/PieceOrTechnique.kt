package com.pseddev.playstreak.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pieces_techniques")
data class PieceOrTechnique(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val type: ItemType,
    val isFavorite: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    
    // Independent statistics fields
    val practiceCount: Int = 0,
    val performanceCount: Int = 0,
    val lastPracticeDate: Long? = null,
    val secondLastPracticeDate: Long? = null,
    val thirdLastPracticeDate: Long? = null,
    val lastPerformanceDate: Long? = null,
    val secondLastPerformanceDate: Long? = null,
    val thirdLastPerformanceDate: Long? = null,
    val lastSatisfactoryPractice: Long? = null,
    val lastSatisfactoryPerformance: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis(),

    // Piece metadata fields (added in database version 3)
    val key: String? = null,        // Musical key (e.g., "C Major", "D Minor")
    val artist: String? = null,     // Composer/Artist name
    val notes: String? = null       // General notes/comments about the piece
)

enum class ItemType { 
    PIECE, 
    TECHNIQUE 
}