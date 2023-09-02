package com.simosc.landworkscheduler.presentation.ui.screens.previewland

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.usecase.land.DeleteLand
import com.simosc.landworkscheduler.domain.usecase.land.GetLand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class LandPreviewViewModel @Inject constructor(
    private val getLandUseCase: GetLand,
    private val deleteLandUseCase: DeleteLand,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private var mainJob: Job? = null

    val uiState = savedStateHandle.getStateFlow<LandPreviewStates>(
        "LandPreviewStates",
        LandPreviewStates.LoadingState
    )

    private fun startSync(landId: Long){
        mainJob?.cancel()
        mainJob = getLandUseCase(landId)
            .onEach {  land ->
                land?.let{
                    savedStateHandle["LandPreviewStates"] = LandPreviewStates.Loaded(land = land)
                }?:run{
                    savedStateHandle["LandPreviewStates"] = LandPreviewStates.LandNotFoundState
                }
            }.launchIn(
                scope = viewModelScope + Dispatchers.IO
            )
    }

    fun setLandId(landId: Long){
        if(landId > 0L){
            startSync(landId)
        }else{
            savedStateHandle["LandPreviewStates"] = LandPreviewStates.LandNotFoundState
        }
    }

    fun deleteLand() = viewModelScope.launch(Dispatchers.IO){
        uiState.value.let { state ->
            if( state is LandPreviewStates.Loaded) {
                deleteLandUseCase(state.land)
            }
        }
    }
}