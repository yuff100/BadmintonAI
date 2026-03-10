package com.badmintonai.data.ml

import android.content.Context
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.StrokeType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class StrokeClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "stroke_classifier.tflite"
    private val inputSize = 33 * 3 * 30 // 33 landmarks, 3 coordinates, 30 frames

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    suspend fun classifyStroke(poseFrames: List<PoseFrame>): StrokeType {
        if (poseFrames.size < 30 || interpreter == null) {
            return StrokeType.UNKNOWN
        }

        // Take last 30 frames for classification
        val recentFrames = poseFrames.takeLast(30)
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, inputSize), java.nio.ByteBuffer.allocateDirect(inputSize * 4).order(java.nio.ByteOrder.nativeOrder()))
        val inputArray = FloatArray(inputSize)

        var index = 0
        for (frame in recentFrames) {
            for (landmark in frame.landmarks) {
                inputArray[index++] = landmark.x
                inputArray[index++] = landmark.y
                inputArray[index++] = landmark.z
            }
        }

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
