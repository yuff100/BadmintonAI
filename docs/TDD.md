# BadmintonAI - Technical Design Document

## Document Info

| Item | Value |
|------|-------|
| Version | 1.0 |
| Created | March 2026 |
| Last Updated | March 10, 2026 |
| Related PRD | [PRD.md](./PRD.md) |

---

## 1. System Overview

### 1.1 Architecture Pattern

采用 **Clean Architecture + MVVM** 分层架构：

```
┌─────────────────────────────────────────────────────────────────┐
│                         App Module                               │
├─────────────────────────────────────────────────────────────────┤
│  Presentation Layer (UI)                                         │
│  ├── Screens (Jetpack Compose)                                  │
│  ├── ViewModels                                                 │
│  └── UI State & Events                                          │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer (Business Logic)                                   │
│  ├── Use Cases                                                  │
│  ├── Domain Models                                              │
│  └── Repository Interfaces                                      │
├─────────────────────────────────────────────────────────────────┤
│  Data Layer                                                      │
│  ├── Repository Implementations                                 │
│  ├── Local Data Sources (Room, DataStore)                       │
│  └── File Storage (Videos)                                      │
├─────────────────────────────────────────────────────────────────┤
│  ML Layer                                                        │
│  ├── MediaPipe Pose Detector                                    │
│  ├── Stroke Classifier (TFLite)                                 │
│  └── Scoring Engine                                             │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Module Structure

```
BadmintonAI/
├── app/                          # Application module
│   ├── src/main/
│   │   ├── java/com/badmintonai/
│   │   │   ├── App.kt           # Application class
│   │   │   ├── di/              # Hilt DI modules
│   │   │   ├── ui/              # Presentation layer
│   │   │   │   ├── navigation/  # Navigation setup
│   │   │   │   ├── home/        # Home screen
│   │   │   │   ├── recording/   # Recording screen
│   │   │   │   ├── analysis/    # Analysis & results
│   │   │   │   ├── history/     # History screen
│   │   │   │   └── components/  # Shared UI components
│   │   │   ├── domain/          # Domain layer
│   │   │   │   ├── model/       # Domain entities
│   │   │   │   ├── usecase/     # Use cases
│   │   │   │   └── repository/  # Repository interfaces
│   │   │   ├── data/            # Data layer
│   │   │   │   ├── repository/  # Repository implementations
│   │   │   │   ├── local/       # Room DB, DataStore
│   │   │   │   └── mapper/      # Data mappers
│   │   │   └── ml/              # ML layer
│   │   │       ├── pose/        # MediaPipe integration
│   │   │       ├── classifier/  # Stroke classifier
│   │   │       ├── scoring/     # Scoring engine
│   │   │       └── feedback/    # Feedback generator
│   │   ├── res/
│   │   └── assets/
│   │       └── models/          # TFLite models
│   └── build.gradle.kts
├── docs/                         # Documentation
├── gradle/
└── build.gradle.kts
```

---

## 2. Technology Stack

### 2.1 Core Dependencies

```kotlin
// build.gradle.kts (app module)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.badmintonai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.badmintonai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // MediaPipe
    implementation("com.google.mediapipe:tasks-vision:0.10.10")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-video:2.5.0")

    // Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
```

### 2.2 Version Constraints

| Component | Version | Rationale |
|-----------|---------|-----------|
| Kotlin | 1.9.22 | Latest stable, Compose compiler compatible |
| Compose BOM | 2024.02.00 | Stable compose versions |
| MediaPipe | 0.10.10 | Latest with Android Tasks Vision API |
| TFLite | 2.14.0 | Stable, good Android support |
| Room | 2.6.1 | Latest stable with KSP |
| minSdk | 26 | Android 8.0, covers 95%+ devices |

---

## 3. Domain Layer Design

### 3.1 Domain Models

```kotlin
// domain/model/Landmark.kt
data class Landmark(
    val x: Float,           // Normalized x (0-1)
    val y: Float,           // Normalized y (0-1)
    val z: Float,           // Depth (relative)
    val visibility: Float   // Confidence (0-1)
)

// domain/model/PoseFrame.kt
data class PoseFrame(
    val frameIndex: Int,
    val timestampMs: Long,
    val landmarks: List<Landmark>  // 33 landmarks
) {
    companion object {
        const val LANDMARK_COUNT = 33
        
        // MediaPipe landmark indices
        const val NOSE = 0
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
        const val LEFT_KNEE = 25
        const val RIGHT_KNEE = 26
        const val LEFT_ANKLE = 27
        const val RIGHT_ANKLE = 28
    }
}

// domain/model/StrokeType.kt
enum class StrokeType(val displayName: String, val displayNameCn: String) {
    FOREHAND_CLEAR("Forehand Clear", "正手高远球"),
    SMASH("Smash", "杀球"),
    DROP_SHOT("Drop Shot", "吊球"),
    SERVE("Serve", "发球"),
    NET_SHOT("Net Shot", "网前球"),
    UNKNOWN("Unknown", "未知")
}

// domain/model/StrokePhase.kt
enum class StrokePhase {
    PREPARATION,    // Ready position
    BACKSWING,      // Racket back
    FORWARD_SWING,  // Acceleration
    CONTACT,        // Ball contact
    FOLLOW_THROUGH  // Completion
}

// domain/model/ScoreDimension.kt
enum class ScoreDimension(
    val displayName: String,
    val displayNameCn: String,
    val weight: Float
) {
    PREPARATION("Preparation", "准备动作", 0.20f),
    BACKSWING("Backswing", "引拍", 0.15f),
    CONTACT_POINT("Contact Point", "击球点", 0.25f),
    FOLLOW_THROUGH("Follow Through", "随挥", 0.15f),
    TIMING_RHYTHM("Timing & Rhythm", "节奏", 0.15f),
    FOOTWORK("Footwork", "步法", 0.10f)
}

// domain/model/DimensionScore.kt
data class DimensionScore(
    val dimension: ScoreDimension,
    val score: Int,                    // 0-100
    val feedback: String,
    val feedbackCn: String,
    val issues: List<TechniqueIssue>
)

// domain/model/TechniqueIssue.kt
data class TechniqueIssue(
    val severity: IssueSeverity,
    val description: String,
    val descriptionCn: String,
    val suggestion: String,
    val suggestionCn: String,
    val affectedFrames: IntRange?
)

enum class IssueSeverity { GOOD, WARNING, ERROR }

// domain/model/AnalysisResult.kt
data class AnalysisResult(
    val id: String,
    val videoUri: String,
    val strokeType: StrokeType,
    val overallScore: Int,
    val dimensionScores: List<DimensionScore>,
    val poseFrames: List<PoseFrame>,
    val keyFrameIndices: KeyFrameIndices,
    val analyzedAt: Long,
    val durationMs: Long
)

data class KeyFrameIndices(
    val preparationFrame: Int,
    val backswingPeakFrame: Int,
    val contactFrame: Int,
    val followThroughEndFrame: Int
)

// domain/model/AnalysisSummary.kt (for history list)
data class AnalysisSummary(
    val id: String,
    val strokeType: StrokeType,
    val overallScore: Int,
    val analyzedAt: Long,
    val thumbnailUri: String?
)
```

### 3.2 Repository Interfaces

```kotlin
// domain/repository/AnalysisRepository.kt
interface AnalysisRepository {
    suspend fun saveAnalysis(result: AnalysisResult)
    suspend fun getAnalysis(id: String): AnalysisResult?
    suspend fun getAnalysisSummaries(
        strokeType: StrokeType? = null,
        limit: Int = 50
    ): List<AnalysisSummary>
    suspend fun getScoreTrend(
        strokeType: StrokeType?,
        days: Int = 7
    ): List<Pair<Long, Int>>  // timestamp to score
    suspend fun deleteAnalysis(id: String)
    fun observeRecentAnalyses(limit: Int): Flow<List<AnalysisSummary>>
}

// domain/repository/VideoRepository.kt
interface VideoRepository {
    suspend fun saveVideo(uri: Uri): String  // Returns saved path
    suspend fun deleteVideo(path: String)
    suspend fun getVideoThumbnail(path: String): Bitmap?
    fun getVideosDirectory(): File
}

