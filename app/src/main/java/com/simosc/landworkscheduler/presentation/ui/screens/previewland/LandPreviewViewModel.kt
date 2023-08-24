package com.simosc.landworkscheduler.presentation.ui.screens.previewland

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.usecase.land.DeleteLand
import com.simosc.landworkscheduler.domain.usecase.land.GetLand
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandPreviewViewModel @Inject constructor(
    private val getLandUseCase: GetLand,
    private val deleteLandUseCase: DeleteLand
): ViewModel() {

    private var mainJob: Job? = null
    override fun onCleared() {
        super.onCleared()
        stopSync()
    }

    private val _landId: MutableStateFlow<Long> = MutableStateFlow(-1L)

    private val _uiState: MutableStateFlow<LandPreviewStates> =
        MutableStateFlow(LandPreviewStates.LoadingState)
    val uiState: StateFlow<LandPreviewStates> =
        _uiState.asStateFlow()

    init {
        _landId.onEach {  landId ->
            stopSync()
            if(landId > -1L)
                startSync(landId)
            else
                _uiState.update {
                    LandPreviewStates.LoadingState
                }
        }.launchIn(
            viewModelScope,
        )
    }

    private fun startSync(landId: Long){
        mainJob = getLandUseCase(landId)
            .onEach {  land ->
                _uiState.update {
                    land?.let{
                        LandPreviewStates.Loaded(land = land)
                    }?:run{
                        LandPreviewStates.LandNotFoundState
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    private fun stopSync(){
        mainJob?.let{
            it.cancel()
            mainJob = null
        }
    }

    fun setLandId(landId: Long){
        _landId.update { if(landId < 0L) 0L else landId }
    }

    fun deleteLand() {
        uiState.value.let {
            viewModelScope.launch(Dispatchers.IO){
                if( it is LandPreviewStates.Loaded)
                    deleteLandUseCase(it.land)
            }
        }
    }
}