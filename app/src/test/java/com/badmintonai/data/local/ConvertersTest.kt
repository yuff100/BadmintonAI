package com.badmintonai.data.local

import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.ScoringDimension
import com.badmintonai.domain.model.StrokeType
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()
    private val gson = Gson()

    @Test
    fun `StrokeType conversion works correctly`() {
        val types = listOf(
            StrokeType.FOREHAND_CLEAR,
            StrokeType.SMASH,
            StrokeType.DROP_SHOT,
            StrokeType.SERVE,
            StrokeType.NET_SHOT,
            StrokeType.UNKNOWN
        )

        for (type in types) {
            val name = converters.fromStrokeType(type)
            val converted = converters.toStrokeType(name)
            assertEquals(type, converted)
        }
    }

    @Test
    fun `DimensionScore list conversion works correctly`() {
        val scores = listOf(
            DimensionScore(ScoringDimension.PREPARATION, 90, 0.2f, "Good preparation"),
            DimensionScore(ScoringDimension.BACKSWING, 80, 0.15f, "Good backswing"),
            DimensionScore(ScoringDimension.CONTACT_POINT, 85, 0.25f, "Excellent contact")
        )

        val json = converters.fromDimensionScores(scores)
        val converted = converters.toDimensionScores(json)

        assertEquals(scores.size, converted.size)
        assertEquals(scores[0].score, converted[0].score)
        assertEquals(scores[1].dimension, converted[1].dimension)
        assertEquals(scores[2].feedback, converted[2].feedback)
        assertEquals(scores[0].weight, converted[0].weight)
    }

    @Test
    fun `Empty dimension scores list converts correctly`() {
        val emptyList = emptyList<DimensionScore>()
        val json = converters.fromDimensionScores(emptyList)
        val converted = converters.toDimensionScores(json)
        assertEquals(emptyList, converted)
    }

    @Test
    fun `Single dimension score converts correctly`() {
        val score = DimensionScore(ScoringDimension.FOOTWORK, 75, 0.1f, "Needs improvement")
        val json = converters.fromDimensionScores(listOf(score))
        val converted = converters.toDimensionScores(json)
        assertEquals(1, converted.size)
        assertEquals(score, converted[0])
    }
}