// domain/repository/PreferencesRepository.kt
interface PreferencesRepository {
    val analysisQuality: Flow<AnalysisQuality>
    val language: Flow<AppLanguage>
    suspend fun setAnalysisQuality(quality: AnalysisQuality)
    suspend fun setLanguage(language: AppLanguage)
}

enum class AnalysisQuality { FAST, BALANCED, ACCURATE }
enum class AppLanguage { CHINESE, ENGLISH }
```

### 3.3 Use Cases

```kotlin
// domain/usecase/AnalyzeVideoUseCase.kt
class AnalyzeVideoUseCase @Inject constructor(
    private val poseDetector: PoseDetector,
    private val strokeClassifier: StrokeClassifier,
    private val scoringEngine: ScoringEngine,
    private val feedbackGenerator: FeedbackGenerator,
    private val analysisRepository: AnalysisRepository
) {
    sealed class Progress {
        data object Decoding : Progress()
        data class PoseDetection(val percent: Int) : Progress()
        data object StrokeClassification : Progress()
        data object Scoring : Progress()
        data object GeneratingFeedback : Progress()
        data class Completed(val result: AnalysisResult) : Progress()
        data class Error(val message: String) : Progress()
    }

    operator fun invoke(
        videoUri: Uri,
        preSelectedStroke: StrokeType? = null
    ): Flow<Progress> = flow {
        emit(Progress.Decoding)
        
        // 1. Extract frames and detect poses
        val poseFrames = mutableListOf<PoseFrame>()
        poseDetector.detectFromVideo(videoUri)
            .collect { (frameIndex, frame) ->
                poseFrames.add(frame)
                emit(Progress.PoseDetection((frameIndex * 100) / totalFrames))
            }
        
        // 2. Classify stroke type
        emit(Progress.StrokeClassification)
        val strokeType = preSelectedStroke 
            ?: strokeClassifier.classify(poseFrames)
        
        // 3. Calculate scores
        emit(Progress.Scoring)
        val keyFrames = detectKeyFrames(poseFrames, strokeType)
        val dimensionScores = scoringEngine.score(poseFrames, strokeType, keyFrames)
        
        // 4. Generate feedback
        emit(Progress.GeneratingFeedback)
        val enrichedScores = feedbackGenerator.generateFeedback(
            dimensionScores, strokeType
        )
        
        // 5. Build result
        val result = AnalysisResult(
            id = UUID.randomUUID().toString(),
            videoUri = videoUri.toString(),
            strokeType = strokeType,
            overallScore = calculateOverallScore(enrichedScores),
            dimensionScores = enrichedScores,
            poseFrames = poseFrames,
            keyFrameIndices = keyFrames,
            analyzedAt = System.currentTimeMillis(),
            durationMs = calculateDuration(poseFrames)
        )
        
        // 6. Save to repository
        analysisRepository.saveAnalysis(result)
        
        emit(Progress.Completed(result))
    }.catch { e ->
        emit(Progress.Error(e.message ?: "Unknown error"))
    }
}

// domain/usecase/RecordVideoUseCase.kt
class RecordVideoUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend fun saveRecording(tempUri: Uri): String {
        return videoRepository.saveVideo(tempUri)
    }
}

// domain/usecase/GetAnalysisHistoryUseCase.kt
class GetAnalysisHistoryUseCase @Inject constructor(
    private val analysisRepository: AnalysisRepository
) {
    operator fun invoke(
        strokeType: StrokeType? = null,
        limit: Int = 50
    ): Flow<List<AnalysisSummary>> = flow {
        emit(analysisRepository.getAnalysisSummaries(strokeType, limit))
    }
}

// domain/usecase/GetScoreTrendUseCase.kt
class GetScoreTrendUseCase @Inject constructor(
    private val analysisRepository: AnalysisRepository
) {
    suspend operator fun invoke(
        strokeType: StrokeType? = null,
        days: Int = 7
    ): List<Pair<Long, Int>> {
        return analysisRepository.getScoreTrend(strokeType, days)
    }
}
```

---

## 4. Data Layer Design

### 4.1 Room Database

```kotlin
// data/local/AppDatabase.kt
@Database(
    entities = [AnalysisEntity::class, DimensionScoreEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
}

// data/local/entity/AnalysisEntity.kt
@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "video_uri") val videoUri: String,
    @ColumnInfo(name = "stroke_type") val strokeType: String,
    @ColumnInfo(name = "overall_score") val overallScore: Int,
    @ColumnInfo(name = "pose_frames_json") val poseFramesJson: String,
    @ColumnInfo(name = "key_frames_json") val keyFramesJson: String,
    @ColumnInfo(name = "analyzed_at") val analyzedAt: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String?
)

