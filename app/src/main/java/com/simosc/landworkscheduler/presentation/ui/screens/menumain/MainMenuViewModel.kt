package com.simosc.landworkscheduler.presentation.ui.screens.menumain

import androidx.lifecycle.ViewModel
import com.simosc.landworkscheduler.domain.usecase.work.GetDateWorksCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val getDateWorksCountUseCase: GetDateWorksCount
): ViewModel() {
    private var mainJob: Job? = null

    private val _uiState: MutableStateFlow<MainMenuStates> =
        MutableStateFlow(MainMenuStates.Loading)
    val uiState: StateFlow<MainMenuStates> =
        _uiState.asStateFlow()

    private fun stopSync(){
        mainJob?.let{
            it.cancel()
            mainJob = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSync()
    }

    fun loadData(){
        stopSync()
        mainJob = getDateWorksCountUseCase(
            LocalDate.now()
        ).onEach { count ->
            _uiState.update {
                MainMenuStates.Loaded(todayWorksCount = count)
            }
        }.launchIn(
            CoroutineScope(Dispatchers.IO)
        )
    }
}