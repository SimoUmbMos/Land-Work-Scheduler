package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.GetKmlText
import com.simosc.landworkscheduler.domain.usecase.land.DeleteLand
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LandsMenuViewModel @Inject constructor(
    private val getLandsUseCase: GetLands,
    private val getDeleteLandUseCase: DeleteLand,
    private val getKmlTextUseCase: GetKmlText
): ViewModel() {
    private var mainJob: Job? = null

    private val _uiState: MutableStateFlow<LandsMenuStates> =
        MutableStateFlow(LandsMenuStates.Loading)
    val uiState: StateFlow<LandsMenuStates> =
        _uiState.asStateFlow()

    private val _isLoadingAction:MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val isLoadingAction:StateFlow<Boolean> =
        _isLoadingAction.asStateFlow()

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

    fun toggleLand(land: Land) {
        _uiState.value.let{ state ->
            when(state){
                is LandsMenuStates.ExportLands ->
                    _uiState.update { state.onToggleLand(land = land) }
                is LandsMenuStates.DeleteLands ->
                    _uiState.update { state.onToggleLand(land = land) }
                else -> {}
            }
        }
    }

    fun changeToNormalState() {
        _uiState.value.let { state ->
            when(state){
                is LandsMenuStates.MultiSelectLands ->
                    _uiState.update {LandsMenuStates.Loaded(state.lands)}
                else -> {}
            }
        }
    }

    fun changeToExportLandsState(){
        _uiState.value.let { state ->
            when(state){
                is LandsMenuStates.MultiSelectLands ->
                    _uiState.update {
                        LandsMenuStates.ExportLands(
                            lands = state.lands,
                            selectedLands = state.selectedLands
                        )
                    }
                is LandsMenuStates.Loaded -> {
                    _uiState.update {
                        LandsMenuStates.ExportLands(
                            lands = state.lands
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun changeToDeleteLandsState(){
        _uiState.value.let { state ->
            when(state){
                is LandsMenuStates.MultiSelectLands ->
                    _uiState.update {
                        LandsMenuStates.DeleteLands(
                            lands = state.lands,
                            selectedLands = state.selectedLands
                        )
                    }
                is LandsMenuStates.Loaded -> {
                    _uiState.update {
                        LandsMenuStates.DeleteLands(
                            lands = state.lands
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun createFileToExport(createFileLauncher: ManagedActivityResultLauncher<String, Uri?>) {
        _uiState.value.let { state ->
            if(state is LandsMenuStates.ExportLands) {
                if(state.selectedLands.isNotEmpty()) {
                    LocalDateTime.now().run {
                        createFileLauncher.launch(
                            "exported_lands_" +
                                    "${year}${monthValue}${dayOfMonth}_" +
                                    "${hour}${minute}${second}_${nano}"
                        )
                    }
                }else{
                    _uiState.update { LandsMenuStates.Loaded(lands = state.lands) }
                }
            }
        }
    }

    fun getLandsKmlString(): String {
        var kmlText = ""
        _uiState.value.let{ state ->
            if(state is LandsMenuStates.ExportLands){
                state.selectedLands.let { selectedLands ->
                    _isLoadingAction.update { true }
                    kmlText = getKmlTextUseCase(selectedLands)
                    _isLoadingAction.update { false }
                }
            }
        }
        return kmlText
    }

    fun executeLandsDelete() {
        _uiState.value.let { state ->
            if(state is LandsMenuStates.DeleteLands) {
                state.selectedLands.let{ selectedLands ->
                    _isLoadingAction.update { true }
                    viewModelScope.launch(Dispatchers.IO){
                        selectedLands.forEach {
                            getDeleteLandUseCase(it)
                        }
                        _uiState.update {
                            LandsMenuStates.Loaded(
                                lands = state.lands.toMutableList().apply {
                                    removeAll(selectedLands)
                                }
                            )
                        }
                        _isLoadingAction.update { false }
                    }
                }
            }
        }
    }

}