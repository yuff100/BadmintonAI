package com.badmintonai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.model.DimensionScore
import com.badmintonai.domain.model.ScoringDimension
import com.badmintonai.domain.model.StrokeType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Dao
interface AnalysisDao {
    @Insert
    suspend fun insert(result: AnalysisResultEntity)
    
    @Query("SELECT * FROM analysis_results ORDER BY timestamp DESC")
    suspend fun getAll(): List<AnalysisResultEntity>
    
    @Query("SELECT * FROM analysis_results WHERE id = :id")
    suspend fun getById(id: Long): AnalysisResultEntity?
    
    @Query("DELETE FROM analysis_results WHERE id = :id")
    suspend fun deleteById(id: Long)
}

class Converters {
    @TypeConverter
    fun fromStrokeType(strokeType: StrokeType): String = strokeType.name
    
    @TypeConverter
    fun toStrokeType(name: String): StrokeType = StrokeType.valueOf(name)
    
    @TypeConverter
    fun fromDimensionScores(scores: List<DimensionScore>): String {
        return Gson().toJson(scores)
    }
    
    @TypeConverter
    fun toDimensionScores(json: String): List<DimensionScore> {
        val type = object : TypeToken<List<DimensionScore>>() {}.type
        return Gson().fromJson(json, type)
    }
}
