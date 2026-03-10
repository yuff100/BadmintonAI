package com.badmintonai.di

import android.content.Context
import androidx.room.Room
import com.badmintonai.data.local.AppDatabase
import com.badmintonai.data.local.AppDatabase.Companion.DATABASE_NAME
import com.badmintonai.data.repository.AnalysisRepositoryImpl
import com.badmintonai.domain.repository.AnalysisRepository
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
            DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideAnalysisDao(database: AppDatabase) = database.analysisDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        repository: AnalysisRepositoryImpl
    ): AnalysisRepository
}
