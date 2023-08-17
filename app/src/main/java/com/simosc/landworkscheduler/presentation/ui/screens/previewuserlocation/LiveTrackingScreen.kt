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
                title = "Live Tracking Screen",
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back Button"
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
                            text = "Waiting Location..."
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
                                    text = "Waiting Location..."
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
                    "Need permission"
                LiveTrackingStates.ErrorState.NeedProviderState ->
                    "Need provider"
                LiveTrackingStates.ErrorState.LocationErrorState ->
                    "Something went wrong"
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
                        text = "Show Permissions"
                    )
                }
            LiveTrackingStates.ErrorState.NeedProviderState ->
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onOpenLocationProviderSettings
                ) {
                    Text(
                        text = "To Settings"
                    )
                }
            else ->
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onStartLocationUpdates
                ) {
                    Text(
                        text = "Try again"
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
    val cameraPositionState: CameraPositionState = rememberCameraPositionState{
        position = CameraPosition.builder().let {
            it.target(userLocation.toLatLng())
            it.zoom(17f)
            it.bearing(userAzimuth ?: userLocation.bearing)
            it.build()
        }
    }
    val markerState: MarkerState = rememberMarkerState(
        position = userLocation.toLatLng()
    )

    LaunchedEffect(userAzimuth){
        userAzimuth?.let{ azimuth ->
            if(!cameraPositionState.isMoving && isMapLoaded) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().let {
                            it.target(cameraPositionState.position.target)
                            it.tilt(cameraPositionState.position.tilt)
                            it.zoom(cameraPositionState.position.zoom)
                            it.bearing(azimuth)
                            it.build()
                        }
                    )
                )
            }
        }
    }

    LaunchedEffect(userLocation){
        userLocation.let { location ->
            userAzimuth.let{ azimuth ->
                markerState.position = location.toLatLng()
                if(isMapLoaded) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newCameraPosition(
                            CameraPosition.builder().let {
                                it.target(location.toLatLng())
                                it.tilt(cameraPositionState.position.tilt)
                                it.zoom(cameraPositionState.position.zoom)
                                it.bearing(azimuth ?: location.bearing)
                                it.build()
                            }
                        )
                    )
                }
            }
        }
    }

    val uiSettings = remember{
        MapUiSettings(
            compassEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = false,
        )
    }

    val properties = remember{
        MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isTrafficEnabled = false,
            mapType = MapType.SATELLITE
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
    val title = remember(uiState){
        "#${uiState.currentLand.id} ${uiState.currentLand.title}"
    }
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
            text = title,
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
    val title = remember(uiState){
        "#${uiState.currentZone.id} ${uiState.currentZone.title}"
    }
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
            text = title,
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
    val title = remember(uiState){
        "#${uiState.currentNote.id} ${uiState.currentNote.title}"
    }
    val desc = remember(uiState){
        uiState.currentNote.desc
    }
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
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = desc,
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
