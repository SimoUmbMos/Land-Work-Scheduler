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
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.core.config.DefaultMapZoom
import com.simosc.landworkscheduler.domain.exception.BorderListException
import com.simosc.landworkscheduler.domain.exception.TitleException
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.usecase.land.GetLand
import com.simosc.landworkscheduler.domain.usecase.land.InsertLand
import com.simosc.landworkscheduler.domain.usecase.location.getGeoLocationAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val _error: MutableStateFlow<String?> =
        MutableStateFlow(null)
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
                }.launchIn(CoroutineScope(Dispatchers.IO))
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

    override fun onCleared() {
        super.onCleared()
        stopSync()
    }

    val cameraPositionState: CameraPositionState
        get() = _cameraPositionState
    val error: StateFlow<String?>
        get() = _error.asStateFlow()
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

    fun clearError(){
        _error.update { null }
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

    fun setAddress(address: String) = viewModelScope.launch{
        if(address.isNotBlank()) {
            _uiState.value.let { state ->
                if (state is LandEditorStates.NeedLocation) {
                    _uiState.update {
                        LandEditorStates.LoadingState
                    }
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
                            _uiState.update {
                                state.toNormalState()
                            }
                        } ?: run {
                            _error.update { "Can't find the give address" }
                            _uiState.update {
                                state
                            }
                        }
                    }
                }
            }
        }
    }

    fun onUpdateTitle(title: String){
        _uiState.value.let { state ->
            if(state is LandEditorStates.EditTitleState)
                _uiState.update { state.performAction(title).submitEdit() }
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
                            _error.update { "Border is Empty" }
                        is TitleException ->
                            _error.update { "Title is Empty" }
                        else ->
                            _error.update { "Something went wrong" }
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
}

