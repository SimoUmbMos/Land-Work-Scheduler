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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
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


@Composable
fun LandsMenuScreen(
    uiState: LandsMenuStates,
    searchQuery: String = "",
    isSearching: Boolean = false,
    isLoadingAction: Boolean = false,
    onBackPress: () -> Unit = {},
    onLandPress: (Land) -> Unit = {},
    onNewLandPress: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onActionChange: (LandsMenuActions) -> Unit = {},
    onDeleteSelectedLands: () -> Unit = {},
    onExportSelectedLands: () -> Unit = {},
    initSearchAppBarOpen: Boolean = false,
) {
    var isSearchAppBarOpen by remember(initSearchAppBarOpen){
        mutableStateOf(initSearchAppBarOpen)
    }
    BackHandler(isSearchAppBarOpen){
        isSearchAppBarOpen = false
    }
    BackHandler(searchQuery.isNotBlank()){
        onSearchChange("")
    }
    BackHandler(uiState is LandsMenuStates.MultiSelectLands){
        onActionChange(LandsMenuActions.None)
    }
    Scaffold(
        topBar = {
            LandMenuTopBar(
                uiState = uiState,
                isSearchAppBarOpen = isSearchAppBarOpen,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onSearchAppBarChange = { isSearchAppBarOpen = it},
                onBackPress = {
                    when{
                        isSearchAppBarOpen ->
                            isSearchAppBarOpen = false

                        searchQuery.isNotBlank() ->
                            onSearchChange("")

                        uiState is LandsMenuStates.MultiSelectLands ->
                            onActionChange(LandsMenuActions.None)

                        else ->
                            onBackPress()
                    }
                },
                onNewLandPress = onNewLandPress,
                onActionChange = onActionChange,
                onExportSelectedLands = onExportSelectedLands,
                onDeleteSelectedLands = onDeleteSelectedLands,
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
                                        text = if(searchQuery.isNotBlank())
                                            "Can't find result's"
                                        else
                                            "Add new land",
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
                if(isSearching || isLoadingAction){
                    //TODO: Loading dialog
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandMenuTopBar(
    uiState: LandsMenuStates,
    searchQuery: String,
    isSearchAppBarOpen: Boolean,
    onBackPress: () -> Unit,
    onNewLandPress: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSearchAppBarChange: (Boolean) -> Unit,
    onActionChange: (LandsMenuActions) -> Unit,
    onDeleteSelectedLands: () -> Unit,
    onExportSelectedLands: () -> Unit,
){
    when{
        isSearchAppBarOpen -> {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp,
                    ),
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(text = "Search...")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchAppBarChange(false)
                    }
                ),
                leadingIcon = {
                    IconButton(
                        onClick = {
                            if(searchQuery.isNotBlank())
                                onSearchChange("")
                            else
                                onSearchAppBarChange(false)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear / Close Search"
                        )
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onSearchAppBarChange(false)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                shape = RoundedCornerShape(32.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTextColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,

                    focusedContainerColor =
                    MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor =
                    MaterialTheme.colorScheme.primaryContainer,

                    focusedIndicatorColor =
                    Color.Transparent,
                    unfocusedIndicatorColor =
                    Color.Transparent,

                    focusedLeadingIconColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedLeadingIconColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,

                    focusedTrailingIconColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTrailingIconColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,

                    focusedPlaceholderColor =
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor =
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                )
            )
        }

        searchQuery.isNotBlank() -> {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Search: $searchQuery",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Navigate Back Button"
                        )
                    }
                },
                actions = {
                    when(uiState){
                        is LandsMenuStates.Loaded -> {
                            IconButton(
                                onClick = { onSearchAppBarChange(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                )
                            }
                            if(uiState.lands.isNotEmpty()) {
                                IconButton(
                                    onClick = { onActionChange(LandsMenuActions.Export) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Export Lands",
                                    )
                                }
                                IconButton(
                                    onClick = { onActionChange(LandsMenuActions.Delete) }
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
                                onClick = { onSearchAppBarChange(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                )
                            }
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
                                onClick = { onSearchAppBarChange(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                )
                            }
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
        }

        else -> {
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
                        onClick = onBackPress
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
                            IconButton(
                                onClick = { onSearchAppBarChange(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                )
                            }
                            if(uiState.lands.isNotEmpty()) {
                                IconButton(
                                    onClick = { onActionChange(LandsMenuActions.Export) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Export Lands",
                                    )
                                }
                                IconButton(
                                    onClick = { onActionChange(LandsMenuActions.Delete) }
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
                                onClick = { onSearchAppBarChange(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                )
                            }
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
                                onClick = { onSearchAppBarChange(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                )
                            }
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
        }
    }
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
            overlineContent = {
                Text(
                    text = "#${land.id}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            headlineContent = {
                Text(
                    text = land.title,
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
                        uiSettings = mapUiSettings,
                        properties = mapProperties,
                        onMapLoaded = {
                            coroutineScope.launch {
                                land.border.toLatLngBounds()?.let{ bounds ->
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngBounds(
                                            bounds,
                                            32
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
