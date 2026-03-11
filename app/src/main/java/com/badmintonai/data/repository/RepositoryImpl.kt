package com.badmintonai.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.badmintonai.data.local.AnalysisDao
import com.badmintonai.data.local.toDomain
import com.badmintonai.data.local.toEntity
import com.badmintonai.data.mediapipe.MediaPipePoseEstimator
import com.badmintonai.data.ml.ScoringEngine
import com.badmintonai.data.ml.StrokeClassifier
import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.StrokeType
import com.badmintonai.domain.repository.AnalysisRepository
import com.badmintonai.domain.repository.PoseEstimationRepository
import com.badmintonai.domain.repository.ScoringRepository
import com.badmintonai.domain.repository.StrokeClassificationRepository
import javax.inject.Inject

class AnalysisRepositoryImpl @Inject constructor(
    private val analysisDao: AnalysisDao
) : AnalysisRepository {
    
    override suspend fun analyzeVideo(videoPath: String): AnalysisResult {
        throw UnsupportedOperationException("Use AnalyzeVideoUseCase instead")
    }
    
    override suspend fun saveResult(result: AnalysisResult) {
        analysisDao.insert(result.toEntity())
    }
    
    override suspend fun getHistory(): List<AnalysisResult> {
        return analysisDao.getAll().map { it.toDomain() }
    }
    
    override suspend fun getResultById(id: Long): AnalysisResult? {
        return analysisDao.getById(id)?.toDomain()
    }
    
    override suspend fun deleteResult(id: Long) {
        analysisDao.deleteById(id)
    }
}

class PoseEstimationRepositoryImpl @Inject constructor(
    private val context: Context
) : PoseEstimationRepository {
    
    private val poseEstimator = MediaPipePoseEstimator(context)
    
    override suspend fun processVideo(videoPath: String): List<PoseFrame> {
        val poseFrames = mutableListOf<PoseFrame>()
        val retriever = MediaMetadataRetriever()
        
        try {
            retriever.setDataSource(videoPath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            val frameInterval = 33L
            
            for (timestamp in 0 until duration step frameInterval) {
                val bitmap = retriever.getFrameAtTime(
                    timestamp * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                ) ?: continue
                
                val result = poseEstimator.processVideoFrame(bitmap, timestamp)
                result?.let { 
                    poseEstimator.convertToPoseFrame(it, timestamp)?.let { frame ->
                        poseFrames.add(frame)
                    }
                }
                
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
            poseEstimator.close()
        }
        
        return poseFrames
    }
    
    override suspend fun processFrame(frameBytes: ByteArray, timestamp: Long): PoseFrame? {
        val bitmap = BitmapFactory.decodeByteArray(frameBytes, 0, frameBytes.size)
        val result = poseEstimator.processVideoFrame(bitmap, timestamp)
        bitmap.recycle()
        return result?.let { poseEstimator.convertToPoseFrame(it, timestamp) }
    }
}

class StrokeClassificationRepositoryImpl @Inject constructor(
    private val context: Context
) : StrokeClassificationRepository {
    
    private val classifier = StrokeClassifier(context)
    
    override suspend fun classifyStroke(poseFrames: List<PoseFrame>): StrokeType {
        return classifier.classifyStroke(poseFrames)
    }
}

class ScoringRepositoryImpl @Inject constructor(
    private val context: Context
) : ScoringRepository {
    
    private val scoringEngine = ScoringEngine(context)
    
    override suspend fun calculateScore(
        strokeType: StrokeType,
        poseFrames: List<PoseFrame>
    ): List<DimensionScore> {
        return scoringEngine.calculateScore(strokeType, poseFrames)
    }
}