@Entity(
    tableName = "dimension_scores",
    foreignKeys = [ForeignKey(
        entity = AnalysisEntity::class,
        parentColumns = ["id"],
        childColumns = ["analysis_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DimensionScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "analysis_id") val analysisId: String,
    @ColumnInfo(name = "dimension") val dimension: String,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "feedback") val feedback: String,
    @ColumnInfo(name = "feedback_cn") val feedbackCn: String,
    @ColumnInfo(name = "issues_json") val issuesJson: String
)

// data/local/dao/AnalysisDao.kt
@Dao
interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AnalysisEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDimensionScores(scores: List<DimensionScoreEntity>)

    @Transaction
    suspend fun insertAnalysisWithScores(
        analysis: AnalysisEntity,
        scores: List<DimensionScoreEntity>
    ) {
        insertAnalysis(analysis)
        insertDimensionScores(scores)
    }

    @Query("SELECT * FROM analyses WHERE id = :id")
    suspend fun getAnalysis(id: String): AnalysisEntity?

    @Query("SELECT * FROM dimension_scores WHERE analysis_id = :analysisId")
    suspend fun getDimensionScores(analysisId: String): List<DimensionScoreEntity>

    @Query("""
        SELECT id, stroke_type, overall_score, analyzed_at, thumbnail_uri 
        FROM analyses 
        WHERE (:strokeType IS NULL OR stroke_type = :strokeType)
        ORDER BY analyzed_at DESC 
        LIMIT :limit
    """)
    suspend fun getAnalysisSummaries(
        strokeType: String?,
        limit: Int
    ): List<AnalysisSummaryTuple>

    @Query("""
        SELECT analyzed_at, overall_score 
        FROM analyses 
        WHERE (:strokeType IS NULL OR stroke_type = :strokeType)
        AND analyzed_at >= :sinceTimestamp
        ORDER BY analyzed_at ASC
    """)
    suspend fun getScoreTrend(
        strokeType: String?,
        sinceTimestamp: Long
    ): List<ScoreTrendTuple>

    @Query("SELECT * FROM analyses ORDER BY analyzed_at DESC LIMIT :limit")
    fun observeRecentAnalyses(limit: Int): Flow<List<AnalysisSummaryTuple>>

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteAnalysis(id: String)
}

data class AnalysisSummaryTuple(
    val id: String,
    @ColumnInfo(name = "stroke_type") val strokeType: String,
    @ColumnInfo(name = "overall_score") val overallScore: Int,
    @ColumnInfo(name = "analyzed_at") val analyzedAt: Long,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String?
)

data class ScoreTrendTuple(
    @ColumnInfo(name = "analyzed_at") val analyzedAt: Long,
    @ColumnInfo(name = "overall_score") val overallScore: Int
)
```

### 4.2 DataStore Preferences

```kotlin
// data/local/PreferencesDataStore.kt
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private object Keys {
        val ANALYSIS_QUALITY = stringPreferencesKey("analysis_quality")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val analysisQuality: Flow<AnalysisQuality> = context.dataStore.data
        .map { prefs ->
            prefs[Keys.ANALYSIS_QUALITY]?.let { 
                AnalysisQuality.valueOf(it) 
            } ?: AnalysisQuality.BALANCED
        }

    val language: Flow<AppLanguage> = context.dataStore.data
        .map { prefs ->
            prefs[Keys.LANGUAGE]?.let { 
                AppLanguage.valueOf(it) 
            } ?: AppLanguage.CHINESE
        }

    suspend fun setAnalysisQuality(quality: AnalysisQuality) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ANALYSIS_QUALITY] = quality.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.name
        }
    }
}
```

### 4.3 Repository Implementations

```kotlin
// data/repository/AnalysisRepositoryImpl.kt
class AnalysisRepositoryImpl @Inject constructor(
    private val analysisDao: AnalysisDao,
    private val mapper: AnalysisMapper
) : AnalysisRepository {

    override suspend fun saveAnalysis(result: AnalysisResult) {
        val entity = mapper.toEntity(result)
        val scoreEntities = mapper.toScoreEntities(result.id, result.dimensionScores)
        analysisDao.insertAnalysisWithScores(entity, scoreEntities)
    }

    override suspend fun getAnalysis(id: String): AnalysisResult? {
        val entity = analysisDao.getAnalysis(id) ?: return null
        val scores = analysisDao.getDimensionScores(id)
        return mapper.toDomain(entity, scores)
    }

    override suspend fun getAnalysisSummaries(
        strokeType: StrokeType?,
        limit: Int
    ): List<AnalysisSummary> {
        return analysisDao.getAnalysisSummaries(strokeType?.name, limit)
            .map { mapper.toSummary(it) }
    }

    override suspend fun getScoreTrend(
        strokeType: StrokeType?,
        days: Int
    ): List<Pair<Long, Int>> {
        val sinceTimestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return analysisDao.getScoreTrend(strokeType?.name, sinceTimestamp)
            .map { it.analyzedAt to it.overallScore }
    }

    override suspend fun deleteAnalysis(id: String) {
        analysisDao.deleteAnalysis(id)
    }

    override fun observeRecentAnalyses(limit: Int): Flow<List<AnalysisSummary>> {
        return analysisDao.observeRecentAnalyses(limit)
            .map { list -> list.map { mapper.toSummary(it) } }
    }
}
```

---

## 5. ML Layer Design

### 5.1 MediaPipe Pose Detector

```kotlin
// ml/pose/MediaPipePoseDetector.kt
class MediaPipePoseDetector @Inject constructor(
    @ApplicationContext private val context: Context
) : PoseDetector {

    private var poseLandmarker: PoseLandmarker? = null

    private fun getOrCreateLandmarker(): PoseLandmarker {
        return poseLandmarker ?: run {
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath("pose_landmarker_heavy.task")
                        .setDelegate(Delegate.GPU)  // Use GPU if available
                        .build()
                )
                .setRunningMode(RunningMode.VIDEO)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()

            PoseLandmarker.createFromOptions(context, options).also {
                poseLandmarker = it
            }
        }
    }

    override fun detectFromVideo(videoUri: Uri): Flow<Pair<Int, PoseFrame>> = flow {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)

        val durationMs = retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toLongOrNull() ?: 0L

        val frameRate = 30 // Target frame rate
        val frameIntervalMs = 1000L / frameRate
        val landmarker = getOrCreateLandmarker()

        var frameIndex = 0
        var currentTimeMs = 0L

        while (currentTimeMs < durationMs) {
            val bitmap = retriever.getFrameAtTime(
                currentTimeMs * 1000, // Convert to microseconds
                MediaMetadataRetriever.OPTION_CLOSEST
            )

            if (bitmap != null) {
                val mpImage = BitmapImageBuilder(bitmap).build()
                val result = landmarker.detectForVideo(mpImage, currentTimeMs)

                if (result.landmarks().isNotEmpty()) {
                    val landmarks = result.landmarks()[0].map { landmark ->
                        Landmark(
                            x = landmark.x(),
                            y = landmark.y(),
                            z = landmark.z(),
                            visibility = landmark.visibility().orElse(0f)
                        )
                    }

                    emit(frameIndex to PoseFrame(
                        frameIndex = frameIndex,
                        timestampMs = currentTimeMs,
                        landmarks = landmarks
                    ))
                }

                bitmap.recycle()
            }

            frameIndex++
            currentTimeMs += frameIntervalMs
        }

        retriever.release()
    }.flowOn(Dispatchers.Default)

    override fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
```

### 5.2 Stroke Classifier

```kotlin
// ml/classifier/TFLiteStrokeClassifier.kt
class TFLiteStrokeClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) : StrokeClassifier {

    private val interpreter: Interpreter by lazy {
        val modelBuffer = loadModelFile("stroke_classifier.tflite")
        Interpreter(modelBuffer, Interpreter.Options().apply {
            setNumThreads(4)
        })
    }

    override suspend fun classify(poseFrames: List<PoseFrame>): StrokeType = 
        withContext(Dispatchers.Default) {
            // Prepare input: sequence of key angles over time
            val inputFeatures = extractFeatures(poseFrames)
            val inputBuffer = FloatBuffer.allocate(inputFeatures.size)
            inputFeatures.forEach { inputBuffer.put(it) }
            inputBuffer.rewind()

            // Output: probability for each stroke type
            val outputBuffer = FloatBuffer.allocate(StrokeType.values().size - 1) // Exclude UNKNOWN

            interpreter.run(inputBuffer, outputBuffer)

            outputBuffer.rewind()
            val probabilities = FloatArray(outputBuffer.capacity())
            outputBuffer.get(probabilities)

            // Return highest probability stroke type
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            StrokeType.values()[maxIndex]
        }

    private fun extractFeatures(frames: List<PoseFrame>): FloatArray {
        // Extract temporal sequence of joint angles
        // Input shape: [1, SEQUENCE_LENGTH, NUM_FEATURES]
        val sequenceLength = 60  // 2 seconds at 30fps
        val numFeatures = 12     // Key angles and positions

        val features = FloatArray(sequenceLength * numFeatures)
        val sampledFrames = sampleFrames(frames, sequenceLength)

        sampledFrames.forEachIndexed { i, frame ->
            val angles = calculateKeyAngles(frame)
            angles.forEachIndexed { j, angle ->
                features[i * numFeatures + j] = angle
            }
        }

        return features
    }

    private fun calculateKeyAngles(frame: PoseFrame): FloatArray {
        val lm = frame.landmarks
        return floatArrayOf(
            // Right arm angles (dominant arm for most players)
            calculateAngle(lm[12], lm[14], lm[16]),  // Shoulder-Elbow-Wrist
            calculateAngle(lm[14], lm[12], lm[24]),  // Elbow-Shoulder-Hip
            // Left arm angles
            calculateAngle(lm[11], lm[13], lm[15]),
            calculateAngle(lm[13], lm[11], lm[23]),
            // Trunk angles
            calculateAngle(lm[11], lm[23], lm[25]),  // Shoulder-Hip-Knee (left)
            calculateAngle(lm[12], lm[24], lm[26]),  // Shoulder-Hip-Knee (right)
            // Wrist heights relative to head
            (lm[16].y - lm[0].y),  // Right wrist
            (lm[15].y - lm[0].y),  // Left wrist
            // Hip rotation indicator
            (lm[24].x - lm[23].x),
            // Shoulder rotation indicator
            (lm[12].x - lm[11].x),
            // Knee bend
            calculateAngle(lm[24], lm[26], lm[28]),
            calculateAngle(lm[23], lm[25], lm[27])
        )
    }

    private fun calculateAngle(a: Landmark, b: Landmark, c: Landmark): Float {
        val ab = floatArrayOf(a.x - b.x, a.y - b.y)
        val cb = floatArrayOf(c.x - b.x, c.y - b.y)

        val dot = ab[0] * cb[0] + ab[1] * cb[1]
        val magAB = sqrt(ab[0] * ab[0] + ab[1] * ab[1])
        val magCB = sqrt(cb[0] * cb[0] + cb[1] * cb[1])

        val cosAngle = dot / (magAB * magCB + 1e-6f)
        return acos(cosAngle.coerceIn(-1f, 1f)) * (180f / PI.toFloat())
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val assetFd = context.assets.openFd(filename)
        val inputStream = FileInputStream(assetFd.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset,
            assetFd.declaredLength
        )
    }
}
```

### 5.3 Scoring Engine

```kotlin
// ml/scoring/ScoringEngine.kt
class ScoringEngine @Inject constructor() {

