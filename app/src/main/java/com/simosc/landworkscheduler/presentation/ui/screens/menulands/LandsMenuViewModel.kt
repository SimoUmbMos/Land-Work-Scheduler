package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import androidx.lifecycle.ViewModel
import com.simosc.landworkscheduler.domain.usecase.land.GetLands
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
import javax.inject.Inject

@HiltViewModel
class LandsMenuViewModel @Inject constructor(
    private val getLandsUseCase: GetLands
): ViewModel() {
    private var mainJob: Job? = null

    private val _uiState: MutableStateFlow<LandsMenuStates> =
        MutableStateFlow(LandsMenuStates.Loading)
    val uiState: StateFlow<LandsMenuStates> =
        _uiState.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        stopSync()
    }

    private fun stopSync(){
        mainJob?.let{
            it.cancel()
            mainJob = null
        }
    }

    fun loadLands() {
        stopSync()
        mainJob = getLandsUseCase().onEach { lands ->
            _uiState.update{
                LandsMenuStates.Loaded(lands)
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

}