package com.simosc.landworkscheduler.presentation.ui.screens.previewuserlocation

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.domain.extension.toLatLng
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.model.Zone
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar


@Composable
fun LiveTrackingScreen(
    uiState: LiveTrackingStates,
    userLocation: Location? = null,
    userAzimuth: Float? = null,
    onBackPress: () -> Unit = {},
    onAskLocationPermission: () -> Unit = {},
    onOpenLocationProviderSettings: () -> Unit = {},
    onStartLocationUpdates: () -> Unit = {}
){
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(id = R.string.live_tracking_title_default),
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_label)
                        )
                    }
                }
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when(uiState){
                    is LiveTrackingStates.LoadingState ->
                        LoadingContentComponent(
                            modifier = Modifier
                                .fillMaxSize()
                        )

                    is LiveTrackingStates.ErrorState ->
                        ErrorScreenContent(
                            uiState = uiState,
                            onAskLocationPermission = onAskLocationPermission,
                            onOpenLocationProviderSettings = onOpenLocationProviderSettings,
                            onStartLocationUpdates = onStartLocationUpdates
                        )

                    is LiveTrackingStates.ReadyState.WaitingUserLocationState ->
                        LoadingContentComponent(
                            modifier = Modifier
                                .fillMaxSize(),
                            text = stringResource(id = R.string.live_tracking_content_waiting_location)
                        )

                    is LiveTrackingStates.ReadyState ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ){
                            if(userLocation != null) {
                                UserLocationMap(
                                    uiState = uiState,
                                    userLocation = userLocation,
                                    userAzimuth = userAzimuth
                                )
                            }else{
                                LoadingContentComponent(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    text = stringResource(id = R.string.live_tracking_content_waiting_location)
                                )
                            }

                            when(uiState){
                                is LiveTrackingStates.ReadyState.InsideLandState ->
                                    InfoCard(uiState = uiState)
                                is LiveTrackingStates.ReadyState.InsideNoteState ->
                                    InfoCard(uiState = uiState)
                                is LiveTrackingStates.ReadyState.InsideZoneState ->
                                    InfoCard(uiState = uiState)
                                else -> {}
                            }
                        }
                }
            }
        }
    )
}

@Composable
private fun ErrorScreenContent(
    uiState: LiveTrackingStates.ErrorState,
    onAskLocationPermission: () -> Unit,
    onOpenLocationProviderSettings: () -> Unit = {},
    onStartLocationUpdates: () -> Unit
){
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = when (uiState) {
                LiveTrackingStates.ErrorState.NeedPermissionState ->
                    stringResource(id = R.string.live_tracking_error_need_location_permission)
                LiveTrackingStates.ErrorState.NeedProviderState ->
                    stringResource(id = R.string.live_tracking_error_need_location_provider)
                LiveTrackingStates.ErrorState.LocationErrorState ->
                    stringResource(id = R.string.live_tracking_error_cant_get_location)
            },
            textAlign = TextAlign.Center
        )
        when(uiState){
            LiveTrackingStates.ErrorState.NeedPermissionState ->
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onAskLocationPermission
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.live_tracking_error_button_need_location_permission
                        )
                    )
                }
            LiveTrackingStates.ErrorState.NeedProviderState ->
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onOpenLocationProviderSettings
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.live_tracking_error_button_need_location_provider
                        )
                    )
                }
            else ->
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onStartLocationUpdates
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.live_tracking_error_button_cant_get_location
                        )
                    )
                }
        }
    }

}

