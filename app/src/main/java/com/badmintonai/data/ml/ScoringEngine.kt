package com.badmintonai.data.ml

import android.content.Context
import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.PoseFrame
import com.badmintonai.domain.model.ReferencePose
import com.badmintonai.domain.model.ScoringDimension
import com.badmintonai.domain.model.StrokeType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.max

class ScoringEngine(private val context: Context) {
    
    private val referencePoses = ReferencePoseData.referencePoses
    
    suspend fun calculateScore(
        strokeType: StrokeType,
        poseFrames: List<PoseFrame>
    ): List<DimensionScore> {
        if (poseFrames.isEmpty()) return emptyList()
        
        val keyFrames = extractKeyFrames(poseFrames)
        val reference = referencePoses[strokeType] ?: return emptyList()
        
        return listOf(
            calculatePreparationScore(keyFrames, reference),
            calculateBackswingScore(keyFrames, reference),
            calculateContactPointScore(keyFrames, reference),
            calculateFollowThroughScore(keyFrames, reference),
            calculateTimingScore(keyFrames, reference),
            calculateFootworkScore(keyFrames, reference)
        )
    }
    
    private fun extractKeyFrames(poseFrames: List<PoseFrame>): KeyFrames {
        var maxVelocity = 0f
        var contactIndex = 0
        
        for (i in 1 until poseFrames.size) {
            val prevWrist = poseFrames[i-1].landmarks[15]
            val currWrist = poseFrames[i].landmarks[15]
            val dx = currWrist.x - prevWrist.x
            val dy = currWrist.y - prevWrist.y
            val velocity = sqrt(dx.pow(2) + dy.pow(2)) 
            
            if (velocity > maxVelocity) {
                maxVelocity = velocity
                contactIndex = i
            }
        }
        
        return KeyFrames(
            preparation = poseFrames.getOrNull(contactIndex - 15) ?: poseFrames.first(),
            backswing = poseFrames.getOrNull(contactIndex - 5) ?: poseFrames.first(),
            contact = poseFrames[contactIndex],
            followThrough = poseFrames.getOrNull(contactIndex + 10) ?: poseFrames.last()
        )
    }
    
    private fun calculatePreparationScore(keyFrames: KeyFrames, reference: ReferencePose): DimensionScore {
        val actualAngle = calculateShoulderElbowWristAngle(keyFrames.preparation)
        val referenceAngle = reference.idealAngles["preparation_angle"] ?: 90f
        
        val score = calculateAngleScore(actualAngle, referenceAngle, 15f)
        
        return DimensionScore(
            dimension = ScoringDimension.PREPARATION,
            score = score,
            weight = 0.2f,
            feedback = getPreparationFeedback(score)
        )
    }
    
    private fun calculateBackswingScore(keyFrames: KeyFrames, reference: ReferencePose): DimensionScore {
        val actualAngle = calculateShoulderHipKneeAngle(keyFrames.backswing)
        val referenceAngle = reference.idealAngles["backswing_angle"] ?: 120f
        
        val score = calculateAngleScore(actualAngle, referenceAngle, 20f)
        
        return DimensionScore(
            dimension = ScoringDimension.BACKSWING,
            score = score,
            weight = 0.15f,
            feedback = getBackswingFeedback(score)
        )
    }
    
    private fun calculateContactPointScore(keyFrames: KeyFrames, reference: ReferencePose): DimensionScore {
        val actualPosition = keyFrames.contact.landmarks[15]
        val referencePosition = reference.keyFrames.firstOrNull()?.landmarks?.get(15) ?: actualPosition
        
        val positionDiff = sqrt(
            (actualPosition.x - referencePosition.x).pow(2) +
            (actualPosition.y - referencePosition.y).pow(2)
        )
        
        val score = max(0, 100 - (positionDiff * 500).toInt())
        
        return DimensionScore(
            dimension = ScoringDimension.CONTACT_POINT,
            score = score,
            weight = 0.25f,
            feedback = getContactPointFeedback(score)
        )
    }
    
    private fun calculateFollowThroughScore(keyFrames: KeyFrames, reference: ReferencePose): DimensionScore {
        val actualAngle = calculateArmFollowThroughAngle(keyFrames.followThrough)
        val referenceAngle = reference.idealAngles["follow_through_angle"] ?: 160f
        
        val score = calculateAngleScore(actualAngle, referenceAngle, 25f)
        
        return DimensionScore(
            dimension = ScoringDimension.FOLLOW_THROUGH,
            score = score,
            weight = 0.15f,
            feedback = getFollowThroughFeedback(score)
        )
    }
    
    private fun calculateTimingScore(keyFrames: KeyFrames, reference: ReferencePose): DimensionScore {
        val actualDuration = keyFrames.followThrough.timestamp - keyFrames.preparation.timestamp
        val referenceDuration = (reference.keyFrames.lastOrNull()?.timestamp ?: 1000L) - (reference.keyFrames.firstOrNull()?.timestamp ?: 0L)
        
        val timingDiff = abs(actualDuration - referenceDuration).toFloat() / referenceDuration.toFloat()
        val score = max(0, 100 - (timingDiff * 200).toInt())
        
        return DimensionScore(
            dimension = ScoringDimension.TIMING,
            score = score,
            weight = 0.15f,
            feedback = getTimingFeedback(score)
        )
    }
    