    fun score(
        frames: List<PoseFrame>,
        strokeType: StrokeType,
        keyFrames: KeyFrameIndices
    ): List<DimensionScore> {
        val referenceModel = getReferenceModel(strokeType)

        return ScoreDimension.values().map { dimension ->
            val score = when (dimension) {
                ScoreDimension.PREPARATION -> scorePreparation(frames, keyFrames, referenceModel)
                ScoreDimension.BACKSWING -> scoreBackswing(frames, keyFrames, referenceModel)
                ScoreDimension.CONTACT_POINT -> scoreContactPoint(frames, keyFrames, referenceModel)
                ScoreDimension.FOLLOW_THROUGH -> scoreFollowThrough(frames, keyFrames, referenceModel)
                ScoreDimension.TIMING_RHYTHM -> scoreTimingRhythm(frames, keyFrames, referenceModel)
                ScoreDimension.FOOTWORK -> scoreFootwork(frames, keyFrames, referenceModel)
            }
            DimensionScore(
                dimension = dimension,
                score = score.score,
                feedback = score.feedback,
                feedbackCn = score.feedbackCn,
                issues = score.issues
            )
        }
    }

    private fun scorePreparation(
        frames: List<PoseFrame>,
        keyFrames: KeyFrameIndices,
        reference: ReferenceModel
    ): ScoringResult {
        val prepFrame = frames[keyFrames.preparationFrame]
        val issues = mutableListOf<TechniqueIssue>()
        var score = 100

        // Check ready position - feet shoulder width apart
        val hipWidth = abs(prepFrame.landmarks[23].x - prepFrame.landmarks[24].x)
        val ankleWidth = abs(prepFrame.landmarks[27].x - prepFrame.landmarks[28].x)
        val stanceRatio = ankleWidth / hipWidth

        if (stanceRatio < 0.8f) {
            score -= 15
            issues.add(TechniqueIssue(
                severity = IssueSeverity.WARNING,
                description = "Stance too narrow",
                descriptionCn = "站姿过窄",
                suggestion = "Position feet shoulder-width apart for better balance",
                suggestionCn = "双脚与肩同宽，保持稳定",
                affectedFrames = null
            ))
        }

        // Check racket preparation (using wrist position as proxy)
        val rightWrist = prepFrame.landmarks[PoseFrame.RIGHT_WRIST]
        val rightShoulder = prepFrame.landmarks[PoseFrame.RIGHT_SHOULDER]
        
        if (rightWrist.y > rightShoulder.y + 0.1f) {
            score -= 10
            issues.add(TechniqueIssue(
                severity = IssueSeverity.WARNING,
                description = "Racket held too low",
                descriptionCn = "球拍位置过低",
                suggestion = "Keep racket at chest height in ready position",
                suggestionCn = "准备姿势时球拍保持在胸前高度",
                affectedFrames = null
            ))
        }

        // Check weight distribution (approximate via hip position)
        val kneeAngleLeft = calculateAngle(
            prepFrame.landmarks[23],
            prepFrame.landmarks[25],
            prepFrame.landmarks[27]
        )
        if (kneeAngleLeft > 170f) {
            score -= 10
            issues.add(TechniqueIssue(
                severity = IssueSeverity.WARNING,
                description = "Knees too straight",
                descriptionCn = "膝盖过直",
                suggestion = "Bend knees slightly for quick movement",
                suggestionCn = "微屈膝盖，便于快速移动",
                affectedFrames = null
            ))
        }

        return ScoringResult(
            score = score.coerceIn(0, 100),
            feedback = if (score >= 80) "Good ready position" else "Improve your ready stance",
            feedbackCn = if (score >= 80) "准备姿势良好" else "需改善准备姿势",
            issues = issues
        )
    }

    private fun scoreContactPoint(
        frames: List<PoseFrame>,
        keyFrames: KeyFrameIndices,
        reference: ReferenceModel
    ): ScoringResult {
        val contactFrame = frames[keyFrames.contactFrame]
        val issues = mutableListOf<TechniqueIssue>()
        var score = 100

        // Check contact height - should be above head for overhead strokes
        val rightWrist = contactFrame.landmarks[PoseFrame.RIGHT_WRIST]
        val head = contactFrame.landmarks[PoseFrame.NOSE]

        val contactHeightRatio = (head.y - rightWrist.y) / head.y
        
        if (contactHeightRatio < reference.minContactHeightRatio) {
            val penalty = ((reference.minContactHeightRatio - contactHeightRatio) * 100).toInt()
            score -= penalty.coerceAtMost(30)
            issues.add(TechniqueIssue(
                severity = if (penalty > 20) IssueSeverity.ERROR else IssueSeverity.WARNING,
                description = "Contact point too low",
                descriptionCn = "击球点过低",
                suggestion = "Strike the shuttle at the highest point above your head",
                suggestionCn = "在头顶最高点击球，获得更大力量",
                affectedFrames = keyFrames.contactFrame..keyFrames.contactFrame
            ))
        }

        // Check arm extension
        val rightShoulder = contactFrame.landmarks[PoseFrame.RIGHT_SHOULDER]
        val rightElbow = contactFrame.landmarks[PoseFrame.RIGHT_ELBOW]
        val elbowAngle = calculateAngle(rightShoulder, rightElbow, rightWrist)

        if (elbowAngle < reference.minElbowAngleAtContact) {
            score -= 20
            issues.add(TechniqueIssue(
                severity = IssueSeverity.WARNING,
                description = "Arm not fully extended at contact",
                descriptionCn = "击球时手臂未完全伸展",
                suggestion = "Extend arm fully at the point of contact",
                suggestionCn = "击球瞬间充分伸展手臂",
                affectedFrames = keyFrames.contactFrame..keyFrames.contactFrame
            ))
        }

        // Check body alignment
        val rightHip = contactFrame.landmarks[PoseFrame.RIGHT_HIP]
        val shoulderHipAngle = calculateAngle(rightWrist, rightShoulder, rightHip)

        if (abs(shoulderHipAngle - reference.idealShoulderHipAngle) > 20f) {
            score -= 15
            issues.add(TechniqueIssue(
                severity = IssueSeverity.WARNING,
                description = "Body alignment off at contact",
                descriptionCn = "击球时身体对位不正",
                suggestion = "Align body towards target at contact",
                suggestionCn = "击球时身体朝向目标方向",
                affectedFrames = keyFrames.contactFrame..keyFrames.contactFrame
            ))
        }

        return ScoringResult(
            score = score.coerceIn(0, 100),
            feedback = when {
                score >= 85 -> "Excellent contact point"
                score >= 70 -> "Good contact, minor adjustments needed"
                else -> "Work on your contact point"
            },
            feedbackCn = when {
                score >= 85 -> "击球点优秀"
                score >= 70 -> "击球点良好，需微调"
                else -> "需改善击球点"
            },
            issues = issues
        )
    }

    // Additional scoring methods for other dimensions...
    private fun scoreBackswing(...): ScoringResult { /* ... */ }
    private fun scoreFollowThrough(...): ScoringResult { /* ... */ }
    private fun scoreTimingRhythm(...): ScoringResult { /* ... */ }
    private fun scoreFootwork(...): ScoringResult { /* ... */ }

    private fun getReferenceModel(strokeType: StrokeType): ReferenceModel {
        return when (strokeType) {
            StrokeType.FOREHAND_CLEAR -> ReferenceModel.FOREHAND_CLEAR
            StrokeType.SMASH -> ReferenceModel.SMASH
            StrokeType.DROP_SHOT -> ReferenceModel.DROP_SHOT
            StrokeType.SERVE -> ReferenceModel.SERVE
            StrokeType.NET_SHOT -> ReferenceModel.NET_SHOT
            else -> ReferenceModel.FOREHAND_CLEAR
        }
    }

    private fun calculateAngle(a: Landmark, b: Landmark, c: Landmark): Float {
        // Same implementation as in classifier
    }

    data class ScoringResult(
        val score: Int,
        val feedback: String,
        val feedbackCn: String,
        val issues: List<TechniqueIssue>
    )
}

