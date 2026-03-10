package com.badmintonai.data.local

import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.ScoringDimension
import com.badmintonai.domain.model.StrokeType
import org.junit.Assert.assertEquals
import org.junit.Test

class EntitiesTest {

    @Test
    fun `Entity to domain mapping works correctly`() {
        val entity = AnalysisResultEntity(
            id = 123,
            timestamp = 123456789L,
            strokeType = StrokeType.SMASH,
            overallScore = 88,
            dimensionScores = listOf(
                DimensionScore(ScoringDimension.PREPARATION, 90, 0.2f, "Good"),
                DimensionScore(ScoringDimension.CONTACT_POINT, 85, 0.25f, "Great")
            ),
            summaryFeedback = "Excellent smash!",
            videoPath = "/test/smash.mp4",
            durationMs = 1500
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.timestamp, domain.timestamp)
        assertEquals(entity.strokeType, domain.strokeType)
        assertEquals(entity.overallScore, domain.overallScore)
        assertEquals(entity.dimensionScores, domain.dimensionScores)
        assertEquals(entity.summaryFeedback, domain.summaryFeedback)
        assertEquals(entity.videoPath, domain.videoPath)
        assertEquals(entity.durationMs, domain.durationMs)
    }

    @Test
    fun `Domain to entity mapping works correctly`() {
        val domain = AnalysisResult(
            id = 456,
            timestamp = 987654321L,
            strokeType = StrokeType.DROP_SHOT,
            overallScore = 76,
            dimensionScores = listOf(
                DimensionScore(ScoringDimension.FOLLOW_THROUGH, 70, 0.15f, "Needs work"),
                DimensionScore(ScoringDimension.FOOTWORK, 80, 0.1f, "Good")
            ),
            summaryFeedback = "Good drop shot, improve follow through",
            videoPath = "/test/drop.mp4",
            durationMs = 1200
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.timestamp, entity.timestamp)
        assertEquals(domain.strokeType, entity.strokeType)
        assertEquals(domain.overallScore, entity.overallScore)
        assertEquals(domain.dimensionScores, entity.dimensionScores)
        assertEquals(domain.summaryFeedback, entity.summaryFeedback)
        assertEquals(domain.videoPath, entity.videoPath)
        assertEquals(domain.durationMs, entity.durationMs)
    }

    @Test
    fun `Mapping is bidirectionally consistent`() {
        val original = AnalysisResult(
            id = 789,
            timestamp = System.currentTimeMillis(),
            strokeType = StrokeType.FOREHAND_CLEAR,
            overallScore = 82,
            dimensionScores = listOf(
                DimensionScore(ScoringDimension.PREPARATION, 85, 0.2f, "Good"),
                DimensionScore(ScoringDimension.BACKSWING, 78, 0.15f, "Average"),
                DimensionScore(ScoringDimension.CONTACT_POINT, 88, 0.25f, "Excellent")
            ),
            summaryFeedback = "Good clear, improve backswing",
            videoPath = "/test/clear.mp4",
            durationMs = 1300
        )

        val mapped = original.toEntity().toDomain()

        assertEquals(original, mapped)
    }
}
