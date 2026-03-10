package com.badmintonai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [AnalysisResultEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
    
    companion object {
        const val DATABASE_NAME = "badmintonai_db"
    }
}
