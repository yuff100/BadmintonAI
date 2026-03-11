package com.badmintonai.di

import android.content.Context
import androidx.room.Room
import com.badmintonai.data.local.AppDatabase
import com.badmintonai.data.local.AnalysisDao
import com.badmintonai.data.repository.AnalysisRepositoryImpl
import com.badmintonai.data.repository.PoseEstimationRepositoryImpl
import com.badmintonai.data.repository.ScoringRepositoryImpl
import com.badmintonai.data.repository.StrokeClassificationRepositoryImpl
import com.badmintonai.domain.repository.AnalysisRepository
import com.badmintonai.domain.repository.PoseEstimationRepository
import com.badmintonai.domain.repository.ScoringRepository
import com.badmintonai.domain.repository.StrokeClassificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideAnalysisDao(database: AppDatabase): AnalysisDao {
        return database.analysisDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        repository: AnalysisRepositoryImpl
    ): AnalysisRepository

    @Binds
    @Singleton
    abstract fun bindPoseEstimationRepository(
        repository: PoseEstimationRepositoryImpl
    ): PoseEstimationRepository

    @Binds
    @Singleton
    abstract fun bindStrokeClassificationRepository(
        repository: StrokeClassificationRepositoryImpl
    ): StrokeClassificationRepository

    @Binds
    @Singleton
    abstract fun bindScoringRepository(
        repository: ScoringRepositoryImpl
    ): ScoringRepository
}