    private fun calculateFootworkScore(keyFrames: KeyFrames, reference: ReferencePose): DimensionScore {
        val leftAnkle = keyFrames.contact.landmarks[27]
        val rightAnkle = keyFrames.contact.landmarks[28]
        
        val stanceWidth = abs(leftAnkle.x - rightAnkle.x)
        val referenceStance = reference.idealAngles["stance_width"] ?: 0.3f
        
        val score = calculateAngleScore(stanceWidth, referenceStance, 0.1f)
        
        return DimensionScore(
            dimension = ScoringDimension.FOOTWORK,
            score = score,
            weight = 0.1f,
            feedback = getFootworkFeedback(score)
        )
    }
    
    private fun calculateAngleScore(actual: Float, reference: Float, tolerance: Float): Int {
        val diff = abs(actual - reference)
        return when {
            diff <= tolerance * 0.5f -> 100
            diff <= tolerance -> 90
            diff <= tolerance * 1.5f -> 80
            diff <= tolerance * 2f -> 70
            diff <= tolerance * 3f -> 60
            else -> max(0, 100 - (diff / tolerance * 20).toInt())
        }
    }
    
    private fun calculateShoulderElbowWristAngle(frame: PoseFrame): Float {
        val shoulder = frame.landmarks[11]
        val elbow = frame.landmarks[13]
        val wrist = frame.landmarks[15]
        
        val v1x = elbow.x - shoulder.x
        val v1y = elbow.y - shoulder.y
        val v2x = wrist.x - elbow.x
        val v2y = wrist.y - elbow.y
        
        val dot = v1x * v2x + v1y * v2y
        val det = v1x * v2y - v1y * v2x
        return atan2(det, dot) * (180 / PI.toFloat())
    }
    
    private fun calculateShoulderHipKneeAngle(frame: PoseFrame): Float {
        val shoulder = frame.landmarks[11]
        val hip = frame.landmarks[23]
        val knee = frame.landmarks[25]
        
        val v1x = hip.x - shoulder.x
        val v1y = hip.y - shoulder.y
        val v2x = knee.x - hip.x
        val v2y = knee.y - hip.y
        
        val dot = v1x * v2x + v1y * v2y
        val det = v1x * v2y - v1y * v2x
        return atan2(det, dot) * (180 / PI.toFloat())
    }
    
    private fun calculateArmFollowThroughAngle(frame: PoseFrame): Float {
        val shoulder = frame.landmarks[11]
        val elbow = frame.landmarks[13]
        val hip = frame.landmarks[23]
        
        val v1x = elbow.x - shoulder.x
        val v1y = elbow.y - shoulder.y
        val v2x = hip.x - shoulder.x
        val v2y = hip.y - shoulder.y
        
        val dot = v1x * v2x + v1y * v2y
        val det = v1x * v2y - v1y * v2x
        return atan2(det, dot) * (180 / PI.toFloat())
    }
    
    private fun getPreparationFeedback(score: Int): String {
        return when {
            score >= 90 -> "Excellent starting position!"
            score >= 80 -> "Good preparation, minor adjustments needed."
            score >= 70 -> "Average preparation, focus on getting into ready stance."
            score >= 60 -> "Needs improvement, ensure proper racket back positioning."
            else -> "Poor preparation, work on your starting stance and posture."
        }
    }
    
    private fun getBackswingFeedback(score: Int): String {
        return when {
            score >= 90 -> "Perfect backswing technique!"
            score >= 80 -> "Good backswing, adjust arm is properly loaded."
            score >= 70 -> "Average backswing, ensure you rotate your shoulders fully."
            score >= 60 -> "Needs improvement, get more shoulder rotation."
            else -> "Poor backswing, work on rotating your body correctly for more power."
        }
    }
    
    private fun getContactPointFeedback(score: Int): String {
        return when {
            score >= 90 -> "Excellent contact point!"
            score >= 80 -> "Good contact point, slightly off the point in front of your body."
            score >= 70 -> "Average contact point, try to hit the shuttle earlier."
            score >= 60 -> "Needs improvement, contact point is too far back."
            else -> "Poor contact point, focus on hitting in front of your body."
        }
    }
    
    private fun getFollowThroughFeedback(score: Int): String {
        return when {
            score >= 90 -> "Perfect follow through!"
            score >= 80 -> "Good follow through, great momentum transfer."
            score >= 70 -> "Average follow through, complete your swing fully."
            score >= 60 -> "Needs improvement, follow through across your body."
            else -> "Poor follow through, finish your swing completely."
        }
    }
    
    private fun getTimingFeedback(score: Int): String {
        return when {
            score >= 90 -> "Excellent timing!"
            score >= 80 -> "Good timing, smooth and rhythm was almost perfect."
            score >= 70 -> "Average timing, slightly off, adjust your pace."
            score >= 60 -> "Needs improvement, work on your swing tempo."
            else -> "Poor timing, practice the motion sequence slowly."
        }
    }
    
    private fun getFootworkFeedback(score: Int): String {
        return when {
            score >= 90 -> "Excellent footwork!"
            score >= 80 -> "Good footwork, stable and balance."
            score >= 70 -> "Average footwork, adjust your stance width."
            score >= 60 -> "Needs improvement, move your feet faster."
            else -> "Poor footwork, work on your stance and movement."
        }
    }
    
    data class KeyFrames(
        val preparation: PoseFrame,
        val backswing: PoseFrame,
        val contact: PoseFrame,
        val followThrough: PoseFrame
    )
}
