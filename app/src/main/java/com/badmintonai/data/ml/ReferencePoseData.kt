package com.badmintonai.data.ml

import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.PoseLandmark
import com.badmintonai.domain.model.ReferencePose
import com.badmintonai.domain.model.StrokeType

object ReferencePoseData {
    val referencePoses = mapOf(
        StrokeType.FOREHAND_CLEAR to ReferencePose(
            strokeType = StrokeType.FOREHAND_CLEAR,
            keyFrames = generateForehandClearFrames(),
            idealAngles = mapOf(
                "preparation_angle" to 85f,
                "backswing_angle" to 130f,
                "follow_through_angle" to 170f,
                "stance_width" to 0.35f
            )
        ),
        StrokeType.SMASH to ReferencePose(
            strokeType = StrokeType.SMASH,
            keyFrames = generateSmashFrames(),
            idealAngles = mapOf(
                "preparation_angle" to 95f,
                "backswing_angle" to 150f,
                "follow_through_angle" to 140f,
                "stance_width" to 0.4f
            )
        ),
        StrokeType.DROP_SHOT to ReferencePose(
            strokeType = StrokeType.DROP_SHOT,
            keyFrames = generateDropShotFrames(),
            idealAngles = mapOf(
                "preparation_angle" to 80f,
                "backswing_angle" to 110f,
                "follow_through_angle" to 120f,
                "stance_width" to 0.3f
            )
        ),
        StrokeType.SERVE to ReferencePose(
            strokeType = StrokeType.SERVE,
            keyFrames = generateServeFrames(),
            idealAngles = mapOf(
                "preparation_angle" to 70f,
                "backswing_angle" to 100f,
                "follow_through_angle" to 130f,
                "stance_width" to 0.25f
            )
        ),
        StrokeType.NET_SHOT to ReferencePose(
            strokeType = StrokeType.NET_SHOT,
            keyFrames = generateNetShotFrames(),
            idealAngles = mapOf(
                "preparation_angle" to 60f,
                "backswing_angle" to 85f,
                "follow_through_angle" to 95f,
                "stance_width" to 0.28f
            )
        )
    )

    private fun generateForehandClearFrames(): List<PoseFrame> {
        return listOf(
            PoseFrame(
                timestamp = 0,
                landmarks = generateStandardPose(
                    rightShoulder = PoseLandmark(0.5f, 0.4f, 0f, 1f),
                    rightElbow = PoseLandmark(0.6f, 0.45f, 0f, 1f),
                    rightWrist = PoseLandmark(0.7f, 0.4f, 0f, 1f)
                )
            )
        )
    }

    private fun generateSmashFrames(): List<PoseFrame> {
        return listOf(
            PoseFrame(
                timestamp = 0,
                landmarks = generateStandardPose(
                    rightShoulder = PoseLandmark(0.5f, 0.35f, 0f, 1f),
                    rightElbow = PoseLandmark(0.65f, 0.3f, 0f, 1f),
                    rightWrist = PoseLandmark(0.75f, 0.25f, 0f, 1f)
                )
            )
        )
    }

    private fun generateDropShotFrames(): List<PoseFrame> {
        return listOf(
            PoseFrame(
                timestamp = 0,
                landmarks = generateStandardPose(
                    rightShoulder = PoseLandmark(0.5f, 0.42f, 0f, 1f),
                    rightElbow = PoseLandmark(0.58f, 0.47f, 0f, 1f),
                    rightWrist = PoseLandmark(0.65f, 0.43f, 0f, 1f)
                )
            )
        )
    }

    private fun generateServeFrames(): List<PoseFrame> {
        return listOf(
            PoseFrame(
                timestamp = 0,
                landmarks = generateStandardPose(
                    rightShoulder = PoseLandmark(0.52f, 0.45f, 0f, 1f),
                    rightElbow = PoseLandmark(0.58f, 0.5f, 0f, 1f),
                    rightWrist = PoseLandmark(0.62f, 0.46f, 0f, 1f)
                )
            )
        )
    }

    private fun generateNetShotFrames(): List<PoseFrame> {
        return listOf(
            PoseFrame(
                timestamp = 0,
                landmarks = generateStandardPose(
                    rightShoulder = PoseLandmark(0.5f, 0.48f, 0f, 1f),
                    rightElbow = PoseLandmark(0.55f, 0.52f, 0f, 1f),
                    rightWrist = PoseLandmark(0.58f, 0.49f, 0f, 1f)
                )
            )
        )
    }

    private fun generateStandardPose(
        rightShoulder: PoseLandmark,
        rightElbow: PoseLandmark,
        rightWrist: PoseLandmark
    ): List<PoseLandmark> {
        val landmarks = MutableList(33) { PoseLandmark(0f, 0f, 0f, 0f) }
        landmarks[11] = rightShoulder
        landmarks[12] = PoseLandmark(0.3f, rightShoulder.y, 0f, 1f)
        landmarks[13] = rightElbow
        landmarks[14] = PoseLandmark(0.2f, rightElbow.y, 0f, 1f)
        landmarks[15] = rightWrist
        landmarks[16] = PoseLandmark(0.1f, rightWrist.y, 0f, 1f)
        landmarks[23] = PoseLandmark(0.5f, 0.6f, 0f, 1f)
        landmarks[24] = PoseLandmark(0.3f, 0.6f, 0f, 1f)
        landmarks[25] = PoseLandmark(0.55f, 0.75f, 0f, 1f)
        landmarks[26] = PoseLandmark(0.25f, 0.75f, 0f, 1f)
        landmarks[27] = PoseLandmark(0.6f, 0.9f, 0f, 1f)
        landmarks[28] = PoseLandmark(0.2f, 0.9f, 0f, 1f)
        return landmarks
    }
}
