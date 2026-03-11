package com.badmintonai.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.PoseLandmark
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MediaPipePoseEstimator(private val context: Context) {
    private var poseLandmarker: PoseLandmarker? = null
    private val modelPath = "pose_landmarker_lite.task"

    init {
        setupPoseLandmarker()
    }

    private fun setupPoseLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(modelPath)
            
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setRunningMode(RunningMode.VIDEO)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)

            poseLandmarker = PoseLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: IOException) {
            throw RuntimeException("Failed to load pose landmarker model", e)
        }
    }

    suspend fun processVideoFrame(
        bitmap: Bitmap,
        timestampMs: Long
    ): PoseLandmarkerResult? = withContext(Dispatchers.Default) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        return@withContext poseLandmarker?.detectForVideo(mpImage, timestampMs * 1000)
    }

    fun convertToPoseFrame(result: PoseLandmarkerResult, timestamp: Long): PoseFrame? {
        if (result.landmarks().isEmpty()) return null
        
        val landmarks = result.landmarks()[0].map { landmark ->
            PoseLandmark(
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z(),
                visibility = landmark.visibility() ?: 0f
            )
        }
        
        return PoseFrame(
            timestamp = timestamp,
            landmarks = landmarks
        )
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    companion object {
        fun copyModelToAssetsIfNeeded(context: Context) {
            val modelFile = File(context.filesDir, "pose_landmarker_lite.task")
            if (!modelFile.exists()) {
                try {
                    val inputStream = context.assets.open("pose_landmarker_lite.task")
                    val outputStream = FileOutputStream(modelFile)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    inputStream.close()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
