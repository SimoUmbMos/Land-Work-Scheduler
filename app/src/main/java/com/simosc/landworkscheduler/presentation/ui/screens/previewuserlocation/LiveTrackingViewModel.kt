package com.simosc.landworkscheduler.presentation.ui.screens.previewuserlocation

import android.Manifest
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simosc.landworkscheduler.domain.enums.LocationPermission
import com.simosc.landworkscheduler.domain.enums.LocationStatues
import com.simosc.landworkscheduler.domain.exception.LocationPermissionException
import com.simosc.landworkscheduler.domain.exception.LocationProviderException
import com.simosc.landworkscheduler.domain.exception.NoAccelerometerSensorException
import com.simosc.landworkscheduler.domain.exception.NoMagnetometerSensorException
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.domain.usecase.location.GetAzimuth
import com.simosc.landworkscheduler.domain.usecase.land.GetLands
import com.simosc.landworkscheduler.domain.usecase.location.GetLocation
import com.simosc.landworkscheduler.domain.usecase.note.GetNotes
import com.simosc.landworkscheduler.domain.usecase.zone.GetZones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val getLandsUseCase: GetLands,
    private val getZonesUseCase: GetZones,
    private val getNotesUseCase: GetNotes,
    private val getLocationUseCase: GetLocation,
    private val getAzimuthUseCase: GetAzimuth
):ViewModel() {

    private var locationJob: Job? = null
    private var azimuthJob: Job? = null

    private var landsJob: Job? = null
    private var zonesJob: Job? = null
    private var notesJob: Job? = null

    private val _lands: MutableStateFlow<List<Land>> =
        MutableStateFlow(emptyList())

    private val _zones: MutableStateFlow<List<Zone>> =
        MutableStateFlow(emptyList())

    private val _notes: MutableStateFlow<List<Note>> =
        MutableStateFlow(emptyList())

    private val _userLocationStatus: MutableStateFlow<LocationStatues> =
        MutableStateFlow(LocationStatues.Waiting)

    private val _locationPermission: MutableStateFlow<LocationPermission> =
        MutableStateFlow(LocationPermission.None)

    private val _userLocation: MutableStateFlow<Location?> =
        MutableStateFlow(null)
    val userLocation: StateFlow<Location?> =
        _userLocation.asStateFlow()

    private val _userAzimuth: MutableStateFlow<Float?> =
        MutableStateFlow(null)
    val userAzimuth: StateFlow<Float?> =
        _userAzimuth.asStateFlow()

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
        _userLocation
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
        CoroutineScope(Dispatchers.IO),
        SharingStarted.WhileSubscribed(5000L),
        LiveTrackingStates.LoadingState
    )

    override fun onCleared() {
        super.onCleared()
        stopDataUpdates()
        stopLocationUpdates()
    }

    fun onPermissionsResult(
        result: Map<String, @JvmSuppressWildcards Boolean>
    ) {
        when{
            result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,false) -> {
                _locationPermission.update { LocationPermission.Fine }
            }
            result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false) -> {
                _locationPermission.update { LocationPermission.Coarse }
            }
            else -> {
                _locationPermission.update { LocationPermission.None }
            }
        }
    }

    fun startDataUpdates(){
        stopDataUpdates()

        landsJob = getLandsUseCase()
            .onEach { lands ->
                _lands.update { lands }
            }.launchIn(
                scope = CoroutineScope(Dispatchers.IO)
            )

        zonesJob = getZonesUseCase()
            .onEach { zones ->
                _zones.update { zones }
            }.launchIn(
                scope = CoroutineScope(Dispatchers.IO)
            )

        notesJob = getNotesUseCase()
            .onEach { notes ->
                _notes.update { notes }
            }
            .launchIn(
                scope = CoroutineScope(Dispatchers.IO)
            )
    }

    fun startLocationUpdates() {
        stopLocationUpdates()
        if(_locationPermission.value != LocationPermission.None){
            locationJob = getLocationUseCase().catch { e ->
                when(e){
                    is LocationPermissionException -> {
                        _userLocationStatus.update {
                            LocationStatues.NeedPermission
                        }
                    }
                    is LocationProviderException -> {
                        _userLocationStatus.update {
                            LocationStatues.NeedProvider
                        }
                    }
                    else -> {
                        _userLocationStatus.update {
                            LocationStatues.Error
                        }
                    }
                }
            }.onEach{ location ->
                _userLocationStatus.update {
                    LocationStatues.Ready
                }
                _userLocation.update {
                    location
                }
            }.launchIn(
                scope = viewModelScope
            )

            azimuthJob = getAzimuthUseCase().catch{e ->
                when(e){
                    is NoMagnetometerSensorException ->
                        _userAzimuth.update {
                            null
                        }
                    is NoAccelerometerSensorException ->
                        _userAzimuth.update {
                            null
                        }
                    else ->
                        _userAzimuth.update {
                            null
                        }
                }
            }.onEach { azimuth ->
                _userAzimuth.update {
                    azimuth
                }
            }.launchIn(
                scope = viewModelScope
            )
        }
    }

    private fun stopDataUpdates(){
        landsJob?.let{
            it.cancel()
            landsJob = null
        }
        zonesJob?.let{
            it.cancel()
            zonesJob = null
        }
        notesJob?.let{
            it.cancel()
            notesJob = null
        }
    }

    fun stopLocationUpdates(){
        locationJob?.let{
            it.cancel()
            locationJob = null
        }
        azimuthJob?.let {
            it.cancel()
            azimuthJob = null
        }
    }

    fun setLoadingState() {
        _userLocationStatus.update {
            LocationStatues.Waiting
        }
    }

}