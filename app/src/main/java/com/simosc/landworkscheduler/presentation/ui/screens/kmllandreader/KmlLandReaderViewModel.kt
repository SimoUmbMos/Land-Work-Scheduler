package com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.file.ReadKml
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KmlLandReaderViewModel @Inject constructor(
    private val readKml: ReadKml
):ViewModel() {

    private val _uiState: MutableStateFlow<KmlLandReaderStates> =
        MutableStateFlow(KmlLandReaderStates.WaitingFile)

    val uiState: StateFlow<KmlLandReaderStates> get()= _uiState.asStateFlow()

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
    }

    fun onOpenDocumentResult(
        uri: Uri?,
        resolver: ContentResolver,
        onCancel: () -> Unit
    ) {
        _uiState.update {
            KmlLandReaderStates.LoadingFile
        }
        if (uri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                resolver.openInputStream(uri)?.use { inputStream ->
                try{
                    readKml(inputStream).let{ lands ->
                        if(lands.isEmpty()) {
                            launch(Dispatchers.Main){
                                _uiState.update {
                                    KmlLandReaderStates.NoLandsFound
                                }
                            }
                        } else {
                            launch(Dispatchers.Main){
                                _uiState.update {
                                    KmlLandReaderStates.NoLandSelected(
                                        lands = lands
                                    )
                                }
                            }
                        }
                    }
                }catch (e: Exception){
                    launch(Dispatchers.Main){
                        _uiState.update {
                            KmlLandReaderStates.ErrorParsing
                        }
                    }
                }
            } ?: onCancel()
            }
        } else {
            onCancel()
        }
    }

}