// ml/scoring/ReferenceModel.kt
enum class ReferenceModel(
    val minContactHeightRatio: Float,
    val minElbowAngleAtContact: Float,
    val idealShoulderHipAngle: Float,
    val idealBackswingElbowAngle: FloatRange,
    val idealFollowThroughAngle: Float
) {
    FOREHAND_CLEAR(
        minContactHeightRatio = 0.15f,     // Wrist should be 15%+ above head center
        minElbowAngleAtContact = 150f,      // Near full extension
        idealShoulderHipAngle = 160f,       // Aligned
        idealBackswingElbowAngle = 90f..120f,
        idealFollowThroughAngle = 45f
    ),
    SMASH(
        minContactHeightRatio = 0.20f,      // Higher contact for smash
        minElbowAngleAtContact = 160f,
        idealShoulderHipAngle = 170f,
        idealBackswingElbowAngle = 80f..110f,
        idealFollowThroughAngle = 60f
    ),
    DROP_SHOT(
        minContactHeightRatio = 0.10f,
        minElbowAngleAtContact = 130f,      // Less extension
        idealShoulderHipAngle = 150f,
        idealBackswingElbowAngle = 100f..130f,
        idealFollowThroughAngle = 30f
    ),
    SERVE(
        minContactHeightRatio = -0.2f,      // Below head for serve
        minElbowAngleAtContact = 140f,
        idealShoulderHipAngle = 140f,
        idealBackswingElbowAngle = 90f..120f,
        idealFollowThroughAngle = 40f
    ),
    NET_SHOT(
        minContactHeightRatio = -0.3f,
        minElbowAngleAtContact = 120f,
        idealShoulderHipAngle = 130f,
        idealBackswingElbowAngle = 110f..140f,
        idealFollowThroughAngle = 20f
    )
}

private operator fun FloatRange.contains(value: Float): Boolean {
    return value >= start && value <= endInclusive
}
```

### 5.4 Key Frame Detection

```kotlin
// ml/pose/KeyFrameDetector.kt
class KeyFrameDetector @Inject constructor() {

    fun detectKeyFrames(
        frames: List<PoseFrame>,
        strokeType: StrokeType
    ): KeyFrameIndices {
        // Find preparation frame (stable stance before motion)
        val preparationFrame = findPreparationFrame(frames)

        // Find backswing peak (maximum racket arm retraction)
        val backswingPeakFrame = findBackswingPeak(frames, preparationFrame)

        // Find contact frame (maximum arm extension / velocity peak)
        val contactFrame = findContactFrame(frames, backswingPeakFrame)

        // Find follow-through end (motion completion)
        val followThroughEndFrame = findFollowThroughEnd(frames, contactFrame)

        return KeyFrameIndices(
            preparationFrame = preparationFrame,
            backswingPeakFrame = backswingPeakFrame,
            contactFrame = contactFrame,
            followThroughEndFrame = followThroughEndFrame
        )
    }

    private fun findPreparationFrame(frames: List<PoseFrame>): Int {
        // Find first stable frame with low motion
        val windowSize = 5
        for (i in windowSize until frames.size - windowSize) {
            val motionScore = calculateMotionScore(
                frames.subList(i - windowSize, i + windowSize)
            )
            if (motionScore < STABILITY_THRESHOLD) {
                return i
            }
        }
        return 0
    }

    private fun findBackswingPeak(frames: List<PoseFrame>, startIdx: Int): Int {
        var maxBackswingIdx = startIdx
        var maxBackswingAngle = 0f

        for (i in startIdx until minOf(startIdx + 30, frames.size)) {
            val frame = frames[i]
            val elbowAngle = calculateElbowAngle(frame)

            // Backswing characterized by elbow bending
            if (elbowAngle > maxBackswingAngle && elbowAngle < 130f) {
                maxBackswingAngle = elbowAngle
                maxBackswingIdx = i
            }
        }

        return maxBackswingIdx
    }

    private fun findContactFrame(frames: List<PoseFrame>, startIdx: Int): Int {
        var maxExtensionIdx = startIdx
        var maxExtension = 0f

        for (i in startIdx until minOf(startIdx + 20, frames.size)) {
            val frame = frames[i]
            val wristHeight = frame.landmarks[PoseFrame.RIGHT_WRIST].y
            val elbowAngle = calculateElbowAngle(frame)

            // Contact: highest wrist + most extended arm
            val extensionScore = (1 - wristHeight) * elbowAngle
            if (extensionScore > maxExtension) {
                maxExtension = extensionScore
                maxExtensionIdx = i
            }
        }

        return maxExtensionIdx
    }

    private fun findFollowThroughEnd(frames: List<PoseFrame>, contactIdx: Int): Int {
        // Find when motion stabilizes after contact
        val windowSize = 3
        for (i in contactIdx + 5 until frames.size - windowSize) {
            val motionScore = calculateMotionScore(
                frames.subList(i, minOf(i + windowSize, frames.size))
            )
            if (motionScore < STABILITY_THRESHOLD * 1.5f) {
                return i
            }
        }
        return frames.lastIndex
    }

    private fun calculateMotionScore(frames: List<PoseFrame>): Float {
        if (frames.size < 2) return 0f

        var totalMotion = 0f
        for (i in 1 until frames.size) {
            val prev = frames[i - 1].landmarks
            val curr = frames[i].landmarks

            // Sum of all landmark movements
            for (j in prev.indices) {
                val dx = curr[j].x - prev[j].x
                val dy = curr[j].y - prev[j].y
                totalMotion += sqrt(dx * dx + dy * dy)
            }
        }

        return totalMotion / (frames.size - 1)
    }

    private fun calculateElbowAngle(frame: PoseFrame): Float {
        return calculateAngle(
            frame.landmarks[PoseFrame.RIGHT_SHOULDER],
            frame.landmarks[PoseFrame.RIGHT_ELBOW],
            frame.landmarks[PoseFrame.RIGHT_WRIST]
        )
    }

