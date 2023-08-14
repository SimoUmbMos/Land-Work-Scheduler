package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandsMenuScreen(
    uiState: LandsMenuStates,
    onBackPress: () -> Unit = {},
    onLandPress: (Land) -> Unit = {},
    onNewLandPress: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Lands",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back Button"
                        )
                    }
                },
                actions = {
                    if (uiState !is LandsMenuStates.Loading) {
                        IconButton(
                            onClick = onNewLandPress
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Create New Land",
                            )
                        }
                    }
                }
            )
        },
        content = { padding ->
            val scope = rememberCoroutineScope()
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (uiState) {
                    is LandsMenuStates.Loaded -> {
                        if (uiState.lands.isEmpty()) {
                            Text(
                                text = "Add new land",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize()
                            )
                        } else {
                            val mapUiSettings = remember {
                                MapUiSettings(
                                    compassEnabled = false,
                                    indoorLevelPickerEnabled = false,
                                    mapToolbarEnabled = false,
                                    myLocationButtonEnabled = false,
                                    rotationGesturesEnabled = false,
                                    scrollGesturesEnabled = false,
                                    scrollGesturesEnabledDuringRotateOrZoom = false,
                                    tiltGesturesEnabled = false,
                                    zoomControlsEnabled = false,
                                    zoomGesturesEnabled = false
                                )
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = uiState.lands,
                                    key = { it.id }
                                ) { land ->
                                    val isLast = uiState.lands.lastOrNull() != land
                                    LandItem(
                                        land = land,
                                        showDivider = isLast,
                                        coroutineScope = scope,
                                        mapUiSettings = mapUiSettings,
                                        onLandClick = onLandPress
                                    )
                                }
                            }

                        }
                    }

                    LandsMenuStates.Loading -> {
                        LoadingContentComponent()
                    }
                }
            }
        }
    )
}

@Composable
private fun LandItem(
    land: Land,
    showDivider: Boolean,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    mapUiSettings: MapUiSettings = MapUiSettings(
        compassEnabled = false,
        indoorLevelPickerEnabled = false,
        mapToolbarEnabled = false,
        myLocationButtonEnabled = false,
        rotationGesturesEnabled = false,
        scrollGesturesEnabled = false,
        scrollGesturesEnabledDuringRotateOrZoom = false,
        tiltGesturesEnabled = false,
        zoomControlsEnabled = false,
        zoomGesturesEnabled = false
    ),
    onLandClick: (Land) -> Unit,
){
    val cameraPositionState = rememberCameraPositionState()
    var mapProperties by remember{
        mutableStateOf(MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isMyLocationEnabled = false,
            isTrafficEnabled = false,
            mapType = MapType.NONE
        ))
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onLandClick(land)
        }
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Text(text = land.title)
            },
            overlineContent = {
                Text(text = "#${land.id}")
            },
            leadingContent = {
                Surface(
                    modifier = Modifier.size(
                        width = 128.dp,
                        height = 128.dp
                    ),
                    shape = RoundedCornerShape(24.dp)
                ){
                    GoogleMap(
                        cameraPositionState = cameraPositionState,
                        uiSettings = mapUiSettings,
                        properties = mapProperties,
                        onMapLoaded = {
                            coroutineScope.launch {
                                land.border.toLatLngBounds()?.let{ bounds ->
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngBounds(
                                            bounds,
                                            24
                                        )
                                    )
                                }
                            }
                            if(mapProperties.mapType == MapType.NONE) {
                                mapProperties = mapProperties.copy(
                                    mapType = MapType.SATELLITE
                                )
                            }
                        },
                        onMapClick = {
                            onLandClick(land)
                        }
                    ){
                        if(land.border.isNotEmpty()){
                            Polygon(
                                points = land.border,
                                holes = land.holes,
                                strokeColor = land.color.copy(alpha = DefaultMapItemStrokeAlpha),
                                fillColor = land.color.copy(alpha = DefaultMapItemFillAlpha),
                                zIndex = 1f
                            )
                        }
                    }
                    if(land.border.isEmpty()){
                        Text(
                            modifier = Modifier.wrapContentHeight(),
                            textAlign = TextAlign.Center,
                            text = "Land border is empty"
                        )
                    }
                }
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowRight,
                    contentDescription = null
                )
            },
        )
    }
    if(showDivider) {
        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .05f),
            thickness = 1.dp
        )
    }
}



@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenLoading(){
    LandsMenuScreen(
        uiState = LandsMenuStates.Loading
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenLoadedEmpty(){
    LandsMenuScreen(
        uiState = LandsMenuStates.Loaded(
            lands = emptyList()
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenLoaded(){
    LandsMenuScreen(
        uiState = LandsMenuStates.Loaded(
            lands = List(10){ index ->
                Land.emptyLand().copy(
                    id = index + 1L,
                    title = "mock land ${index + 1}",
                    border = listOf(
                        LatLng(0.0,0.0),
                        LatLng(1.0,0.0),
                        LatLng(1.0,1.0),
                        LatLng(0.0,1.0),
                        LatLng(0.0,0.0),
                    ),
                    holes = emptyList()
                )
            }
        )
    )
}
