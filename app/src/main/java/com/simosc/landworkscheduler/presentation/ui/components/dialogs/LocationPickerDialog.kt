package com.simosc.landworkscheduler.presentation.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.domain.extension.isInside
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land


@Preview
@Composable
private fun PreviewDialogLocationPickerWithPointLoading(){
    SingleLocationPickerDialog(
        initialCameraTarget = LatLng(0.0,0.0),
        onDismissRequest = {},
        onSubmitRequest = {}
    )
}

@Preview
@Composable
private fun PreviewDialogLocationPickerWithPointLoaded(){
    SingleLocationPickerDialog(
        initialCameraTarget = LatLng(0.0,0.0),
        initMapLoaded = true,
        onDismissRequest = {},
        onSubmitRequest = {}
    )
}

@Preview
@Composable
private fun PreviewDialogLocationPickerWithBoundLoading(){
    SingleLocationPickerDialog(
        landPoints = listOf(
            LatLng(1.0, 1.0),
            LatLng(1.0, -1.0),
            LatLng(-1.0, -1.0),
            LatLng(-1.0, 1.0)
        ),
        onDismissRequest = {},
        onSubmitRequest = {}
    )
}

@Preview
@Composable
private fun PreviewDialogLocationPickerWithBoundLoaded(){
    SingleLocationPickerDialog(
        landPoints = listOf(
            LatLng(1.0, 1.0),
            LatLng(1.0, -1.0),
            LatLng(-1.0, -1.0),
            LatLng(-1.0, 1.0)
        ),
        initMapLoaded = true,
        onDismissRequest = {},
        onSubmitRequest = {}
    )
}

@Preview
@Composable
private fun PreviewDialogLocationPickerWithBoundEmptyLoading(){
    SingleLocationPickerDialog(
        initMapLoaded = false,
        onDismissRequest = {},
        onSubmitRequest = {}
    )
}

@Preview
@Composable
private fun PreviewDialogLocationPickerWithBoundEmptyLoaded(){
    SingleLocationPickerDialog(
        initMapLoaded = true,
        onDismissRequest = {},
        onSubmitRequest = {}
    )
}

@Composable
fun SingleLocationPickerDialog(
    initialCameraTarget: LatLng,
    initialCameraZoom: Float = 10f,
    initMapLoaded: Boolean = false,
    mapSize: Dp = 256.dp,
    mapType: MapType = MapType.HYBRID,
    dialogProperties: DialogProperties = DialogProperties(),
    submitButtonText: String = "Submit",
    onDismissRequest: () -> Unit,
    onSubmitRequest: (LatLng) -> Unit,
){
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCameraTarget, initialCameraZoom)
    }
    var mapLoaded by remember(initMapLoaded){
        mutableStateOf(initMapLoaded)
    }
    val properties = remember(mapType){
        MapProperties(
            mapType = mapType
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogProperties
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapSize),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = properties,
                            onMapLoaded = {mapLoaded = true}
                        )
                        if(mapLoaded){
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset((-2).dp, (-16).dp),
                                tint = Color.Black.copy(alpha = 0.5f)
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(0.dp, (-14).dp),
                                tint = Color.Red
                            )
                        }else{
                            Text(text = "Map is loading...")
                        }
                    }
                    IconButton(
                        modifier = Modifier.wrapContentSize(align = Alignment.TopEnd),
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if(mapLoaded) {
                                onSubmitRequest(
                                    cameraPositionState.position.target
                                )
                            }
                        },
                    ) {
                        Text(
                            text = submitButtonText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SingleLocationPickerDialog(
    initMapLoaded: Boolean = false,
    landPoints: List<LatLng> = emptyList(),
    landColor: Color = Land.emptyLand().color,
    mapSize: Dp = 256.dp,
    selectedMapType: MapType = MapType.HYBRID,
    dialogProperties: DialogProperties = DialogProperties(),
    submitButtonText: String = "Submit",
    onDismissRequest: () -> Unit,
    onSubmitRequest: (LatLng) -> Unit,
){
    val cameraPositionState = rememberCameraPositionState()

    var mapType by remember {
        mutableStateOf(MapType.NONE)
    }

    var mapLoaded by remember(initMapLoaded){
        mutableStateOf(initMapLoaded)
    }

    val bounds = remember(landPoints){
        landPoints.toLatLngBounds()
    }

    val properties = remember(mapType){
        MapProperties(
            mapType = mapType
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogProperties
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapSize),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = properties,
                            onMapLoaded = {
                                if(bounds != null) {
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngBounds(
                                            bounds,
                                            64
                                        )
                                    )
                                }
                                if (mapType != selectedMapType) {
                                    mapType = selectedMapType
                                }
                                mapLoaded = true
                            }
                        ) {
                            if (landPoints.isNotEmpty()) {
                                Polygon(
                                    points = landPoints,
                                    strokeColor = landColor.copy(alpha = DefaultMapItemStrokeAlpha),
                                    fillColor = landColor.copy(alpha = DefaultMapItemFillAlpha)
                                )
                            }
                        }
                        if (mapLoaded) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset((-2).dp, (-16).dp),
                                tint = Color.Black.copy(alpha = 0.5f)
                            )
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(0.dp, (-14).dp),
                                tint = Color.Red
                            )
                        } else {
                            Text(text = "Map is loading...")
                        }
                    }
                    IconButton(
                        modifier = Modifier.wrapContentSize(
                            align = Alignment.TopEnd
                        ),
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if(mapLoaded){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                cameraPositionState.position.target.let{ point ->
                                    if(landPoints.isEmpty() || point.isInside(landPoints)) {
                                        onSubmitRequest(point)
                                    }
                                }
                            },
                        ) {
                            Text(
                                text = submitButtonText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}