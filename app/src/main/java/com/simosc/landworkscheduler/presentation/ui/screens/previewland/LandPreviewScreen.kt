package com.simosc.landworkscheduler.presentation.ui.screens.previewland

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.core.config.DefaultMapZoom
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.MessageDialog
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar
import kotlinx.coroutines.launch


@Composable
fun LandPreviewScreen(
    onBackPress: () -> Unit = {},
    onEditLandPress: () -> Unit = {},
    onDeleteLandPress: () -> Unit = {},
    toLandZonesPress: () -> Unit = {},
    toLandNotesPress: () -> Unit = {},
    initialValueShowDeleteDialog: Boolean = false,
    uiState: LandPreviewStates,
){
    var showDeleteDialog by remember(initialValueShowDeleteDialog){
        mutableStateOf(initialValueShowDeleteDialog)
    }
    LaunchedEffect(uiState){
        if(uiState is LandPreviewStates.LandNotFoundState) {
            onBackPress()
        }
    }
    Scaffold (
        topBar = {
            LandPreviewScreenTopBar(
                uiState = uiState,
                onBackPress = onBackPress,
                onEditLandPress = onEditLandPress,
                onDeleteLandPress = { showDeleteDialog = true },
                toLandZonesPress = toLandZonesPress,
                toLandNotesPress = toLandNotesPress,
            )
        }
    ){ padding ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            if(uiState is LandPreviewStates.Loaded){
                LandPreviewScreenContent(
                    uiState = uiState,
                    showDialog = showDeleteDialog,
                    onSubmitDialog = onDeleteLandPress,
                    onDismissDialog = { showDeleteDialog = false }
                )
            } else {
                LoadingContentComponent()
            }
        }
    }
}

@Composable
private fun LandPreviewScreenTopBar(
    uiState: LandPreviewStates,
    onBackPress: () -> Unit,
    onEditLandPress: () -> Unit,
    onDeleteLandPress: () -> Unit,
    toLandZonesPress: () -> Unit,
    toLandNotesPress: () -> Unit
){
    var showActionsMenu by remember {
        mutableStateOf(false)
    }
    var showNavigationMenu by remember {
        mutableStateOf(false)
    }
    DefaultTopAppBar(
        title = stringResource(
            id = R.string.land_preview_title_default
        ),
        subTitle = if(uiState is LandPreviewStates.Loaded)
            stringResource(
                id = R.string.land_preview_subtitle_loaded,
                uiState.land.id,
                uiState.land.title
            )
        else
            stringResource(
                id = R.string.land_preview_subtitle_default
            ),
        navigationIcon = {
            IconButton(
                onClick = {
                    onBackPress()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack ,
                    contentDescription = stringResource(id = R.string.navigate_back_label)
                )
            }
        },
        actions = {
            if(uiState is LandPreviewStates.Loaded) {
                IconButton(
                    onClick = {
                        showActionsMenu = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(
                            id = R.string.land_preview_action_open_edit_menu
                        )
                    )
                }
                DropdownMenu(
                    expanded = showActionsMenu,
                    onDismissRequest = { showActionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = R.string.land_preview_action_edit_land
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = onEditLandPress
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = R.string.land_preview_action_delete_land
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                        },
                        onClick = onDeleteLandPress
                    )
                }

                IconButton(
                    onClick = {
                        showNavigationMenu = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(
                            id = R.string.land_preview_action_open_navigation_menu
                        )
                    )
                }
                DropdownMenu(
                    expanded = showNavigationMenu,
                    onDismissRequest = { showNavigationMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = R.string.land_preview_action_navigate_to_land_zones
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null
                            )
                        },
                        onClick = toLandZonesPress
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = R.string.land_preview_action_navigate_to_land_notes
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null
                            )
                        },
                        onClick = toLandNotesPress
                    )
                }
            }
        }
    )
}

@Composable
private fun LandPreviewScreenContent(
    uiState: LandPreviewStates.Loaded,
    showDialog: Boolean,
    onSubmitDialog: () -> Unit,
    onDismissDialog:() -> Unit
){

    if(showDialog){
        MessageDialog(
            title = stringResource(id = R.string.land_preview_delete_dialog_label),
            message = stringResource(
                id = R.string.land_preview_delete_dialog_text,
                uiState.land.id,
                uiState.land.title
            ),
            submitText = stringResource(id = R.string.delete_label),
            cancelText = stringResource(id = R.string.cancel_label),
            titleColor = MaterialTheme.colorScheme.error,
            submitColor = MaterialTheme.colorScheme.error,
            onSubmit = onSubmitDialog,
            onDismiss = onDismissDialog
        )
    }

    val bounds = remember(uiState) {
        uiState.land.border.toLatLngBounds()
    }?:return

    val coroutineScope = rememberCoroutineScope()
    var mapStyle by remember {
        mutableStateOf(MapType.NONE)
    }
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(DefaultMapTarget, DefaultMapZoom)
    }

    val uiSettings = remember {
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
    val properties = remember(mapStyle){
        MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isMyLocationEnabled = false,
            isTrafficEnabled = false,
            mapStyleOptions = null,
            mapType = mapStyle,
        )
    }

    val zoomPadding = with(LocalDensity.current){32.dp.toPx()}.toInt()
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties,
        onMapLoaded = {
            coroutineScope.launch {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        zoomPadding
                    )
                )
                if (mapStyle != MapType.SATELLITE) {
                    mapStyle = MapType.SATELLITE
                }
            }
        }
    ) {
        Polygon(
            points = uiState.land.border,
            holes = uiState.land.holes,
            strokeColor = uiState.land.color.copy(alpha = DefaultMapItemStrokeAlpha),
            fillColor = uiState.land.color.copy(alpha = DefaultMapItemFillAlpha),
            zIndex = 1f
        )
    }
}



@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandPreviewScreenLoading(){
    LandPreviewScreen(
        uiState = LandPreviewStates.LoadingState
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandPreviewScreenLoaded(){
    LandPreviewScreen(
        uiState = LandPreviewStates.Loaded(
            land = Land.emptyLand().copy(
                id = 1L,
                title = "Land Title",
                border = listOf(
                    LatLng( 0.0 , 0.0)
                )
            )
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandPreviewScreenLoadedDeleteDialog(){
    LandPreviewScreen(
        uiState = LandPreviewStates.Loaded(
            land = Land.emptyLand().copy(
                id = 1L,
                title = "Land Title",
                border = listOf(
                    LatLng( 0.0 , 0.0)
                )
            )
        ),
        initialValueShowDeleteDialog = true
    )
}