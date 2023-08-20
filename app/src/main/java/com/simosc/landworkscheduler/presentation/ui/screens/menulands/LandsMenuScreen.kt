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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.domain.extension.toLatLngBounds
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar
import com.simosc.landworkscheduler.presentation.ui.components.topbar.SearchTopAppBar
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
    errorMessage: String? = null,
) {
    var isSearchAppBarOpen by remember(initSearchAppBarOpen){
        mutableStateOf(initSearchAppBarOpen)
    }
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler(isLoadingAction){
        scope.launch {
            snackbarHostState.showSnackbar(
                ctx.getString(R.string.land_menu_error_wait_action_to_finish)
            )
        }
    }
    BackHandler(isSearchAppBarOpen){
        if(!isLoadingAction) {
            isSearchAppBarOpen = false
        }
    }
    BackHandler(searchQuery.isNotBlank()){
        if(!isLoadingAction) {
            onSearchChange("")
        }
    }
    BackHandler(uiState is LandsMenuStates.MultiSelectLands){
        if(!isLoadingAction) {
            onActionChange(LandsMenuActions.None)
        }
    }
    Scaffold(
        topBar = {
            LandMenuTopBar(
                uiState = uiState,
                isSearchAppBarOpen = isSearchAppBarOpen,
                isLoadingAction = isLoadingAction,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onSearchAppBarChange = { isSearchAppBarOpen = it},
                onBackPress = {
                    when{
                        isLoadingAction ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    ctx.getString(R.string.land_menu_error_wait_action_to_finish)
                                )
                            }

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
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if(isLoadingAction){
                    LoadingContentComponent(
                        text = when (uiState) {
                            is LandsMenuStates.DeleteLands ->
                                stringResource(
                                    R.string.land_menu_content_is_exporting_lands
                                )

                            is LandsMenuStates.ExportLands ->
                                stringResource(
                                    R.string.land_menu_content_is_deleting_lands
                                )

                            else ->
                                stringResource(
                                    R.string.land_menu_content_is_action_executing
                                )
                        }
                    )
                }else {
                    when (uiState) {
                        is LandsMenuStates.Loading -> {
                            LoadingContentComponent()
                        }

                        is LandsMenuStates.Loaded,
                        is LandsMenuStates.MultiSelectLands -> {
                            if (isSearching) {
                                LoadingContentComponent(
                                    text = stringResource(
                                        R.string.land_menu_content_is_searching_lands
                                    )
                                )
                            } else {
                                val lands = remember(uiState) {
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
                                                text = if (searchQuery.isNotBlank())
                                                    stringResource(
                                                        id = R.string.land_menu_content_cant_find_any_land
                                                    )
                                                else
                                                    stringResource(
                                                        id = R.string.land_menu_content_empty_list
                                                    ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillParentMaxSize()
                                                    .wrapContentHeight()
                                            )
                                        }
                                    } else {
                                        items(
                                            items = lands,
                                            key = { it.id }
                                        ) { land ->
                                            val isLast = lands.lastOrNull() != land
                                            LandItem(
                                                land = land,
                                                showDivider = isLast,
                                                showCheckBox = uiState is LandsMenuStates.MultiSelectLands,
                                                isChecked = if (uiState is LandsMenuStates.MultiSelectLands)
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
                LaunchedEffect(errorMessage){
                    if(!errorMessage.isNullOrBlank()){
                        snackbarHostState.showSnackbar(
                            message = errorMessage
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun LandMenuTopBar(
    uiState: LandsMenuStates,
    searchQuery: String,
    isSearchAppBarOpen: Boolean,
    isLoadingAction: Boolean,
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
            SearchTopAppBar(
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onCloseSearchBar = {
                    onSearchAppBarChange(false)
                }
            )
        }

        searchQuery.isNotBlank() -> {
            DefaultTopAppBar(
                title = if(isLoadingAction) {
                    stringResource(
                        id = R.string.land_menu_title_default
                    )
                }else{
                    when(uiState) {
                        is LandsMenuStates.DeleteLands -> stringResource(
                            id = R.string.land_menu_title_delete_lands
                        )
                        is LandsMenuStates.ExportLands -> stringResource(
                            id = R.string.land_menu_title_export_lands
                        )
                        else -> stringResource(
                            id = R.string.land_menu_title_default
                        )
                    }
                },
                subTitle = stringResource(
                    id = R.string.land_menu_subtitles_search,
                    searchQuery
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.navigate_back_label)
                        )
                    }
                },
                actions = {
                    if(!isLoadingAction) {
                        when (uiState) {

                            is LandsMenuStates.Loaded -> {
                                IconButton(
                                    onClick = { onSearchAppBarChange(true) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            id = R.string.search_label
                                        ),
                                    )
                                }
                                if (uiState.lands.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onActionChange(LandsMenuActions.Export) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = stringResource(
                                                id = R.string.land_menu_action_export_lands
                                            ),
                                        )
                                    }
                                    IconButton(
                                        onClick = { onActionChange(LandsMenuActions.Delete) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(
                                                id = R.string.land_menu_action_delete_lands
                                            ),
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onNewLandPress
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(
                                            id = R.string.land_menu_action_create_land
                                        ),
                                    )
                                }
                            }

                            is LandsMenuStates.ExportLands -> {
                                IconButton(
                                    onClick = { onSearchAppBarChange(true) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            id = R.string.search_label
                                        ),
                                    )
                                }
                                IconButton(
                                    onClick = onExportSelectedLands
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = stringResource(
                                            id = R.string.land_menu_action_export_lands_execute
                                        ),
                                    )
                                }
                            }

                            is LandsMenuStates.DeleteLands -> {
                                IconButton(
                                    onClick = { onSearchAppBarChange(true) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            id = R.string.search_label
                                        ),
                                    )
                                }
                                IconButton(
                                    onClick = onDeleteSelectedLands
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(
                                            id = R.string.land_menu_action_delete_lands_execute
                                        ),
                                    )
                                }
                            }

                            else -> {}
                        }
                    }
                }
            )
        }

        else -> {
            DefaultTopAppBar(
                title =  if(isLoadingAction) {
                    stringResource(
                        id = R.string.land_menu_title_default
                    )
                }else{
                    when(uiState) {
                        is LandsMenuStates.DeleteLands -> stringResource(
                            id = R.string.land_menu_title_delete_lands
                        )
                        is LandsMenuStates.ExportLands -> stringResource(
                            id = R.string.land_menu_title_export_lands
                        )
                        else -> stringResource(
                            id = R.string.land_menu_title_default
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(
                            imageVector = if(uiState is LandsMenuStates.MultiSelectLands)
                                Icons.Default.Close
                            else
                                Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back_label)
                        )
                    }
                },
                actions = {
                    if(!isLoadingAction) {
                        when (uiState) {

                            is LandsMenuStates.Loaded -> {
                                IconButton(
                                    onClick = { onSearchAppBarChange(true) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            id = R.string.search_label
                                        ),
                                    )
                                }
                                if (uiState.lands.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onActionChange(LandsMenuActions.Export) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = stringResource(
                                                id = R.string.land_menu_action_export_lands
                                            ),
                                        )
                                    }
                                    IconButton(
                                        onClick = { onActionChange(LandsMenuActions.Delete) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(
                                                id = R.string.land_menu_action_delete_lands
                                            ),
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onNewLandPress
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(
                                            id = R.string.land_menu_action_create_land
                                        ),
                                    )
                                }
                            }

                            is LandsMenuStates.ExportLands -> {
                                IconButton(
                                    onClick = { onSearchAppBarChange(true) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            id = R.string.search_label
                                        ),
                                    )
                                }
                                IconButton(
                                    onClick = onExportSelectedLands
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = stringResource(
                                            id = R.string.land_menu_action_export_lands_execute
                                        ),
                                    )
                                }
                            }

                            is LandsMenuStates.DeleteLands -> {
                                IconButton(
                                    onClick = { onSearchAppBarChange(true) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(
                                            id = R.string.search_label
                                        ),
                                    )
                                }
                                IconButton(
                                    onClick = onDeleteSelectedLands
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(
                                            id = R.string.land_menu_action_delete_lands_execute
                                        ),
                                    )
                                }
                            }

                            else -> {}
                        }
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
                    text = stringResource(
                        id = R.string.land_menu_list_item_land_id,
                        land.id
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            headlineContent = {
                Text(
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
                            text = stringResource(
                                id = R.string.land_menu_list_item_land_border_empty
                            )
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
            thickness = 2.dp
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

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchLoading(){
    LandsMenuScreen(
        uiState = LandsMenuStates.Loading,
        searchQuery = "Search Query",
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchLoadedEmpty(){
    LandsMenuScreen(
        searchQuery = "Search Query",
        uiState = LandsMenuStates.Loaded(
            lands = emptyList()
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchLoaded(){
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
        searchQuery = "Search Query",
        uiState = LandsMenuStates.Loaded(
            lands = mockLands
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchExport(){
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
        searchQuery = "Search Query",
        uiState = LandsMenuStates.ExportLands(
            lands = mockLands
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchDelete(){
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
        searchQuery = "Search Query",
        uiState = LandsMenuStates.DeleteLands(
            lands = mockLands
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuIsSearching(){
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
        searchQuery = "Search Query",
        isSearching = true,
        uiState = LandsMenuStates.Loaded(
            lands = mockLands
        )
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenExportActionRunning(){
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
        ),
        isLoadingAction = true,
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenDeleteActionRunning(){
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
        ),
        isLoadingAction = true
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenLoadingActionRunning(){
    LandsMenuScreen(
        uiState = LandsMenuStates.Loading,
        isLoadingAction = true
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenLoadedActionRunning(){
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
        ),
        isLoadingAction = true
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchExportActionRunning(){
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
        ),
        isLoadingAction = true,
        searchQuery = "Search Query",
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchDeleteActionRunning(){
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
        ),
        isLoadingAction = true,
        searchQuery = "Search Query",
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchLoadingActionRunning(){
    LandsMenuScreen(
        uiState = LandsMenuStates.Loading,
        isLoadingAction = true,
        searchQuery = "Search Query",
    )
}

@Composable
@Preview( showSystemUi = true, showBackground = true )
private fun PreviewLandsMenuScreenSearchLoadedActionRunning(){
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
        ),
        isLoadingAction = true,
        searchQuery = "Search Query",
    )
}
