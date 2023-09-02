package com.simosc.landworkscheduler.presentation.ui.screens.previewuserlocation

import android.Manifest
import android.content.res.Configuration
import android.location.Location
import android.view.Surface
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.enums.LocationPermission
import com.simosc.landworkscheduler.domain.enums.LocationStatues
import com.simosc.landworkscheduler.domain.exception.LocationPermissionException
import com.simosc.landworkscheduler.domain.exception.LocationProviderException
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.usecase.location.GetAzimuth
import com.simosc.landworkscheduler.domain.usecase.land.GetLands
import com.simosc.landworkscheduler.domain.usecase.location.GetLocation
import com.simosc.landworkscheduler.domain.usecase.note.GetNotes
import com.simosc.landworkscheduler.domain.usecase.zone.GetZones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val getLandsUseCase: GetLands,
    private val getZonesUseCase: GetZones,
    private val getNotesUseCase: GetNotes,
    private val getLocationUseCase: GetLocation,
    private val getAzimuthUseCase: GetAzimuth,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {

    private var locationJob: Job? = null
    private var azimuthJob: Job? = null

    private var landsJob: Job? = null
    private var zonesJob: Job? = null
    private var notesJob: Job? = null

    private val _locationPermission = savedStateHandle.getStateFlow(
        "LiveTrackingLocationPermission",
        LocationPermission.None
    )

    val userLocation = savedStateHandle.getStateFlow<Location?>(
        "LiveTrackingUserLocation",
        null
    )

    private val _rawAzimuth = savedStateHandle.getStateFlow<Float?>(
        "LiveTrackingRawAzimuth",
        null
    )

    private val _displayOrientation = savedStateHandle.getStateFlow<Float?>(
        "LiveTrackingDisplayOrientation",
        null
    )

    val userAzimuth = _rawAzimuth.combine(
        _displayOrientation
    ){ rawAzimuth, displayOrientation ->
        if(rawAzimuth != null && displayOrientation != null)
            (rawAzimuth - displayOrientation).let{ azimuth ->
                when{
                    azimuth > 180f -> azimuth - 360f
                    azimuth < -180.0f -> azimuth + 360f
                    else -> azimuth
                }
            }
        else
            null
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        null
    )

    private val _lands = savedStateHandle.getStateFlow<List<Land>>(
        "LiveTrackingLands",
        emptyList()
    )
    private val _zones = savedStateHandle.getStateFlow<List<Zone>>(
        "LiveTrackingZones",
        emptyList()
    )
    private val _notes = savedStateHandle.getStateFlow<List<Note>>(
        "LiveTrackingNotes",
        emptyList()
    )
    private val _userLocationStatus = savedStateHandle.getStateFlow(
        "LiveTrackingLocationStatues",
        LocationStatues.Waiting
    )

    val uiState: StateFlow<LiveTrackingStates> = combine(
        _lands, _zones, _notes, _userLocationStatus
    ){ lands, zones, notes, userLocationStatus ->
        when(userLocationStatus){
            LocationStatues.Ready ->
                LiveTrackingStates.ReadyState.WaitingUserLocationState(
                    lands = lands,
                    zones = zones,
                    notes = notes
                )

            LocationStatues.NeedPermission,
            LocationStatues.NeedProvider,
            LocationStatues.Error ->
                LiveTrackingStates.ErrorState.getErrorState(
                    userLocationStatus
                )

            else ->
                LiveTrackingStates.LoadingState
        }
    }.combine(
        userLocation
    ){ currentUiState, userLocation ->
        if(
            currentUiState is LiveTrackingStates.ReadyState.WaitingUserLocationState &&
            userLocation != null
        )
            currentUiState.findLocationState(
                userLocation = userLocation
            )
        else
            currentUiState
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        LiveTrackingStates.LoadingState
    )

    fun onPermissionsResult(result: Map<String, @JvmSuppressWildcards Boolean>) {
        savedStateHandle["LiveTrackingLocationPermission"] = when{
            result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,false) ->
                LocationPermission.Fine
            result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false) ->
                LocationPermission.Coarse
            else ->
                LocationPermission.None
        }
    }

    fun startDataUpdates(){
        landsJob?.cancel()
        landsJob = getLandsUseCase()
            .onEach { lands ->
                savedStateHandle["LiveTrackingLands"] = lands
            }.launchIn(
                scope = viewModelScope + Dispatchers.IO
            )

        zonesJob?.cancel()
        zonesJob = getZonesUseCase()
            .onEach { zones ->
                savedStateHandle["LiveTrackingZones"] = zones
            }.launchIn(
                scope = viewModelScope + Dispatchers.IO
            )

        notesJob?.cancel()
        notesJob = getNotesUseCase()
            .onEach { notes ->
                savedStateHandle["LiveTrackingNotes"] = notes
            }
            .launchIn(
                scope = viewModelScope + Dispatchers.IO
            )
    }

    fun startLocationUpdates() {
        if(_locationPermission.value != LocationPermission.None){
            locationJob?.cancel()
            locationJob = getLocationUseCase().catch { e ->
                savedStateHandle["LiveTrackingLocationStatues"] = when(e){
                    is LocationPermissionException ->
                        LocationStatues.NeedPermission
                    is LocationProviderException ->
                        LocationStatues.NeedProvider
                    else ->
                        LocationStatues.Error
                }
            }.onEach{ location ->
                savedStateHandle["LiveTrackingLocationStatues"] =
                    LocationStatues.Ready
                savedStateHandle["LiveTrackingUserLocation"] =
                    location
            }.launchIn(
                scope = viewModelScope
            )

            azimuthJob?.cancel()
            azimuthJob = getAzimuthUseCase().catch{
                savedStateHandle["LiveTrackingRawAzimuth"] = null
            }.onEach { azimuth ->
                savedStateHandle["LiveTrackingRawAzimuth"] = azimuth
            }.launchIn(
                scope = viewModelScope
            )
        }
    }

    fun stopLocationUpdates() {
        locationJob?.cancel()
        azimuthJob?.cancel()
    }

    fun setLoadingState() {
        savedStateHandle["LiveTrackingLocationStatues"] =
            LocationStatues.Waiting
    }

    fun setOrientation(orientation: Int, rotation: Int) {
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(rotation == Surface.ROTATION_90){
                savedStateHandle["LiveTrackingDisplayOrientation"] = -90f
            }else if(rotation == Surface.ROTATION_270){
                savedStateHandle["LiveTrackingDisplayOrientation"] = 90f
            }
        }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
            savedStateHandle["LiveTrackingDisplayOrientation"] = 0f
        }
    }

}