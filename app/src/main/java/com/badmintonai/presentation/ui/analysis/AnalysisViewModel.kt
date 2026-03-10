package com.badmintonai.presentation.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badmintonai.domain.usecase.AnalyzeVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analyzeVideoUseCase: AnalyzeVideoUseCase
) : ViewModel() {
    
    private val _analysisState = MutableStateFlow<Result<Long>?>(null)
    val analysisState: StateFlow<Result<Long>?> = _analysisState
    
    fun analyzeVideo(videoPath: String) = flow<Result<Long>> {
        try {
            val result = analyzeVideoUseCase(videoPath)
            emit(Result.success(result.id))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
