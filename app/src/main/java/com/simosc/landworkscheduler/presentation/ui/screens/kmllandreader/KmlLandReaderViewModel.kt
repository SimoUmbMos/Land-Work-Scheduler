package com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.ReadKml
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KmlLandReaderViewModel @Inject constructor(
    private val readKml: ReadKml,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {

    val uiState = savedStateHandle.getStateFlow<KmlLandReaderStates>(
        "KmlLandReaderStates",
        KmlLandReaderStates.WaitingFile
    )

    fun onLandSelect(land: Land){
        uiState.value.let { state ->
            if(state is KmlLandReaderStates.LoadedLands)
                savedStateHandle["KmlLandReaderStates"] = KmlLandReaderStates.LandSelected(
                    lands = state.lands,
                    selectedLand = land
                )
        }
    }

    fun onClearSelectedLand(){
        uiState.value.let { state ->
            if(state is KmlLandReaderStates.LoadedLands)
                savedStateHandle["KmlLandReaderStates"] = KmlLandReaderStates.NoLandSelected(
                    lands = state.lands
                )
        }
    }

    fun onOpenDocumentResult(
        uri: Uri?,
        resolver: ContentResolver,
        onCancel: () -> Unit
    ) {
        savedStateHandle["KmlLandReaderStates"] = KmlLandReaderStates.LoadingFile
        if (uri != null) viewModelScope.launch(Dispatchers.IO) {
                resolver.openInputStream(uri)?.use { inputStream ->
                    try{
                        readKml(inputStream).let{ lands ->
                            savedStateHandle["KmlLandReaderStates"] = if(lands.isEmpty()) {
                                KmlLandReaderStates.NoLandsFound
                            } else {
                                KmlLandReaderStates.NoLandSelected(
                                    lands = lands
                                )
                            }
                        }
                    }catch (e: Exception){
                        savedStateHandle["KmlLandReaderStates"] = KmlLandReaderStates.ErrorParsing
                    }
                } ?: onCancel()
        } else
            onCancel()
    }

}