    companion object {
        private const val STABILITY_THRESHOLD = 0.02f
    }
}
```

---

## 6. Presentation Layer Design

### 6.1 Navigation

```kotlin
// ui/navigation/AppNavigation.kt
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecording = { strokeType ->
                    navController.navigate(Screen.Recording.createRoute(strokeType))
                },
                onNavigateToResults = { analysisId ->
                    navController.navigate(Screen.Results.createRoute(analysisId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(
            route = Screen.Recording.route,
            arguments = listOf(
                navArgument("strokeType") { 
                    type = NavType.StringType 
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val strokeType = backStackEntry.arguments?.getString("strokeType")
                ?.let { StrokeType.valueOf(it) }

            RecordingScreen(
                preSelectedStroke = strokeType,
                onVideoRecorded = { uri ->
                    navController.navigate(Screen.Analyzing.createRoute(uri.toString()))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Analyzing.route,
            arguments = listOf(
                navArgument("videoUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri") ?: ""

            AnalyzingScreen(
                videoUri = Uri.parse(videoUri),
                onAnalysisComplete = { resultId ->
                    navController.navigate(Screen.Results.createRoute(resultId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onError = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("analysisId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val analysisId = backStackEntry.arguments?.getString("analysisId") ?: ""

            ResultsScreen(
                analysisId = analysisId,
                onBack = { navController.popBackStack() },
                onRetry = {
                    navController.navigate(Screen.Recording.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToResults = { analysisId ->
                    navController.navigate(Screen.Results.createRoute(analysisId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Recording : Screen("recording?strokeType={strokeType}") {
        fun createRoute(strokeType: StrokeType?) = 
            "recording?strokeType=${strokeType?.name ?: ""}"
    }
    data object Analyzing : Screen("analyzing/{videoUri}") {
        fun createRoute(videoUri: String) = 
            "analyzing/${Uri.encode(videoUri)}"
    }
    data object Results : Screen("results/{analysisId}") {
        fun createRoute(analysisId: String) = "results/$analysisId"
    }
    data object History : Screen("history")
}
```

### 6.2 Home Screen

```kotlin
// ui/home/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentAnalyses()
    }

    private fun loadRecentAnalyses() {
        viewModelScope.launch {
            analysisRepository.observeRecentAnalyses(limit = 5)
                .collect { analyses ->
                    _uiState.update { it.copy(recentAnalyses = analyses) }
                }
        }
    }
}

data class HomeUiState(
    val recentAnalyses: List<AnalysisSummary> = emptyList()
)

// ui/home/HomeScreen.kt
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToRecording: (StrokeType?) -> Unit,
    onNavigateToResults: (String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomNavTab.Home,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.History -> onNavigateToHistory()
                        else -> { /* Handle other tabs */ }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            HomeHeader()

            // Action buttons
            ActionButtonsRow(
                onRecordClick = { onNavigateToRecording(null) },
                onImportClick = { /* Handle import */ }
            )

            // Recent analyses
            RecentAnalysesSection(
                analyses = uiState.recentAnalyses,
                onAnalysisClick = onNavigateToResults,
                onViewAllClick = onNavigateToHistory
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onRecordClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Videocam,
            iconBackground = Color(0xFFE53935),
            title = "录制视频",
            subtitle = "实时拍摄动作",
            onClick = onRecordClick
        )

        ActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Folder,
            iconBackground = Color(0xFF43A047),
            title = "导入视频",
            subtitle = "从相册选择",
            onClick = onImportClick
        )
    }
}
```

### 6.3 Recording Screen

```kotlin
// ui/recording/RecordingViewModel.kt
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val recordVideoUseCase: RecordVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    fun onStrokeTypeSelected(strokeType: StrokeType) {
        _uiState.update { it.copy(selectedStrokeType = strokeType) }
    }

    fun onRecordingStarted() {
        _uiState.update { it.copy(isRecording = true, recordingDurationMs = 0L) }
    }

    fun onRecordingProgress(durationMs: Long) {
        _uiState.update { it.copy(recordingDurationMs = durationMs) }
    }

    fun onRecordingStopped(tempUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecording = false, isSaving = true) }
            try {
                val savedPath = recordVideoUseCase.saveRecording(tempUri)
                _uiState.update { 
                    it.copy(isSaving = false, savedVideoUri = Uri.parse(savedPath)) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = e.message) 
                }
            }
        }
    }
}

data class RecordingUiState(
    val selectedStrokeType: StrokeType = StrokeType.FOREHAND_CLEAR,
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val isSaving: Boolean = false,
    val savedVideoUri: Uri? = null,
    val error: String? = null
)

// ui/recording/RecordingScreen.kt
@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = hiltViewModel(),
    preSelectedStroke: StrokeType?,
    onVideoRecorded: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Camera permission
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        preSelectedStroke?.let { viewModel.onStrokeTypeSelected(it) }
    }

    // Handle saved video
    LaunchedEffect(uiState.savedVideoUri) {
        uiState.savedVideoUri?.let { uri ->
            onVideoRecorded(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(
                isRecording = uiState.isRecording,
                onRecordingStarted = viewModel::onRecordingStarted,
                onRecordingProgress = viewModel::onRecordingProgress,
                onRecordingStopped = viewModel::onRecordingStopped
            )
        } else {
            PermissionDeniedContent()
        }

        // Top bar
        RecordingTopBar(
            isRecording = uiState.isRecording,
            durationMs = uiState.recordingDurationMs,
            onBack = onBack
        )

        // Guide overlay
        if (!uiState.isRecording) {
            PoseGuideOverlay()
        }

        // Tips
        RecordingTips(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp)
        )

        // Stroke type selector
        StrokeTypeSelector(
            selectedType = uiState.selectedStrokeType,
            onTypeSelected = viewModel::onStrokeTypeSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
        )

        // Recording controls
        RecordingControls(
            isRecording = uiState.isRecording,
            onRecordClick = { /* Toggle recording */ },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

### 6.4 Results Screen

```kotlin
// ui/analysis/ResultsViewModel.kt
@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val analysisId: String = savedStateHandle["analysisId"] ?: ""

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    init {
        loadAnalysis()
    }

    private fun loadAnalysis() {
        viewModelScope.launch {
            try {
                val result = analysisRepository.getAnalysis(analysisId)
                if (result != null) {
                    _uiState.value = ResultsUiState.Success(result)
                } else {
                    _uiState.value = ResultsUiState.Error("Analysis not found")
                }
            } catch (e: Exception) {
                _uiState.value = ResultsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data class Success(val result: AnalysisResult) : ResultsUiState
    data class Error(val message: String) : ResultsUiState
}

// ui/analysis/ResultsScreen.kt
@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel = hiltViewModel(),
    analysisId: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ResultsUiState.Loading -> LoadingContent()
        is ResultsUiState.Error -> ErrorContent(state.message, onBack)
        is ResultsUiState.Success -> {
            ResultsContent(
                result = state.result,
                onBack = onBack,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun ResultsContent(
    result: AnalysisResult,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        ResultsHeader(
            strokeType = result.strokeType,
            analyzedAt = result.analyzedAt,
            onBack = onBack
        )

        // Video with skeleton overlay
        VideoWithSkeletonOverlay(
            videoUri = result.videoUri,
            poseFrames = result.poseFrames,
            keyFrames = result.keyFrameIndices
        )

        // Score card
        ScoreCard(
            overallScore = result.overallScore,
            dimensionScores = result.dimensionScores
        )

        // Detailed feedback
        FeedbackSection(dimensionScores = result.dimensionScores)

        // Recommended drill
        DrillSuggestionCard(strokeType = result.strokeType)

        // Action buttons
        ActionButtons(
            onSave = { /* Save */ },
            onRetry = onRetry
        )
    }
}

@Composable
private fun ScoreCard(
    overallScore: Int,
    dimensionScores: List<DimensionScore>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-20).dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Overall score with grade
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ScoreCircle(score = overallScore)
                ScoreGradeText(score = overallScore)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dimension scores grid
            DimensionScoresGrid(dimensionScores = dimensionScores)
        }
    }
}

@Composable
private fun ScoreCircle(score: Int) {
    val color = when {
        score >= 90 -> Color(0xFF43A047)
        score >= 70 -> Color(0xFF1A73E8)
        score >= 50 -> Color(0xFFFB8C00)
        else -> Color(0xFFE53935)
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(color, color.copy(alpha = 0.7f))
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "综合评分",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DimensionScoresGrid(dimensionScores: List<DimensionScore>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(dimensionScores) { dimensionScore ->
            DimensionScoreItem(dimensionScore = dimensionScore)
        }
    }
}

@Composable
private fun DimensionScoreItem(dimensionScore: DimensionScore) {
    val color = when {
        dimensionScore.score >= 85 -> Color(0xFF43A047)
        dimensionScore.score >= 70 -> Color(0xFF1A73E8)
        else -> Color(0xFFFB8C00)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = { dimensionScore.score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFFE0E0E0)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = dimensionScore.score.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = dimensionScore.dimension.displayNameCn,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
```

---

## 7. Dependency Injection

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "badminton_ai_db"
        ).build()
    }

    @Provides
    fun provideAnalysisDao(database: AppDatabase): AnalysisDao {
        return database.analysisDao()
    }
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        impl: AnalysisRepositoryImpl
    ): AnalysisRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        impl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}

// di/MLModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class MLModule {

    @Binds
    @Singleton
    abstract fun bindPoseDetector(
        impl: MediaPipePoseDetector
    ): PoseDetector

    @Binds
    @Singleton
    abstract fun bindStrokeClassifier(
        impl: TFLiteStrokeClassifier
    ): StrokeClassifier
}
```

---

## 8. Testing Strategy

### 8.1 Unit Tests

```kotlin
// Test scoring engine
class ScoringEngineTest {
    private lateinit var scoringEngine: ScoringEngine

    @Before
    fun setup() {
        scoringEngine = ScoringEngine()
    }

    @Test
    fun `scoreContactPoint returns high score for ideal contact position`() {
        val frames = createTestFramesWithIdealContact()
        val keyFrames = KeyFrameIndices(0, 5, 10, 15)
        
        val result = scoringEngine.score(
            frames, 
            StrokeType.FOREHAND_CLEAR, 
            keyFrames
        )
        
        val contactScore = result.find { 
            it.dimension == ScoreDimension.CONTACT_POINT 
        }
        
        assertThat(contactScore?.score).isGreaterThan(80)
    }

    @Test
    fun `scoreContactPoint detects low contact point issue`() {
        val frames = createTestFramesWithLowContact()
        val keyFrames = KeyFrameIndices(0, 5, 10, 15)
        
        val result = scoringEngine.score(
            frames, 
            StrokeType.FOREHAND_CLEAR, 
            keyFrames
        )
        
        val contactScore = result.find { 
            it.dimension == ScoreDimension.CONTACT_POINT 
        }
        
        assertThat(contactScore?.issues).isNotEmpty()
        assertThat(contactScore?.issues?.first()?.descriptionCn)
            .contains("击球点过低")
    }
}

// Test use case
class AnalyzeVideoUseCaseTest {
    @Mock lateinit var poseDetector: PoseDetector
    @Mock lateinit var strokeClassifier: StrokeClassifier
    @Mock lateinit var scoringEngine: ScoringEngine
    @Mock lateinit var feedbackGenerator: FeedbackGenerator
    @Mock lateinit var analysisRepository: AnalysisRepository

    private lateinit var useCase: AnalyzeVideoUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = AnalyzeVideoUseCase(
            poseDetector, strokeClassifier, scoringEngine, 
            feedbackGenerator, analysisRepository
        )
    }

    @Test
    fun `invoke emits correct progress sequence`() = runTest {
        // Setup mocks
        whenever(poseDetector.detectFromVideo(any()))
            .thenReturn(flowOf(0 to testPoseFrame()))
        whenever(strokeClassifier.classify(any()))
            .thenReturn(StrokeType.FOREHAND_CLEAR)
        whenever(scoringEngine.score(any(), any(), any()))
            .thenReturn(testDimensionScores())
        whenever(feedbackGenerator.generateFeedback(any(), any()))
            .thenReturn(testDimensionScores())

        val progressList = mutableListOf<AnalyzeVideoUseCase.Progress>()
        
        useCase(Uri.EMPTY).collect { progressList.add(it) }

        assertThat(progressList.map { it::class })
            .containsExactly(
                AnalyzeVideoUseCase.Progress.Decoding::class,
                AnalyzeVideoUseCase.Progress.PoseDetection::class,
                AnalyzeVideoUseCase.Progress.StrokeClassification::class,
                AnalyzeVideoUseCase.Progress.Scoring::class,
                AnalyzeVideoUseCase.Progress.GeneratingFeedback::class,
                AnalyzeVideoUseCase.Progress.Completed::class
            )
    }
}
```

### 8.2 Integration Tests

```kotlin
// Test Room database
@RunWith(AndroidJUnit4::class)
class AnalysisDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var analysisDao: AnalysisDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        analysisDao = database.analysisDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveAnalysis() = runTest {
        val entity = createTestAnalysisEntity()
        val scores = createTestScoreEntities(entity.id)

        analysisDao.insertAnalysisWithScores(entity, scores)

        val retrieved = analysisDao.getAnalysis(entity.id)
        val retrievedScores = analysisDao.getDimensionScores(entity.id)

        assertThat(retrieved).isEqualTo(entity)
        assertThat(retrievedScores).hasSize(scores.size)
    }

    @Test
    fun getScoreTrendReturnsCorrectData() = runTest {
        // Insert analyses over multiple days
        val analyses = (0..6).map { dayOffset ->
            createTestAnalysisEntity(
                analyzedAt = System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000L),
                score = 70 + dayOffset * 3
            )
        }
        analyses.forEach { analysisDao.insertAnalysis(it) }

        val trend = analysisDao.getScoreTrend(
            strokeType = null,
            sinceTimestamp = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        )

        assertThat(trend).hasSize(7)
    }
}
```

### 8.3 UI Tests

```kotlin
// Test Compose UI
class ResultsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scoreCardDisplaysCorrectScore() {
        val testResult = createTestAnalysisResult(overallScore = 85)

        composeTestRule.setContent {
            ResultsContent(
                result = testResult,
                onBack = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("85").assertIsDisplayed()
        composeTestRule.onNodeWithText("综合评分").assertIsDisplayed()
    }

    @Test
    fun feedbackSectionShowsIssues() {
        val testResult = createTestAnalysisResultWithIssues()

        composeTestRule.setContent {
            ResultsContent(
                result = testResult,
                onBack = {},
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("击球点过低").assertIsDisplayed()
    }
}
```

---

## 9. Performance Optimization

### 9.1 Video Processing

```kotlin
// Optimize frame extraction with hardware decoding
class OptimizedFrameExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

    suspend fun extractFrames(
        videoUri: Uri,
        targetFps: Int = 30,
        onFrame: suspend (Int, Bitmap) -> Unit
    ) = withContext(Dispatchers.Default) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)

        // Find video track
        val trackIndex = (0 until extractor.trackCount).find { i ->
            extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)
                ?.startsWith("video/") == true
        } ?: throw IllegalArgumentException("No video track found")

        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)

        // Configure decoder with Surface for GPU rendering
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)

        // Process frames...
    }
}
```

### 9.2 Memory Management

```kotlin
// Efficient pose frame storage
class CompressedPoseStorage {
    // Store only key frames + interpolation data
    fun compress(frames: List<PoseFrame>): CompressedPoseData {
        val keyFrameIndices = selectKeyFrames(frames)
        val keyFrames = keyFrameIndices.map { frames[it] }
        val interpolationData = calculateInterpolationData(frames, keyFrameIndices)
        
        return CompressedPoseData(keyFrames, interpolationData)
    }