@Composable
private fun UserLocationMap(
    uiState: LiveTrackingStates.ReadyState,
    userLocation: Location,
    userAzimuth: Float?,
){
    var isMapLoaded by remember {
        mutableStateOf(false)
    }
    var zoomControlsEnabled by remember{
        mutableStateOf(true)
    }
    val cameraPositionState: CameraPositionState = rememberCameraPositionState{
        position = CameraPosition.builder().let {
            it.zoom(16f)
            it.target(userLocation.toLatLng())
            it.bearing(userAzimuth ?: userLocation.bearing)
            it.build()
        }
    }
    val markerState: MarkerState = rememberMarkerState(
        position = userLocation.toLatLng()
    )
    LaunchedEffect(cameraPositionState.isMoving){
        zoomControlsEnabled = !cameraPositionState.isMoving
    }
    LaunchedEffect(userLocation, userAzimuth){
        userLocation.toLatLng().let{ userPosition ->
            if(markerState.position != userPosition)
                markerState.position = userPosition
            if(isMapLoaded && !cameraPositionState.isMoving)
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().let {
                            it.zoom(cameraPositionState.position.zoom)
                            it.target(userPosition)
                            it.bearing(userAzimuth ?: userLocation.bearing)
                            it.build()
                        }
                    )
                )
        }
    }

    val uiSettings = remember(zoomControlsEnabled){
        MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = zoomControlsEnabled,
            zoomGesturesEnabled = false
        )
    }
    val properties = remember{
        MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isTrafficEnabled = false,
            mapType = MapType.SATELLITE,
            maxZoomPreference = 18f,
            minZoomPreference = 14f
        )
    }

    GoogleMap(
        modifier = Modifier .fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
        onMapLoaded = {
            isMapLoaded = true
        }
    ){
        uiState.lands.forEach { land ->
            Polygon(
                points = land.border,
                holes = land.holes,
                strokeColor = land.color.copy(alpha = DefaultMapItemStrokeAlpha),
                fillColor = land.color.copy(alpha = DefaultMapItemFillAlpha),
                zIndex = 1f
            )
        }
        uiState.zones.forEach { zone ->
            Polygon(
                points = zone.border,
                strokeColor = zone.color.copy(alpha = DefaultMapItemStrokeAlpha),
                fillColor = zone.color.copy(alpha = DefaultMapItemFillAlpha),
                zIndex = 2f
            )
        }
        uiState.notes.forEach { note ->
            Circle(
                center = note.center,
                radius = note.radius,
                strokeColor = note.color,
                fillColor = note.color.copy(alpha = 0.25f),
                zIndex = 3f
            )
            Marker(
                state = rememberMarkerState(
                    position = note.center
                ),
                title = note.title,
                snippet = note.desc,
                zIndex = 4f
            )
        }
        Marker(
            state = markerState,
            zIndex = 5f,
            onClick = {true}
        )
    }
}

@Composable
private fun InfoCard(
    uiState: LiveTrackingStates.ReadyState.InsideLandState
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = stringResource(
                id = R.string.live_tracking_card_land_label,
                uiState.currentLand.id,
                uiState.currentLand.title
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoCard(
    uiState: LiveTrackingStates.ReadyState.InsideZoneState
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = stringResource(
                id = R.string.live_tracking_card_zone_label,
                uiState.currentZone.id,
                uiState.currentZone.title
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoCard(
    uiState: LiveTrackingStates.ReadyState.InsideNoteState
) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(
                    id = R.string.live_tracking_card_note_label,
                    uiState.currentNote.id,
                    uiState.currentNote.title
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = uiState.currentNote.desc,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}



@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenLoadingState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.LoadingState,
        userLocation = null
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenPermissionErrorState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ErrorState.NeedPermissionState,
        userLocation = null
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenProviderErrorState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ErrorState.NeedProviderState,
        userLocation = null
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenLocationErrorState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ErrorState.LocationErrorState,
        userLocation = null
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenWaitingUserLocationState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ReadyState.WaitingUserLocationState(),
        userLocation = Location("mock location").apply {
            latitude = 0.0
            longitude = 0.0
        }
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenNotInsideLocation(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ReadyState.NotInsideLocationState(),
        userLocation = Location("mock location").apply {
            latitude = 0.0
            longitude = 0.0
        }
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenInsideLandState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ReadyState.InsideLandState(
            currentLand = Land.emptyLand().copy(
                title = "Mock Land"
            )
        ),
        userLocation = Location("mock location").apply {
            latitude = 0.0
            longitude = 0.0
        }
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenInsideZoneState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ReadyState.InsideZoneState(
            currentZone = Zone.emptyZone(
                lid = -1
            ).copy(
                title = "Mock Zone Title"
            )
        ),
        userLocation = Location("mock location").apply {
            latitude = 0.0
            longitude = 0.0
        }
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLiveTrackingScreenInsideNoteState(){
    LiveTrackingScreen(
        uiState = LiveTrackingStates.ReadyState.InsideNoteState(
            currentNote = Note.emptyNote(
                lid = -1,
                center = LatLng(0.0, 0.0)
            ).copy(
                title = "Mock Note Title",
                desc = "Mock Note Description"
            )
        ),
        userLocation = Location("mock location").apply {
            latitude = 0.0
            longitude = 0.0
        }
    )
}
