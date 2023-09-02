package com.simosc.landworkscheduler.presentation.ui.screens.editorland

import android.location.Address
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.core.config.DefaultMapZoom
import com.simosc.landworkscheduler.domain.exception.BorderListException
import com.simosc.landworkscheduler.domain.exception.TitleException
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.extension.trimWithSingleWhitespaces
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.land.GetLand
import com.simosc.landworkscheduler.domain.usecase.land.InsertLand
import com.simosc.landworkscheduler.domain.usecase.location.GetGeoLocationAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LandEditorViewModel @Inject constructor(
    private val getLand: GetLand,
    private val insertLand: InsertLand,
    private val getGeoLocationAddress: GetGeoLocationAddress,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {
    private var syncJob: Job? = null

    val cameraPositionState: CameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            DefaultMapTarget,
            DefaultMapZoom
        )
    )

    private val _error: MutableSharedFlow<Int?> = MutableSharedFlow()
    val error: SharedFlow<Int?> = _error.asSharedFlow()

    private val _selectedAddress = savedStateHandle.getStateFlow<Address?>(
        "LandEditorSelectedAddress",
        null
    )

    private val _selectedId = savedStateHandle.getStateFlow(
        "LandEditorSelectedId",
        0L
    )

    val uiState = savedStateHandle.getStateFlow<LandEditorStates>(
        "LandEditorUiStates",
        LandEditorStates.LoadingState
    )

    private fun syncData(landId: Long) {
        syncJob?.cancel()
        if(landId > 0L) {
            syncJob = getLand(landId).onEach { land ->
                val bounds = land?.border?.toLatLngBounds()
                val address = _selectedAddress.value
                when{

                    bounds != null -> {
                        viewModelScope.launch(Dispatchers.Main){
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngBounds(
                                    bounds,
                                    128
                                )
                            )
                        }
                        savedStateHandle["LandEditorSelectedAddress"] =
                            Address(Locale.getDefault()).apply {
                                latitude = bounds.center.latitude
                                longitude = bounds.center.longitude
                            }
                        savedStateHandle["LandEditorUiStates"] =
                            LandEditorStates.NormalState(
                                land = land
                            )
                    }

                    address != null -> {
                        LatLng(address.latitude, address.longitude).let{ point ->
                            viewModelScope.launch(Dispatchers.Main) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLng(point)
                                )
                            }
                        }
                        savedStateHandle["LandEditorUiStates"] =
                            LandEditorStates.NormalState(
                                land = land ?: Land.emptyLand().copy(id = landId)
                            )
                    }

                    else -> {
                        savedStateHandle["LandEditorUiStates"] =
                            LandEditorStates.NeedLocation(
                                land = land ?: Land.emptyLand().copy(id = landId)
                            )
                    }
                }
            }.launchIn(
                scope = viewModelScope + Dispatchers.IO
            )
        }else{
            val address = _selectedAddress.value
            if(address != null){
                LatLng(address.latitude, address.longitude).let{ point ->
                    viewModelScope.launch(Dispatchers.Main) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLng(point)
                        )
                    }
                }
                savedStateHandle["LandEditorUiStates"] =
                    LandEditorStates.NormalState(
                        land = Land.emptyLand()
                    )
            }else{
                savedStateHandle["LandEditorUiStates"] =
                    LandEditorStates.NeedLocation(
                        land = Land.emptyLand()
                    )
            }
        }
    }

    fun setSelectedId(landId: Long){
        if(landId > 0L){
            savedStateHandle["LandEditorSelectedId"] = landId
            syncData(landId)
        }else{
            savedStateHandle["LandEditorSelectedId"] = 0L
            syncData(0L)
        }
    }

    fun setAction(action: LandEditorActions){
        uiState.value.let { state ->
            if(state is LandEditorStates.NormalState){
                savedStateHandle["LandEditorUiStates"] =
                    state.toEditState(action)
            }else if(state is LandEditorStates.EditState){
                savedStateHandle["LandEditorUiStates"] =
                    state.submitEdit().toEditState(action)
            }
        }
    }

    fun onMapClick(point: LatLng){
        uiState.value.let { state ->
            when(state){
                is LandEditorStates.AddPointState -> {
                    savedStateHandle["LandEditorUiStates"] =
                        state.performAction(point)
                }
                is LandEditorStates.AddBetweenPointState -> {
                    savedStateHandle["LandEditorUiStates"] =
                        state.performAction(point)
                }
                is LandEditorStates.DeletePointState -> {
                    savedStateHandle["LandEditorUiStates"] =
                        state.performAction(point)
                }
                is LandEditorStates.EditPointState -> {
                    savedStateHandle["LandEditorUiStates"] =
                        state.performAction(point)
                }
                else -> {}
            }
        }
    }

    fun setLandTitleAndAddress(title: String, address: String) = viewModelScope.launch{
        title.trimWithSingleWhitespaces().let{ newTitle ->
            uiState.value.let{ state ->
                when(state){
                    is LandEditorStates.NeedLocation ->
                        savedStateHandle["LandEditorUiStates"] =
                            LandEditorStates.NeedLocation(
                                state.land.copy(title = newTitle)
                            )

                    is LandEditorStates.NormalState ->
                        savedStateHandle["LandEditorUiStates"] =
                            LandEditorStates.NormalState(
                                land = state.land,
                                newTitle = newTitle
                            )

                    else ->
                        savedStateHandle["LandEditorUiStates"] =
                            state
                }
            }
        }
        uiState.value.let { state ->
            if (state is LandEditorStates.NeedLocation && address.isNotBlank()) {

                savedStateHandle["LandEditorUiStates"] =
                    LandEditorStates.LoadingState

                getGeoLocationAddress(address).let { addressList ->
                    Log.d("TAG", "getGeoLocationAddress: $addressList")
                    addressList.firstOrNull {
                        it.hasLatitude() && it.hasLatitude()
                    }?.let { address ->
                        savedStateHandle["LandEditorSelectedAddress"] = address
                        LatLng(address.latitude, address.longitude).let{ point ->
                            viewModelScope.launch(Dispatchers.Main) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLng(point)
                                )
                            }
                        }
                        savedStateHandle["LandEditorUiStates"] =
                            state.toNormalState()
                    } ?: run {
                        _error.tryEmit(R.string.land_editor_error_land_address_is_not_found)
                        savedStateHandle["LandEditorUiStates"] =
                            state
                    }
                }
            }
        }
    }

    fun onUpdateTitle(title: String){
        uiState.value.let { state ->
            if(state is LandEditorStates.EditTitleState) {
                savedStateHandle["LandEditorUiStates"] =
                    state.performAction(title.trimWithSingleWhitespaces()).submitEdit()
            }
        }
    }

    fun onUpdateColor(color: Color){
        uiState.value.let { state ->
            if(state is LandEditorStates.EditColorState)
                savedStateHandle["LandEditorUiStates"] =
                    state.performAction(color).submitEdit()
        }
    }

    fun onSubmitAction(){
        uiState.value.let { state ->
            if(state is LandEditorStates.EditState)
                savedStateHandle["LandEditorUiStates"] =
                    state.submitEdit()
        }
    }

    fun onCancelAction(){
        uiState.value.let { state ->
            if(state is LandEditorStates.EditState)
                savedStateHandle["LandEditorUiStates"] =
                    state.cancelEdit()
        }
    }

    suspend fun onSaveLand(): Boolean{
        var tempState: LandEditorStates.NormalState? = null
        uiState.value.let { state ->
            if(state is LandEditorStates.NormalState){
                tempState = state
            }else if(state is LandEditorStates.EditState){
                tempState = state.submitEdit()
            }
        }
        tempState?.let{ state ->
            return viewModelScope.async(Dispatchers.IO){
                try {
                    insertLand(
                        state.land.copy(
                            title = state.newTitle,
                            color = Color(state.newColor),
                            border = state.newBorder,
                            holes = state.newHoles
                        )
                    )
                    true
                }catch (e: Exception){
                    when(e){
                        is BorderListException ->
                            _error.tryEmit(R.string.land_editor_error_land_border_is_empty)

                        is TitleException ->
                            _error.tryEmit(R.string.land_editor_error_land_title_is_empty)

                        else ->
                            _error.tryEmit(R.string.land_editor_error_land_save_error)

                    }
                    false
                }
            }.await()
        }
        return false
    }

    fun onResetAction(){
        uiState.value.let { state ->
            if(state is LandEditorStates.NormalState) {
                savedStateHandle["LandEditorUiStates"] =
                    LandEditorStates.NormalState(land = state.land)
            }else if(state is LandEditorStates.EditState){
                savedStateHandle["LandEditorUiStates"] =
                    LandEditorStates.NormalState(land = state.land)
            }
        }
    }

    fun onImportLand(land: Land) {
        land.border.toLatLngBounds()?.let{ bounds ->
            viewModelScope.launch(Dispatchers.Main){
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        128
                    )
                )
            }
        }

        val originalLand = uiState.value.let { state ->
            when(state){
                is LandEditorStates.NeedLocation ->
                    state.land

                is LandEditorStates.NormalState ->
                    state.land

                is LandEditorStates.EditState -> {
                    state.land
                }

                else ->
                    Land.emptyLand().copy(
                        id = if(_selectedId.value > 0L)
                            _selectedId.value
                        else
                            0L
                    )
            }
        }

        savedStateHandle["LandEditorUiStates"] =
            LandEditorStates.NormalState(
                land = originalLand,
                newTitle = land.title,
                newColor = land.color.toArgb(),
                newBorder = land.border,
                newHoles = land.holes
            )
    }
}