    fun decompress(data: CompressedPoseData): List<PoseFrame> {
        // Reconstruct full sequence using interpolation
    }

    private fun selectKeyFrames(frames: List<PoseFrame>): List<Int> {
        // Select frames with significant motion changes
        val indices = mutableListOf(0) // Always include first frame
        
        for (i in 1 until frames.size) {
            val motionDelta = calculateMotionDelta(frames[i - 1], frames[i])
            if (motionDelta > MOTION_THRESHOLD) {
                indices.add(i)
            }
        }
        
        indices.add(frames.lastIndex) // Always include last frame
        return indices
    }
}
```

### 9.3 Analysis Quality Modes

```kotlin
// Quality/performance tradeoff configuration
enum class AnalysisQuality(
    val targetFps: Int,
    val poseModelComplexity: Int,
    val smoothingWindowSize: Int
) {
    FAST(
        targetFps = 15,
        poseModelComplexity = 0,  // Lite model
        smoothingWindowSize = 3
    ),
    BALANCED(
        targetFps = 24,
        poseModelComplexity = 1,  // Full model
        smoothingWindowSize = 5
    ),
    ACCURATE(
        targetFps = 30,
        poseModelComplexity = 2,  // Heavy model
        smoothingWindowSize = 7
    )
}
```

---

## 10. Error Handling

```kotlin
// Centralized error handling
sealed class AppError : Exception() {
    data class VideoError(override val message: String) : AppError()
    data class PoseDetectionError(override val message: String) : AppError()
    data class AnalysisError(override val message: String) : AppError()
    data class StorageError(override val message: String) : AppError()
}

// Error handler in ViewModel base
abstract class BaseViewModel : ViewModel() {
    protected val _error = MutableSharedFlow<AppError>()
    val error: SharedFlow<AppError> = _error.asSharedFlow()

    protected fun handleError(throwable: Throwable) {
        viewModelScope.launch {
            val appError = when (throwable) {
                is AppError -> throwable
                is IOException -> AppError.StorageError(throwable.message ?: "Storage error")
                else -> AppError.AnalysisError(throwable.message ?: "Unknown error")
            }
            _error.emit(appError)
        }
    }
}

// Error display composable
@Composable
fun ErrorHandler(
    viewModel: BaseViewModel,
    onRetry: () -> Unit
) {
    val error by viewModel.error.collectAsStateWithLifecycle(initialValue = null)

    error?.let { appError ->
        Snackbar(
            action = {
                TextButton(onClick = onRetry) {
                    Text("重试")
                }
            }
        ) {
            Text(
                text = when (appError) {
                    is AppError.VideoError -> "视频处理失败: ${appError.message}"
                    is AppError.PoseDetectionError -> "姿态检测失败: ${appError.message}"
                    is AppError.AnalysisError -> "分析失败: ${appError.message}"
                    is AppError.StorageError -> "存储错误: ${appError.message}"
                }
            )
        }
    }
}
```

---

## 11. Security Considerations

### 11.1 Data Privacy

- All ML processing runs on-device
- Videos stored in app-private directory
- No network calls for analysis features
- User can delete all data from settings

### 11.2 Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

<!-- For Android 12 and below -->
<uses-permission 
    android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
```

---

## 12. Testing Strategy

### 12.1 Test Architecture
采用分层测试策略，覆盖从单元测试到端到端测试的全流程：

```
┌─────────────────────────────────────────────────────┐
│                  End-to-End Tests                    │
│  (UI flow testing on real devices/emulators)        │
├─────────────────────────────────────────────────────┤
│              Integration Tests                       │
│  (Component integration, database, API tests)       │
├─────────────────────────────────────────────────────┤
│                Unit Tests                           │
│  (Business logic, models, use cases, utilities)     │
└─────────────────────────────────────────────────────┘
```

