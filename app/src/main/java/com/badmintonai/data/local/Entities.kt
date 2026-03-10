package com.badmintonai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.StrokeType

@Entity(tableName = "analysis_results")
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val strokeType: StrokeType,
    val overallScore: Int,
    val dimensionScores: List<DimensionScore>,
    val summaryFeedback: String,
    val videoPath: String,
    val durationMs: Long
)

fun AnalysisResultEntity.toDomain(): AnalysisResult {
    return AnalysisResult(
        id = id,
        timestamp = timestamp,
        strokeType = strokeType,
        overallScore = overallScore,
        dimensionScores = dimensionScores,
        summaryFeedback = summaryFeedback,
        videoPath = videoPath,
        durationMs = durationMs
    )
}

fun AnalysisResult.toEntity(): AnalysisResultEntity {
    return AnalysisResultEntity(
        id = id,
        timestamp = timestamp,
        strokeType = strokeType,
        overallScore = overallScore,
        dimensionScores = dimensionScores,
        summaryFeedback = summaryFeedback,
        videoPath = videoPath,
        durationMs = durationMs
    )
}
