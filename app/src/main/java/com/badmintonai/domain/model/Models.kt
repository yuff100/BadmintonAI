package com.badmintonai.domain.model

enum class StrokeType {
    FOREHAND_CLEAR,
    SMASH,
    DROP_SHOT,
    SERVE,
    NET_SHOT,
    UNKNOWN
}

enum class ScoringDimension {
    PREPARATION,
    BACKSWING,
    CONTACT_POINT,
    FOLLOW_THROUGH,
    TIMING,
    FOOTWORK
}

data class PoseLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)

data class PoseFrame(
    val timestamp: Long,
    val landmarks: List<PoseLandmark>
)

data class DimensionScore(
    val dimension: ScoringDimension,
    val score: Int,
    val weight: Float,
    val feedback: String
)

data class AnalysisResult(
    val id: Long = 0,
    val timestamp: Long,
    val strokeType: StrokeType,
    val overallScore: Int,
    val dimensionScores: List<DimensionScore>,
    val summaryFeedback: String,
    val videoPath: String,
    val durationMs: Long
)

data class ReferencePose(
    val strokeType: StrokeType,
    val keyFrames: List<PoseFrame>,
    val idealAngles: Map<String, Float>
)
