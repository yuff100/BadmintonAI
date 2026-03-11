package com.badmintonai.domain.usecase

import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.ScoringDimension
import com.badmintonai.domain.model.StrokeType
import com.badmintonai.domain.repository.AnalysisRepository
import com.badmintonai.domain.repository.PoseEstimationRepository
import com.badmintonai.domain.repository.ScoringRepository
import com.badmintonai.domain.repository.StrokeClassificationRepository
import javax.inject.Inject

class AnalyzeVideoUseCase @Inject constructor(
    private val poseEstimationRepo: PoseEstimationRepository,
    private val classificationRepo: StrokeClassificationRepository,
    private val scoringRepo: ScoringRepository,
    private val analysisRepo: AnalysisRepository
) {
    suspend operator fun invoke(videoPath: String): AnalysisResult {
        val poseFrames = poseEstimationRepo.processVideo(videoPath)
        val strokeType = classificationRepo.classifyStroke(poseFrames)
        val dimensionScores = scoringRepo.calculateScore(strokeType, poseFrames)
        
        val overallScore = dimensionScores.sumOf { 
            (it.score * it.weight).toInt() 
        }
        
        val summaryFeedback = generateSummaryFeedback(dimensionScores, overallScore)
        
        val result = AnalysisResult(
            timestamp = System.currentTimeMillis(),
            strokeType = strokeType,
            overallScore = overallScore,
            dimensionScores = dimensionScores,
            summaryFeedback = summaryFeedback,
            videoPath = videoPath,
            durationMs = poseFrames.lastOrNull()?.timestamp ?: 0
        )
        
        analysisRepo.saveResult(result)
        
        return result
    }
    
    private fun generateSummaryFeedback(
        dimensionScores: List<DimensionScore>,
        overallScore: Int
    ): String {
        val lowestScore = dimensionScores.minByOrNull { it.score }
        val highestScore = dimensionScores.maxByOrNull { it.score }
        
        return buildString {
            append("Overall performance: ")
            append(when {
                overallScore >= 90 -> "Excellent!"
                overallScore >= 80 -> "Very good!"
                overallScore >= 70 -> "Good!"
                overallScore >= 60 -> "Average."
                else -> "Needs improvement."
            })
            append("\n\n")
            
            highestScore?.let {
                append("Strength: ${it.dimension.name} - ${it.feedback}\n")
            }
            
            lowestScore?.let {
                append("Area to improve: ${it.dimension.name} - ${it.feedback}")
            }
        }
    }
}

class GetAnalysisHistoryUseCase @Inject constructor(
    private val analysisRepository: AnalysisRepository
) {
    suspend operator fun invoke(): List<AnalysisResult> {
        return analysisRepository.getHistory()
    }
}

class GetAnalysisResultUseCase @Inject constructor(
    private val analysisRepository: AnalysisRepository
) {
    suspend operator fun invoke(id: Long): AnalysisResult? {
        return analysisRepository.getResultById(id)
    }
}

class DeleteAnalysisResultUseCase @Inject constructor(
    private val analysisRepository: AnalysisRepository
) {
    suspend operator fun invoke(id: Long) {
        return analysisRepository.deleteResult(id)
    }
}
