package com.simosc.landworkscheduler.presentation.ui.screens.editorland

import android.location.Address
import android.util.Log
import androidx.compose.ui.graphics.Color
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
import com.simosc.landworkscheduler.domain.usecase.location.getGeoLocationAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class LandEditorViewModel @Inject constructor(
    private val getLand: GetLand,
    private val insertLand: InsertLand,
    private val getGeoLocationAddress: getGeoLocationAddress
):ViewModel() {
    private var syncJob: Job? = null

    private val _cameraPositionState: CameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(
            DefaultMapTarget,
            DefaultMapZoom
        )
    )

    private val _selectedAddress: MutableStateFlow<Address?> =
        MutableStateFlow(null)
    private val _selectedId: MutableStateFlow<Long> =
        MutableStateFlow(0L)
    private val _error: MutableSharedFlow<Int?> =
        MutableSharedFlow()
    private val _uiState: MutableStateFlow<LandEditorStates> =
        MutableStateFlow(LandEditorStates.LoadingState)

    private fun stopSync() {
        syncJob?.let{
            it.cancel()
            syncJob = null
        }
    }

    private fun syncData() {
        stopSync()
        _selectedId.value.let { selectedId ->
            if(selectedId > 0L) {
                syncJob = getLand(selectedId).onEach { land ->
                    land?.let {
                        _uiState.update {
                            LandEditorStates.NormalState(land = land)
                        }
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
                    }?:run{
                        _selectedAddress.value?.let{ address ->
                            LatLng(address.latitude, address.longitude).let{ point ->
                                viewModelScope.launch(Dispatchers.Main) {
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLng(point)
                                    )
                                }
                            }
                            _uiState.update{
                                LandEditorStates.NormalState(
                                    land = Land.emptyLand().copy(id = selectedId)
                                )
                            }
                        }?:run{
                            _uiState.update{
                                LandEditorStates.NeedLocation(
                                    land = Land.emptyLand().copy(id = selectedId)
                                )
                            }
                        }
                    }
                }.launchIn(
                    scope = viewModelScope + Dispatchers.IO
                )
            }else{
                _selectedAddress.value?.let{ address ->
                    LatLng(address.latitude, address.longitude).let{ point ->
                        viewModelScope.launch(Dispatchers.Main) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLng(point)
                            )
                        }
                    }
                    _uiState.update{
                        LandEditorStates.NormalState(
                            land = Land.emptyLand()
                        )
                    }
                }?:run{
                    _uiState.update{
                        LandEditorStates.NeedLocation(
                            land = Land.emptyLand()
                        )
                    }
                }
            }
        }
    }

    val cameraPositionState: CameraPositionState
        get() = _cameraPositionState
    val error: SharedFlow<Int?>
        get() = _error.asSharedFlow()
    val uiState: StateFlow<LandEditorStates>
        get() = _uiState.asStateFlow()

    fun setSelectedId(id: Long){
        _selectedId.update {
            if(id > 0L)
                id
            else
                0L
        }
        syncData()
    }

    fun setAction(action: LandEditorActions){
        _uiState.value.let { state ->
            if(state is LandEditorStates.NormalState){
                _uiState.update { state.toEditState(action) }
            }else if(state is LandEditorStates.EditState){
                _uiState.update { state.submitEdit().toEditState(action) }
            }
        }
    }

    fun onMapClick(point: LatLng){
        _uiState.value.let { state ->
            when(state){
                is LandEditorStates.AddPointState -> {
                    _uiState.update { state.performAction(point) }
                }
                is LandEditorStates.AddBetweenPointState -> {
                    _uiState.update { state.performAction(point) }
                }
                is LandEditorStates.DeletePointState -> {
                    _uiState.update { state.performAction(point) }
                }
                is LandEditorStates.EditPointState -> {
                    _uiState.update { state.performAction(point) }
                }
                else -> {}
            }
        }
    }

    fun setLandTitleAndAddress(title: String, address: String) = viewModelScope.launch{
        title.trimWithSingleWhitespaces().let{ newTitle ->
            if(newTitle.isNotBlank()){
                _uiState.update {
                    when(it){
                        is LandEditorStates.NeedLocation ->
                            LandEditorStates.NeedLocation(
                                it.land.copy(title = newTitle)
                            )

                        is LandEditorStates.NormalState ->
                            LandEditorStates.NormalState(
                                land = it.land,
                                newTitle = newTitle
                            )

                        else ->
                            it
                    }
                }
            }
        }
        _uiState.value.let { state ->
            if (state is LandEditorStates.NeedLocation && address.isNotBlank()) {
                _uiState.update { LandEditorStates.LoadingState }
                getGeoLocationAddress(address).let { addressList ->
                    Log.d("TAG", "getGeoLocationAddress: $addressList")
                    addressList.firstOrNull {
                        it.hasLatitude() && it.hasLatitude()
                    }?.let { result ->
                        _selectedAddress.update { result }
                        LatLng(result.latitude, result.longitude).let{ point ->
                            viewModelScope.launch(Dispatchers.Main) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLng(point)
                                )
                            }
                        }
                        _uiState.update { state.toNormalState() }
                    } ?: run {
                        _error.tryEmit(R.string.land_editor_error_land_address_is_not_found)
                        _uiState.update { state }
                    }
                }
            }
        }
    }

    fun onUpdateTitle(title: String){
        _uiState.value.let { state ->
            if(state is LandEditorStates.EditTitleState) {
                _uiState.update {
                    state.performAction(title.trimWithSingleWhitespaces()).submitEdit()
                }
            }
        }
    }

    fun onUpdateColor(color: Color){
        _uiState.value.let { state ->
            if(state is LandEditorStates.EditColorState)
                _uiState.update { state.performAction(color).submitEdit() }
        }
    }

    fun onSubmitAction(){
        _uiState.value.let { state ->
            if(state is LandEditorStates.EditState)
                _uiState.update { state.submitEdit() }
        }
    }

    fun onCancelAction(){
        _uiState.value.let { state ->
            if(state is LandEditorStates.EditState)
                _uiState.update { state.cancelEdit() }
        }
    }

    suspend fun onSaveLand(): Boolean{
        var tempState: LandEditorStates.NormalState? = null
        _uiState.value.let { state ->
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
                            color = state.newColor,
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
        _uiState.value.let { state ->
            if(state is LandEditorStates.NormalState) {
                _uiState.update { LandEditorStates.NormalState(land = state.land) }
            }else if(state is LandEditorStates.EditState){
                _uiState.update { LandEditorStates.NormalState(land = state.land) }
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

        val originalLand = _uiState.value.let { state ->
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

        _uiState.update {
            LandEditorStates.NormalState(
                land = originalLand,
                newTitle = land.title,
                newColor = land.color,
                newBorder = land.border,
                newHoles = land.holes
            )
        }
    }
}

