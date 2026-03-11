package com.badmintonai.presentation.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badmintonai.domain.usecase.AnalyzeVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analyzeVideoUseCase: AnalyzeVideoUseCase
) : ViewModel() {
    
    fun analyzeVideo(videoPath: String): Flow<Result<Long>> = flow {
        try {
            val result = analyzeVideoUseCase(videoPath)
            emit(Result.success(result.id))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
