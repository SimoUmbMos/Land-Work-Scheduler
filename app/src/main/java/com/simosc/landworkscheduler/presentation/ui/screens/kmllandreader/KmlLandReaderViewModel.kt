package com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.core.config.DefaultMapZoom
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.ReadKml
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class KmlLandReaderViewModel @Inject constructor(
    private val readKml: ReadKml
):ViewModel() {
    private var cameraJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        cameraJob?.cancel()
    }

    private val _landsBounds: MutableStateFlow<LatLngBounds?> =
        MutableStateFlow(null)
    private val _uiState: MutableStateFlow<KmlLandReaderStates> =
        MutableStateFlow(KmlLandReaderStates.WaitingFile)

    val uiState: StateFlow<KmlLandReaderStates> get()= _uiState.asStateFlow()

    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            DefaultMapTarget,
            DefaultMapZoom
        )
    )

    fun onLandSelect(land: Land){
        _uiState.update {
            if(it is KmlLandReaderStates.LoadedLands)
                KmlLandReaderStates.LandSelected(
                    lands = it.lands,
                    selectedLand = land
                )
            else
                it
        }
        land.border.toLatLngBounds()?.let{ bounds ->
            cameraJob?.cancel()
            cameraJob = viewModelScope.launch(Dispatchers.Main){
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds,128)
                )
            }
        }
    }

    fun onClearSelectedLand(){
        _uiState.update {
            if(it is KmlLandReaderStates.LoadedLands)
                KmlLandReaderStates.NoLandSelected(
                    lands = it.lands
                )
            else
                it
        }
        _landsBounds.value?.let{ bounds ->
            cameraJob?.cancel()
            cameraJob = viewModelScope.launch(Dispatchers.Main){
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds,128)
                )
            }
        }
    }

    suspend fun parseFile(inputStream: InputStream){
        _uiState.update { KmlLandReaderStates.LoadingFile }
        try{
            readKml(inputStream).let{ lands ->
                if(lands.isEmpty()) {
                    _uiState.update { KmlLandReaderStates.NoLandsFound }
                } else {
                    _landsBounds.update{
                        LatLngBounds.Builder().run {
                            var size = 0
                            lands.forEach { land ->
                                land.border.forEach{ point ->
                                    include(point)
                                    size++
                                }
                            }
                            if(size > 0)
                                build()
                            else
                                null
                        }
                    }
                    _landsBounds.value?.let{ bounds ->
                        viewModelScope.async(Dispatchers.Main){
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngBounds(bounds,64)
                            )
                        }.await()
                    }
                    _uiState.update {
                        KmlLandReaderStates.NoLandSelected(
                            lands = lands
                        )
                    }
                }
            }
        }catch (e: Exception){
            _uiState.update { KmlLandReaderStates.ErrorParsing }
        }
    }

}