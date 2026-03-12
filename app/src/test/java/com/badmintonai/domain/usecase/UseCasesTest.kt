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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UseCasesTest {

    private lateinit var poseEstimationRepo: PoseEstimationRepository
    private lateinit var classificationRepo: StrokeClassificationRepository
    private lateinit var scoringRepo: ScoringRepository
    private lateinit var analysisRepo: AnalysisRepository
    private lateinit var analyzeVideoUseCase: AnalyzeVideoUseCase

    @Before
    fun setup() {
        poseEstimationRepo = mockk()
        classificationRepo = mockk()
        scoringRepo = mockk()
        analysisRepo = mockk(relaxUnitFun = true)

        analyzeVideoUseCase = AnalyzeVideoUseCase(
            poseEstimationRepo,
            classificationRepo,
            scoringRepo,
            analysisRepo
        )
    }

    @Test
    fun `AnalyzeVideoUseCase correctly processes video and returns result`() = runTest {
        val testVideoPath = "/test/video.mp4"
        val testPoseFrames = listOf(
            PoseFrame(0, emptyList()),
            PoseFrame(33, emptyList()),
            PoseFrame(66, emptyList())
        )
        val testStrokeType = StrokeType.FOREHAND_CLEAR
        val testDimensionScores = listOf(
            DimensionScore(ScoringDimension.PREPARATION, 90, 0.2f, "Good"),
            DimensionScore(ScoringDimension.BACKSWING, 80, 0.15f, "Good"),
            DimensionScore(ScoringDimension.CONTACT_POINT, 85, 0.25f, "Great"),
            DimensionScore(ScoringDimension.FOLLOW_THROUGH, 75, 0.15f, "Average"),
            DimensionScore(ScoringDimension.TIMING, 95, 0.15f, "Excellent"),
            DimensionScore(ScoringDimension.FOOTWORK, 70, 0.1f, "Needs work")
        )

        coEvery { poseEstimationRepo.processVideo(testVideoPath) } returns testPoseFrames
        coEvery { classificationRepo.classifyStroke(testPoseFrames) } returns testStrokeType
        coEvery { scoringRepo.calculateScore(testStrokeType, testPoseFrames) } returns testDimensionScores

        val result = analyzeVideoUseCase(testVideoPath)

        assertEquals(testStrokeType, result.strokeType)
        assertEquals(83, result.overallScore)
        assertEquals(testDimensionScores, result.dimensionScores)
        assertEquals(testVideoPath, result.videoPath)

        coVerify(exactly = 1) { analysisRepo.saveResult(any<AnalysisResult>()) }
    }

    @Test
    fun `AnalyzeVideoUseCase handles empty pose frames gracefully`() = runTest {
        val testVideoPath = "/test/empty.mp4"
        coEvery { poseEstimationRepo.processVideo(testVideoPath) } returns emptyList()
        coEvery { classificationRepo.classifyStroke(emptyList()) } returns StrokeType.UNKNOWN
        coEvery { scoringRepo.calculateScore(StrokeType.UNKNOWN, emptyList()) } returns emptyList()

        val result = analyzeVideoUseCase(testVideoPath)

        assertEquals(StrokeType.UNKNOWN, result.strokeType)
        assertEquals(0, result.overallScore)
        assertEquals(emptyList<DimensionScore>(), result.dimensionScores)
    }

    @Test
    fun `GetAnalysisHistoryUseCase correctly fetches history`() = runTest {
        val expectedHistory = listOf(
            AnalysisResult(1, System.currentTimeMillis(), StrokeType.SMASH, 85, emptyList(), "", "/test/1.mp4", 1000),
            AnalysisResult(2, System.currentTimeMillis() - 3600000, StrokeType.DROP_SHOT, 72, emptyList(), "", "/test/2.mp4", 1200)
        )
        val useCase = GetAnalysisHistoryUseCase(analysisRepo)
        coEvery { analysisRepo.getHistory() } returns expectedHistory

        val result = useCase()

        assertEquals(expectedHistory, result)
        assertEquals(2, result.size)
        assertEquals(StrokeType.SMASH, result[0].strokeType)
    }

    @Test
    fun `GetAnalysisResultUseCase returns correct result by id`() = runTest {
        val testId = 123L
        val expectedResult = AnalysisResult(testId, System.currentTimeMillis(), StrokeType.SERVE, 78, emptyList(), "", "/test/serve.mp4", 800)
        val useCase = GetAnalysisResultUseCase(analysisRepo)
        coEvery { analysisRepo.getResultById(testId) } returns expectedResult

        val result = useCase(testId)

        assertEquals(expectedResult, result)
        assertEquals(testId, result?.id)
        assertEquals(StrokeType.SERVE, result?.strokeType)
    }

    @Test
    fun `DeleteAnalysisResultUseCase calls repository correctly`() = runTest {
        val testId = 456L
        val useCase = DeleteAnalysisResultUseCase(analysisRepo)
        coEvery { analysisRepo.deleteResult(testId) } returns Unit

        useCase(testId)

        coVerify(exactly = 1) { analysisRepo.deleteResult(testId) }
    }
}
