package com.simosc.landworkscheduler.presentation.ui.screens.menulandnotes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.core.config.DefaultMapTarget
import com.simosc.landworkscheduler.core.config.DefaultMapZoom
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar
import com.simosc.landworkscheduler.presentation.ui.components.topbar.SearchTopAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun LandNotesMenuScreen(
    uiState: LandNotesMenuStates,
    searchQuery: String = "",
    isSearchLoading: Boolean = false,
    isSearchOpenInitValue: Boolean = false,
    onBackPress: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onNoteClick: (Note) -> Unit = {},
    onNewNoteClick: (Land) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isSearchOpen by remember(isSearchOpenInitValue){
        mutableStateOf(isSearchOpenInitValue)
    }
    LaunchedEffect(uiState){
        if (uiState is LandNotesMenuStates.CantInit) {
            onBackPress()
        }
    }
    BackHandler(isSearchOpen){
        isSearchOpen = false
    }
    BackHandler(!isSearchOpen && searchQuery.isNotBlank()){
        onSearchChange("")
    }
    Scaffold(
        topBar = {
            LandNotesMenuTopBar(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onBackPress = onBackPress,
                isSearchOpen = isSearchOpen,
                onIsSearchOpenChange = { newIsSearchOpen ->
                    isSearchOpen = newIsSearchOpen
                },
            )
        },
        floatingActionButton = {
            if (uiState is LandNotesMenuStates.LoadedState) {
                FloatingActionButton(
                    onClick = {
                        onNewNoteClick(uiState.land)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Create New Land Note",
                    )
                }
            }
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when{
                    uiState is LandNotesMenuStates.LoadedState && isSearchLoading ->
                        LoadingContentComponent(text = "Searching..")

                    uiState is LandNotesMenuStates.LoadedState ->
                        LandNotesMenuScreenContent(
                            uiState = uiState,
                            searchQuery = searchQuery,
                            coroutineScope = coroutineScope,
                            onNoteClick = onNoteClick,
                        )

                    else ->
                        LoadingContentComponent()
                }
            }
        }
    )
}

