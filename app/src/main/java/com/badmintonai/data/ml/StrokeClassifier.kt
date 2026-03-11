package com.badmintonai.data.ml

import android.content.Context
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.StrokeType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer

class StrokeClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "stroke_classifier.tflite"
    private val inputSize = 33 * 3 * 30

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val model: MappedByteBuffer = FileUtil.loadMappedFile(context, modelPath)
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun classifyStroke(poseFrames: List<PoseFrame>): StrokeType {
        if (poseFrames.size < 30 || interpreter == null) {
            return StrokeType.UNKNOWN
        }

        val recentFrames = poseFrames.takeLast(30)
        val inputArray = FloatArray(inputSize)

        var index = 0
        for (frame in recentFrames) {
            for (landmark in frame.landmarks) {
                inputArray[index++] = landmark.x
                inputArray[index++] = landmark.y
                inputArray[index++] = landmark.z
            }
        }

        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, inputSize), org.tensorflow.lite.DataType.FLOAT32)
        inputBuffer.loadArray(inputArray)

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 6), org.tensorflow.lite.DataType.FLOAT32)
        interpreter?.run(inputBuffer.buffer, outputBuffer.buffer)

        val outputArray = outputBuffer.floatArray
        val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1

        return when (maxIndex) {
            0 -> StrokeType.FOREHAND_CLEAR
            1 -> StrokeType.SMASH
            2 -> StrokeType.DROP_SHOT
            3 -> StrokeType.SERVE
            4 -> StrokeType.NET_SHOT
            else -> StrokeType.UNKNOWN
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
