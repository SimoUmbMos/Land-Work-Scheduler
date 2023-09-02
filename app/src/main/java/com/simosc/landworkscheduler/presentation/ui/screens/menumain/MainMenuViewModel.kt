package com.simosc.landworkscheduler.presentation.ui.screens.menumain

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.usecase.work.GetDateWorksCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val getDateWorksCountUseCase: GetDateWorksCount,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private var mainJob: Job? = null

    val uiState = savedStateHandle.getStateFlow<MainMenuStates>(
        key = "MainMenuStates",
        initialValue = MainMenuStates.Loading
    )

    fun loadData(){
        mainJob?.cancel()
        mainJob = getDateWorksCountUseCase(
            LocalDate.now()
        ).onEach { todayWorksCount ->
            savedStateHandle["MainMenuStates"] = MainMenuStates.Loaded(
                todayWorksCount = todayWorksCount
            )
        }.launchIn(
            scope = viewModelScope + Dispatchers.IO
        )
    }
}