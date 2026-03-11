package com.badmintonai.presentation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.badmintonai.domain.model.AnalysisResult
import com.badmintonai.domain.usecase.GetAnalysisHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAnalysisHistoryUseCase: GetAnalysisHistoryUseCase
) : ViewModel() {
    
    fun getHistory(): List<AnalysisResult> = runBlocking {
        getAnalysisHistoryUseCase()
    }
}
