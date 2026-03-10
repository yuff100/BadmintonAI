package com.badmintonai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    @Test
    fun `StrokeType enum contains all required values`() {
        val expectedTypes = listOf(
            "FOREHAND_CLEAR",
            "SMASH",
            "DROP_SHOT",
            "SERVE",
            "NET_SHOT",
            "UNKNOWN"
        )

        val actualTypes = StrokeType.values().map { it.name }
        assertEquals(expectedTypes, actualTypes)
    }

    @Test
    fun `ScoringDimension enum has correct weights`() {
        val dimensions = ScoringDimension.values()
        assertEquals(6, dimensions.size)

        val expectedWeights = mapOf(
            ScoringDimension.PREPARATION to 0.2f,
            ScoringDimension.BACKSWING to 0.15f,
            ScoringDimension.CONTACT_POINT to 0.25f,
            ScoringDimension.FOLLOW_THROUGH to 0.15f,
            ScoringDimension.TIMING to 0.15f,
            ScoringDimension.FOOTWORK to 0.1f
        )

        val totalWeight = expectedWeights.values.sum()
        assertEquals(1.0f, totalWeight, 0.001f)
    }

    @Test
    fun `PoseLandmark correctly stores coordinates`() {
        val landmark = PoseLandmark(0.5f, 0.3f, -0.2f, 0.9f)

        assertEquals(0.5f, landmark.x)
        assertEquals(0.3f, landmark.y)
        assertEquals(-0.2f, landmark.z)
        assertEquals(0.9f, landmark.visibility)
    }

    @Test
    fun `DimensionScore calculates weighted score correctly`() {
        val score = DimensionScore(
            dimension = ScoringDimension.CONTACT_POINT,
            score = 80,
            weight = 0.25f,
            feedback = "Good contact point"
        )

        val weighted = score.score * score.weight
        assertEquals(20f, weighted)
    }

    @Test
    fun `AnalysisResult overall score calculation is correct`() {
        val dimensionScores = listOf(
            DimensionScore(ScoringDimension.PREPARATION, 90, 0.2f, ""),
            DimensionScore(ScoringDimension.BACKSWING, 80, 0.15f, ""),
            DimensionScore(ScoringDimension.CONTACT_POINT, 85, 0.25f, ""),
            DimensionScore(ScoringDimension.FOLLOW_THROUGH, 75, 0.15f, ""),
            DimensionScore(ScoringDimension.TIMING, 95, 0.15f, ""),
            DimensionScore(ScoringDimension.FOOTWORK, 70, 0.1f, "")
        )

        val overall = dimensionScores.sumOf {
            (it.score * it.weight).toInt()
        }

        assertEquals(83, overall)
        assertTrue(overall in 0..100)
    }
}