### 12.2 Test Coverage Goals
| Test Type | Coverage Target | Priority |
|-----------|-----------------|----------|
| Unit Tests | ≥90% | P0 |
| Integration Tests | ≥70% | P1 |
| UI Tests | ≥50% | P1 |
| E2E Tests | ≥30% | P2 |

### 12.3 Unit Test Structure
#### 12.3.1 Domain Layer Tests
```kotlin
// Test location: app/src/test/java/com/badmintonai/domain/
├── model/
│   ├── ModelsTest.kt          // Data model validation
│   └── ScoreCalculationTest.kt // Scoring logic tests
└── usecase/
    ├── AnalyzeVideoUseCaseTest.kt
    ├── GetHistoryUseCaseTest.kt
    └── DeleteResultUseCaseTest.kt
```

**Test Framework**:
- JUnit 4 for test runner
- MockK for dependency mocking
- kotlinx-coroutines-test for coroutine testing

#### 12.3.2 Data Layer Tests
```kotlin
// Test location: app/src/test/java/com/badmintonai/data/
├── local/
│   ├── EntitiesTest.kt       // Entity mapping tests
│   ├── ConvertersTest.kt     // Room type converter tests
│   └── DaoTest.kt            // Database operation tests
├── repository/
│   ├── AnalysisRepositoryTest.kt
│   └── PoseEstimationRepositoryTest.kt
└── ml/
    ├── ScoringEngineTest.kt
    └── StrokeClassifierTest.kt
```

### 12.4 Instrumented Tests
#### 12.4.1 UI Tests
```kotlin
// Test location: app/src/androidTest/java/com/badmintonai/presentation/ui/
├── home/
│   └── HomeScreenTest.kt
├── recording/
│   └── RecordingScreenTest.kt
├── analysis/
│   └── AnalysisScreenTest.kt
├── results/
│   └── ResultsScreenTest.kt
└── history/
    └── HistoryScreenTest.kt
```

**UI Test Framework**:
- Jetpack Compose Testing
- Espresso for view interactions
- Mockito for navigation mocking

#### 12.4.2 ML Component Tests
```kotlin
// Test location: app/src/androidTest/java/com/badmintonai/ml/
├── MediaPipePoseEstimatorTest.kt
├── TFLiteStrokeClassifierTest.kt
└── ScoringEngineIntegrationTest.kt
```

### 12.5 Test Dependencies
```kotlin
// build.gradle.kts (app module)
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Instrumented testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
}
```

### 12.6 Test Execution
#### 12.6.1 Local Execution
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run unit tests for specific module
./gradlew domain:testDebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest

# Run UI tests for specific screen
./gradlew connectedDebugAndroidTest -P android.testInstrumentationRunnerArguments.class=com.badmintonai.presentation.ui.home.HomeScreenTest
```

#### 12.6.2 CI Pipeline Integration
```yaml
# .github/workflows/android.yml (partial)
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest
      - name: Generate test report
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: app/build/reports/tests/testDebugUnitTest/
```

### 12.7 Test Case Examples
#### 12.7.1 Unit Test Example
```kotlin
@Test
fun `AnalyzeVideoUseCase correctly calculates overall score`() = runTest {
    // Given
    val testFrames = generateTestPoseFrames()
    val dimensionScores = listOf(
        DimensionScore(PREPARATION, 90, 0.2f, ""),
        DimensionScore(BACKSWING, 80, 0.15f, ""),
        DimensionScore(CONTACT_POINT, 85, 0.25f, ""),
        DimensionScore(FOLLOW_THROUGH, 75, 0.15f, ""),
        DimensionScore(TIMING, 95, 0.15f, ""),
        DimensionScore(FOOTWORK, 70, 0.1f, "")
    )
    
    coEvery { poseRepo.processVideo(any()) } returns testFrames
    coEvery { classificationRepo.classifyStroke(any()) } returns FOREHAND_CLEAR
    coEvery { scoringRepo.calculateScore(any(), any()) } returns dimensionScores

    // When
    val result = useCase("/test/video.mp4")

    // Then
    assertEquals(83, result.overallScore) // Weighted sum calculation
    assertEquals(FOREHAND_CLEAR, result.strokeType)
    coVerify { analysisRepo.saveResult(result) }
}
```

#### 12.7.2 UI Test Example
```kotlin
@Test
fun homeScreen_startAnalysisButton_click_navigatesToRecording() {
    val navController = mock<NavController>()
    
    composeTestRule.setContent {
        BadmintonAITheme {
            HomeScreen(navController = navController)
        }
    }

    composeTestRule.onNodeWithText("Start Analysis").performClick()
    verify(navController).navigate("recording")
}
```

### 12.8 Test Best Practices
1. **Isolation**: Each test should be independent, no shared state between tests
2. **Readability**: Use descriptive test names following `should_xxx_when_xxx` pattern
3. **Speed**: Unit tests should run in <10ms each, avoid heavy operations in unit tests
4. **Flakiness**: Avoid time-dependent tests, use TestDispatcher for coroutines
5. **Maintainability**: Keep tests simple, avoid complex logic in test code
6. **Coverage**: Focus on critical paths first, then expand to edge cases

---

## 13. Build & Deployment

### 12.1 Build Variants

```kotlin
// build.gradle.kts
android {
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("free") {
            dimension = "version"
            applicationIdSuffix = ".free"
            buildConfigField("Boolean", "IS_PREMIUM", "false")
        }
        create("premium") {
            dimension = "version"
            buildConfigField("Boolean", "IS_PREMIUM", "true")
        }
    }
}
```

### 12.2 ProGuard Rules

```proguard
# proguard-rules.pro

# MediaPipe
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
```

### 12.3 CI/CD Pipeline

```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3

    - name: Run unit tests
      run: ./gradlew testDebugUnitTest

    - name: Run lint
      run: ./gradlew lintDebug

    - name: Build debug APK
      run: ./gradlew assembleDebug

    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

---

## Appendix A: MediaPipe Landmark Indices

| Index | Landmark | Index | Landmark |
|-------|----------|-------|----------|
| 0 | NOSE | 17 | LEFT_PINKY |
| 1 | LEFT_EYE_INNER | 18 | RIGHT_PINKY |
| 2 | LEFT_EYE | 19 | LEFT_INDEX |
| 3 | LEFT_EYE_OUTER | 20 | RIGHT_INDEX |
| 4 | RIGHT_EYE_INNER | 21 | LEFT_THUMB |
| 5 | RIGHT_EYE | 22 | RIGHT_THUMB |
| 6 | RIGHT_EYE_OUTER | 23 | LEFT_HIP |
| 7 | LEFT_EAR | 24 | RIGHT_HIP |
| 8 | RIGHT_EAR | 25 | LEFT_KNEE |
| 9 | MOUTH_LEFT | 26 | RIGHT_KNEE |
| 10 | MOUTH_RIGHT | 27 | LEFT_ANKLE |
| 11 | LEFT_SHOULDER | 28 | RIGHT_ANKLE |
| 12 | RIGHT_SHOULDER | 29 | LEFT_HEEL |
| 13 | LEFT_ELBOW | 30 | RIGHT_HEEL |
| 14 | RIGHT_ELBOW | 31 | LEFT_FOOT_INDEX |
| 15 | LEFT_WRIST | 32 | RIGHT_FOOT_INDEX |
| 16 | RIGHT_WRIST | | |

---

## Appendix B: Stroke Reference Angle Ranges

| Stroke Type | Phase | Metric | Ideal Range |
|-------------|-------|--------|-------------|
| Forehand Clear | Backswing | Elbow Angle | 90° - 120° |
| Forehand Clear | Contact | Elbow Angle | 150° - 180° |
| Forehand Clear | Contact | Wrist Above Head | > 15% |
| Smash | Contact | Elbow Angle | 160° - 180° |
| Smash | Contact | Wrist Above Head | > 20% |
| Drop Shot | Contact | Elbow Angle | 120° - 150° |
| Serve | Contact | Elbow Angle | 130° - 160° |

---

*Document Version: 1.0*
*Created: March 2026*
