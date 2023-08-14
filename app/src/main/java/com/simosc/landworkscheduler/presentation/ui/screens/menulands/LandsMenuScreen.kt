package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextOverflow
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
    onChangeToNormalState: () -> Unit = {},
    onChangeToDeleteState: () -> Unit = {},
    onChangeToExportState: () -> Unit = {},
    onDeleteSelectedLands: () -> Unit = {},
    onExportSelectedLands: () -> Unit = {},
) {
    BackHandler(uiState is LandsMenuStates.MultiSelectLands){
        onChangeToNormalState()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "My Lands",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(uiState is LandsMenuStates.MultiSelectLands)
                                onChangeToNormalState()
                            else
                                onBackPress()
                        }
                    ) {
                        Icon(
                            imageVector = if(uiState is LandsMenuStates.MultiSelectLands)
                                    Icons.Rounded.Close
                                else
                                    Icons.Rounded.ArrowBack,
                            contentDescription = "Navigate Back Button"
                        )
                    }
                },
                actions = {
                    when(uiState){
                        is LandsMenuStates.Loaded -> {
                            if(uiState.lands.isNotEmpty()) {
                                IconButton(
                                    onClick = onChangeToExportState
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Export Lands",
                                    )
                                }
                                IconButton(
                                    onClick = onChangeToDeleteState
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Delete Lands",
                                    )
                                }
                            }
                            IconButton(
                                onClick = onNewLandPress
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Create New Land",
                                )
                            }
                        }
                        is LandsMenuStates.ExportLands -> {
                            IconButton(
                                onClick = onExportSelectedLands
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Export Selected Lands",
                                )
                            }
                        }
                        is LandsMenuStates.DeleteLands -> {
                            IconButton(
                                onClick = onDeleteSelectedLands
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete Selected Lands",
                                )
                            }
                        }
                        else -> {}
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
                    is LandsMenuStates.Loading -> {
                        LoadingContentComponent()
                    }
                    is LandsMenuStates.Loaded,
                    is LandsMenuStates.MultiSelectLands-> {
                        val lands = remember(uiState){
                            when (uiState) {
                                is LandsMenuStates.Loaded -> uiState.lands
                                is LandsMenuStates.MultiSelectLands -> uiState.lands
                                else -> emptyList()
                            }
                        }
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
                            if (lands.isEmpty()) {
                                item {
                                    Text(
                                        text = "Add new land",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .wrapContentHeight()
                                    )
                                }
                            }else{
                                items(
                                    items = lands,
                                    key = { it.id }
                                ) { land ->
                                    val isLast = lands.lastOrNull() != land
                                    LandItem(
                                        land = land,
                                        showDivider = isLast,
                                        showCheckBox = uiState is LandsMenuStates.MultiSelectLands,
                                        isChecked = if(uiState is LandsMenuStates.MultiSelectLands)
                                                        uiState.selectedLands.contains(land)
                                                    else
                                                        false,
                                        coroutineScope = scope,
                                        mapUiSettings = mapUiSettings,
                                        onLandClick = onLandPress
                                    )
                                }
                            }
                        }
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
    showCheckBox: Boolean,
    isChecked: Boolean,
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
                if(showCheckBox){
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onLandClick(land) }
                    )
                }else{
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
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
    val mockLands = List(10){ index ->
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
    LandsMenuScreen(
        uiState = LandsMenuStates.Loaded(
            lands = mockLands
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenExport(){
    val mockLands = List(10){ index ->
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
    LandsMenuScreen(
        uiState = LandsMenuStates.ExportLands(
            lands = mockLands
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenDelete(){
    val mockLands = List(10){ index ->
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
    LandsMenuScreen(
        uiState = LandsMenuStates.DeleteLands(
            lands = mockLands
        )
    )
}
