package com.badmintonai.domain.repository

import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.StrokeType

interface AnalysisRepository {
    suspend fun analyzeVideo(videoPath: String): AnalysisResult
    suspend fun saveResult(result: AnalysisResult)
    suspend fun getHistory(): List<AnalysisResult>
    suspend fun getResultById(id: Long): AnalysisResult?
    suspend fun deleteResult(id: Long)
}

interface PoseEstimationRepository {
    suspend fun processVideo(videoPath: String): List<PoseFrame>
    suspend fun processFrame(frameBytes: ByteArray, timestamp: Long): PoseFrame?
}

interface StrokeClassificationRepository {
    suspend fun classifyStroke(poseFrames: List<PoseFrame>): StrokeType
}

interface ScoringRepository {
    suspend fun calculateScore(
        strokeType: StrokeType,
        poseFrames: List<PoseFrame>
    ): List<com.badmintonai.domain.model.DimensionScore>
}
