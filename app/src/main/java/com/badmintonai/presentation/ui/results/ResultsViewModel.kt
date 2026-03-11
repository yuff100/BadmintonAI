package com.badmintonai.presentation.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.usecase.GetAnalysisResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val getAnalysisResultUseCase: GetAnalysisResultUseCase
) : ViewModel() {
    
    fun getResult(resultId: Long): AnalysisResult? = runBlocking {
        getAnalysisResultUseCase(resultId)
    }
}