@Composable
private fun LandNotesMenuTopBar(
    uiState: LandNotesMenuStates,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBackPress: () -> Unit,
    isSearchOpen: Boolean,
    onIsSearchOpenChange: (Boolean) -> Unit,
) {
    if(isSearchOpen && uiState is LandNotesMenuStates.LoadedState){
        SearchTopAppBar(
            searchQuery = searchQuery,
            onSearchChange = onSearchChange,
            onCloseSearchBar = {
                onIsSearchOpenChange(false)
            }
        )
    }else{
        DefaultTopAppBar(
            title = when {
                searchQuery.isNotBlank() ->
                    "Search: $searchQuery"

                uiState is LandNotesMenuStates.LoadedState ->
                    "Land Notes: #${uiState.land.id} ${uiState.land.title}"

                else ->
                    "Land Notes: Loading..."
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if(searchQuery.isNotBlank())
                            onSearchChange("")
                        else
                            onBackPress()
                    }
                ) {
                    Icon(
                        imageVector = if(searchQuery.isNotBlank())
                            Icons.Rounded.Close
                        else
                            Icons.Rounded.ArrowBack,
                        contentDescription = "Navigate Back"
                    )
                }
            },
            actions = {
                if(uiState is LandNotesMenuStates.LoadedState){
                    IconButton(
                        onClick = {
                            onIsSearchOpenChange(true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Open Search"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun LandNotesMenuScreenContent(
    uiState: LandNotesMenuStates.LoadedState,
    searchQuery: String,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onNoteClick: (Note) -> Unit
) {
    if(uiState.land.border.isEmpty()) return
    val bounds = remember(uiState.land){
        uiState.land.border.toLatLngBounds()
    }?: return

    if(uiState.notes.isNotEmpty())
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ){
            items(
                items = uiState.notes,
                key = {note -> note.id}
            ){ note ->
                LandNoteCard(
                    land = uiState.land,
                    note = note,
                    coroutineScope = coroutineScope,
                    bounds = bounds,
                    onNoteClick = onNoteClick
                )
            }
        }
    else if(searchQuery.isNotBlank())
        Text(
            text = "No Results",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )
    else
        Text(
            text = "Add new note",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandNoteCard(
    land: Land,
    note: Note,
    bounds: LatLngBounds,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    mapSize: Dp = 256.dp,
    onNoteClick: (Note) -> Unit
) {

    val cameraPositionState = rememberCameraPositionState(
        key = "#${note.id} ${note.title}"
    ){
        position = CameraPosition.fromLatLngZoom(DefaultMapTarget, DefaultMapZoom)
    }
    var mapType by remember(note){
        mutableStateOf(MapType.NONE)
    }

    val uiSettings = remember{
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
    val properties = remember(mapType){
        MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isMyLocationEnabled = false,
            isTrafficEnabled = false,
            mapStyleOptions = null,
            mapType = mapType,
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            ),
        onClick = { onNoteClick(note) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "#${note.id} ${note.title}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent
            ){
                Box(
                    contentAlignment = Alignment.Center
                ){
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mapSize),
                        cameraPositionState = cameraPositionState,
                        uiSettings = uiSettings,
                        properties = properties,
                        onMapClick = {
                            onNoteClick(note)
                        },
                        onMapLoaded = {
                            coroutineScope.launch {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        32
                                    )
                                )
                                mapType = MapType.SATELLITE
                            }
                        }
                    ) {
                        if(land.border.isNotEmpty()) {
                            Polygon(
                                points = land.border,
                                holes = land.holes,
                                strokeColor = land.color.copy(alpha = DefaultMapItemStrokeAlpha),
                                fillColor = land.color.copy(alpha = DefaultMapItemFillAlpha),
                                zIndex = 1f
                            )
                        }

                        Circle(
                            center = note.center,
                            radius = note.radius,
                            strokeColor = note.color,
                            fillColor = note.color.copy(alpha = 0.15f),
                            zIndex = 2f
                        )
                    }
                    if(mapType == MapType.NONE){
                        Text(text = "Loading Map...")
                    }
                }
            }
        }
    }
}


@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandNotesMenuScreenLoadingState(){
    LandNotesMenuScreen(
        uiState = LandNotesMenuStates.LoadingState
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandNotesMenuScreenNoSearchState(){
    val land = Land.emptyLand().copy(
        title = "mock land",
        border = listOf(
            LatLng(
                0.0,
                0.0
            )
        )
    )
    val notes = List(10){index ->
        Note.emptyNote(
            lid = land.id,
            center = LatLng(
                0.0,
                0.0
            )
        ).copy(
            id = index + 1L,
            title = "mock note ${index + 1}"
        )
    }
    LandNotesMenuScreen(
        uiState = LandNotesMenuStates.LoadedState(
            land = land,
            notes = notes
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandNotesMenuScreenSearchState(){
    val land = Land.emptyLand().copy(
        title = "mock land",
        border = listOf(
            LatLng(
                0.0,
                0.0
            )
        )
    )
    val notes = List(10){index ->
        Note.emptyNote(
            lid = land.id,
            center = LatLng(
                0.0,
                0.0
            )
        ).copy(
            id = index + 1L,
            title = "mock note ${index + 1}"
        )
    }
    LandNotesMenuScreen(
        uiState = LandNotesMenuStates.LoadedState(
            land = land,
            notes = notes
        ),
        searchQuery = "kati",
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandNotesMenuScreenNoSearchStateSearchLoading(){
    val land = Land.emptyLand().copy(
        title = "mock land",
        border = listOf(
            LatLng(
                0.0,
                0.0
            )
        )
    )
    val notes = List(10){index ->
        Note.emptyNote(
            lid = land.id,
            center = LatLng(
                0.0,
                0.0
            )
        ).copy(
            id = index + 1L,
            title = "mock note ${index + 1}"
        )
    }
    LandNotesMenuScreen(
        uiState = LandNotesMenuStates.LoadedState(
            land = land,
            notes = notes
        ),
        isSearchOpenInitValue = true,
        isSearchLoading = true
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandNotesMenuScreenNoSearchStateSearching(){
    val land = Land.emptyLand().copy(
        title = "mock land",
        border = listOf(
            LatLng(
                0.0,
                0.0
            )
        )
    )
    val notes = List(10){index ->
        Note.emptyNote(
            lid = land.id,
            center = LatLng(
                0.0,
                0.0
            )
        ).copy(
            id = index + 1L,
            title = "mock note ${index + 1}"
        )
    }
    LandNotesMenuScreen(
        uiState = LandNotesMenuStates.LoadedState(
            land = land,
            notes = notes
        ),
        isSearchOpenInitValue = true
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandNotesMenuScreenSearchStateSearching(){
    val land = Land.emptyLand().copy(
        title = "mock land",
        border = listOf(
            LatLng(
                0.0,
                0.0
            )
        )
    )
    val notes = List(10){index ->
        Note.emptyNote(
            lid = land.id,
            center = LatLng(
                0.0,
                0.0
            )
        ).copy(
            id = index + 1L,
            title = "mock note ${index + 1}"
        )
    }
    LandNotesMenuScreen(
        uiState = LandNotesMenuStates.LoadedState(
            land = land,
            notes = notes
        ),
        searchQuery = "kati",
        isSearchOpenInitValue = true
    )
}
