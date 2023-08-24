package com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.core.config.DefaultMapZoom
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun KmlReaderScreenContent(
    modifier: Modifier = Modifier,
    uiState: KmlLandReaderStates,
    onOpenDocuments: () -> Unit = {},
    onLandClick: (Land) -> Unit = {}
){
    Surface(
        modifier = modifier
    ){
        uiState.let{ state ->
            when(state){

                is KmlLandReaderStates.LoadedLands -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ){
                        items(
                            count = state.lands.size
                        ){ index ->
                            state.lands[index].let{ land ->
                                LandItem(
                                    land = land,
                                    showDivider = land != state.lands.last(),
                                    isSelected = state is KmlLandReaderStates.LandSelected && state.selectedLand == land,
                                    onClick = { onLandClick(land) }
                                )
                            }
                        }
                    }
                }

                is KmlLandReaderStates.ErrorParsing -> {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        textAlign = TextAlign.Center,
                        text = stringResource(
                            id = R.string.kml_lands_reader_error_file_cant_parse
                        )
                    )
                }

                is KmlLandReaderStates.NoLandsFound -> {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        textAlign = TextAlign.Center,
                        text = stringResource(
                            id = R.string.kml_lands_reader_error_no_lands_found_in_file
                        )
                    )
                }

                is KmlLandReaderStates.LoadingFile -> {
                    LoadingContentComponent(
                        text = stringResource(
                            id = R.string.kml_lands_reader_content_reading_the_file
                        )
                    )
                }

                is KmlLandReaderStates.WaitingFile -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            text = stringResource(
                                id = R.string.kml_lands_reader_content_select_a_file
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onOpenDocuments()
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.kml_lands_reader_button_select_a_file
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandItem(
    land: Land,
    isSelected: Boolean,
    showDivider: Boolean,
    scope: CoroutineScope = rememberCoroutineScope(),
    onClick: () -> Unit,
){
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(
            DefaultMapTarget,
            DefaultMapZoom
        )
    }
    var mapProperties by remember{
        mutableStateOf(
            MapProperties(
                isBuildingEnabled = false,
                isIndoorEnabled = false,
                isMyLocationEnabled = false,
                isTrafficEnabled = false,
                mapType = MapType.NONE
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onClick()
        }
    ){
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(
                        id = R.string.land_menu_list_item_land_title,
                        land.title
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Surface(
                    modifier = Modifier.size(
                        width = 168.dp,
                        height = 94.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                ){
                    GoogleMap(
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
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
                        properties = mapProperties,
                        onMapClick = {
                            onClick()
                        },
                        onMapLoaded = {
                            if(land.border.isNotEmpty()){
                                scope.launch {
                                    land.border.toLatLngBounds()?.run{
                                        cameraPositionState.move(
                                            CameraUpdateFactory.newLatLngBounds(
                                                this,
                                                32
                                            )
                                        )
                                    }
                                    if(mapProperties.mapType == MapType.NONE) {
                                        mapProperties = mapProperties.copy(
                                            mapType = MapType.SATELLITE
                                        )
                                    }
                                }
                            }
                        }
                    ){
                        if(land.border.isNotEmpty()){
                            Polygon(
                                points = land.border,
                                holes = land.holes,
                                strokeColor = land.color.copy(alpha = DefaultMapItemStrokeAlpha),
                                fillColor = land.color.copy(alpha = DefaultMapItemFillAlpha),
                            )
                        }
                    }
                }
            },
            trailingContent = {
                RadioButton(
                    selected = isSelected,
                    onClick = {
                        onClick()
                    }
                )
            }
        )
    }
    if(showDivider) {
        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .05f),
            thickness = 2.dp
        )
    }
}


@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewKmlLandReaderScreen_LoadingFile(){
    KmlReaderScreenContent(
        uiState = KmlLandReaderStates.LoadingFile
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewKmlLandReaderScreen_ErrorParsing(){
    KmlReaderScreenContent(
        uiState = KmlLandReaderStates.ErrorParsing
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewKmlLandReaderScreen_NoLandsFound(){
    KmlReaderScreenContent(
        uiState = KmlLandReaderStates.NoLandsFound
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewKmlLandReaderScreen_NoLandSelected(){
    val lands = List(10){ index ->
        Land.emptyLand().copy(
            title = "${index + 1} Land",
            border = listOf(
                LatLng(0.0,0.0),
                LatLng(1.0,0.0),
                LatLng(1.0,1.0),
                LatLng(0.0,1.0),
            )
        )
    }
    KmlReaderScreenContent(
        uiState = KmlLandReaderStates.NoLandSelected(
            lands = lands
        )
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewKmlLandReaderScreen_LandSelected(){
    val lands = List(10){ index ->
        Land.emptyLand().copy(
            title = "Land ${index + 1}",
            border = listOf(
                LatLng(0.0,0.0),
                LatLng(1.0,0.0),
                LatLng(1.0,1.0),
                LatLng(0.0,1.0),
            )
        )
    }
    KmlReaderScreenContent(
        uiState = KmlLandReaderStates.LandSelected(
            lands = lands,
            selectedLand = lands.first()
        )
    )
}
