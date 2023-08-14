package com.simosc.landworkscheduler.presentation.ui.screens.editorland

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.core.config.DefaultSelectedPointFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultUnselectedPointFillAlpha
import com.simosc.landworkscheduler.domain.extension.calcRadiusFromZoom
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.ColorPickerDialog
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.DualTextEditorDialog
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.MessageDialog
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.TextEditorDialog


@Composable
fun LandEditorScreen(
    uiState: LandEditorStates,
    error: String? = null,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onBackPress: () -> Unit = {},
    onActionUpdate: (LandEditorActions) -> Unit = {},
    onSubmitAction: () -> Unit = {},
    onCancelAction: () -> Unit = {},
    onResetAction: () -> Unit = {},
    onSaveAction: () -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    onAddressUpdate: (String, String) -> Unit = {_,_ -> },
    onUpdateColor: (Color) -> Unit = {},
    onUpdateTitle: (String) -> Unit = {},
    onClearError: () -> Unit ={},
    showNeedSave: Boolean = false,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showNeedSaveDialog by remember(showNeedSave){ mutableStateOf(showNeedSave) }

    when(uiState){
        is LandEditorStates.EditState ->
            BackHandler(true){
                onCancelAction()
            }
        is LandEditorStates.NormalState ->
            BackHandler(uiState.needSave()){
                showNeedSaveDialog = true
            }
        else -> {}
    }

    Scaffold(
        topBar = {
            LandEditorTopBar(
                uiState = uiState,
                onBackPress = {
                    when(uiState){
                        is LandEditorStates.EditState -> onCancelAction()
                        is LandEditorStates.NormalState ->
                            if(uiState.needSave()) showNeedSaveDialog = true
                            else onBackPress()
                        else -> onBackPress()
                    }
                },
                onActionUpdate = onActionUpdate,
                onResetAction = onResetAction,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            LandEditorFab(
                uiState = uiState,
                onSubmitAction = onSubmitAction,
                onSaveAction = onSaveAction
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ){

                when(uiState){
                    is LandEditorStates.NormalState,
                    is LandEditorStates.EditState -> LandEditorMap(
                        uiState = uiState,
                        cameraPositionState = cameraPositionState,
                        onMapClick = onMapClick
                    )

                    is LandEditorStates.NeedLocation -> LoadingContentComponent(
                        text = "Waiting Address..."
                    )

                    else -> LoadingContentComponent()
                }

                ShowDialogs(
                    uiState = uiState,
                    showNeedSaveDialog = showNeedSaveDialog,
                    onAddressUpdate = onAddressUpdate,
                    onUpdateColor = onUpdateColor,
                    onUpdateTitle = onUpdateTitle,
                    onSaveAction = onSaveAction,
                    onNeedSaveDismiss = { showNeedSaveDialog = false },
                    onCancelAction = onCancelAction,
                    onBackPress = onBackPress
                )

                LaunchedEffect(error){
                    error?.let{
                        snackbarHostState.showSnackbar(it)
                        onClearError()
                    }
                }

            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandEditorTopBar(
    uiState: LandEditorStates,
    onBackPress: () -> Unit,
    onActionUpdate: (LandEditorActions) -> Unit,
    onResetAction: () -> Unit
) {
    var showSubMenu by remember{
        mutableStateOf(false)
    }
    val title = remember(uiState){
        when(uiState){
            is LandEditorStates.AddPointState -> "Land Editor: Add Points"
            is LandEditorStates.AddBetweenPointState ->
                if(uiState.selectedIndex == -1)
                    if(uiState.startIndex == -1) "Land Editor: Select Point"
                    else if(uiState.endIndex == -1) "Land Editor: Select Next Point"
                    else "Land Editor: Place Point"
                else "Land Editor: Edit Point"
            is LandEditorStates.DeletePointState -> "Land Editor: Delete Points"
            is LandEditorStates.EditPointState ->
                if(uiState.selectedIndex == -1) "Land Editor: Select Point"
                else "Land Editor: Place Point"
            is LandEditorStates.EditColorState -> "Land Editor: Select Color"
            is LandEditorStates.EditTitleState -> "Land Editor: Select Title"
            is LandEditorStates.NeedLocation -> "Land Editor: Select Location"
            is LandEditorStates.NormalState ->
                if(uiState.newTitle.isBlank()) "Land Editor"
                else if(uiState.land.id > 0L) "#${uiState.land.id} ${uiState.newTitle}"
                else uiState.newTitle
            else -> "Land Editor"
        }
    }
    TopAppBar(
        title = {
            Text(
                text = title,
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
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Go Back"
                )
            }
        },
        actions = {
            if (
                uiState is LandEditorStates.NormalState ||
                uiState is LandEditorStates.EditState
            ) {
                IconButton(
                    onClick = {
                        showSubMenu = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Show Menu"
                    )
                }
                DropdownMenu(
                    expanded = showSubMenu,
                    onDismissRequest = { showSubMenu = false }
                ) {
                    LandEditorActions.values().forEach {
                        val text = when (it) {
                            LandEditorActions.ADD_POINTS -> "Add Points"
                            LandEditorActions.ADD_BETWEEN_POINTS -> "Add Point Between Points"
                            LandEditorActions.DELETE_POINTS -> "Delete Points"
                            LandEditorActions.EDIT_POINTS -> "Edit Points"
                            LandEditorActions.CHANGE_TITLE -> "Change Title"
                            LandEditorActions.CHANGE_COLOR -> "Change Color"
                        }
                        DropdownMenuItem(
                            text = { Text(text = text) },
                            onClick = {
                                showSubMenu = false
                                onActionUpdate(it)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(text = "Reset Changes") },
                        onClick = {
                            showSubMenu = false
                            onResetAction()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun LandEditorFab(
    uiState: LandEditorStates,
    onSubmitAction: () -> Unit,
    onSaveAction: () -> Unit
) {
    when(uiState){
        is LandEditorStates.NormalState ->
            ExtendedFloatingActionButton(
                onClick = onSaveAction,
                containerColor = Color(245, 245, 255, 255).copy(alpha = .75f),
                contentColor = Color(90, 90, 95, 255),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Land",
                    fontWeight = FontWeight.Bold
                )
            }
        is LandEditorStates.AddPointState,
        is LandEditorStates.AddBetweenPointState,
        is LandEditorStates.EditPointState,
        is LandEditorStates.DeletePointState ->
            ExtendedFloatingActionButton(
                onClick = onSubmitAction,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Changes",
                    fontWeight = FontWeight.Bold
                )
            }
        else -> {}
    }
}

@Composable
private fun LandEditorMap(
    uiState: LandEditorStates,
    cameraPositionState: CameraPositionState,
    onMapClick: (LatLng) -> Unit
) {
    var currentZoom by remember {
        mutableStateOf(cameraPositionState.position.zoom)
    }

    var pointsRadius by remember{
        mutableStateOf(cameraPositionState.position.calcRadiusFromZoom())
    }

    val border = remember(uiState){
        when(uiState){
            is LandEditorStates.AddPointState -> uiState.tempBorder
            is LandEditorStates.AddBetweenPointState -> uiState.tempBorder
            is LandEditorStates.DeletePointState -> uiState.tempBorder
            is LandEditorStates.EditPointState -> uiState.tempBorder
            is LandEditorStates.NormalState -> uiState.newBorder
            is LandEditorStates.EditState -> uiState.newBorder
            else -> emptyList()
        }
    }

    val holes = remember(uiState){
        when(uiState){
            is LandEditorStates.NeedLocation -> uiState.land.holes
            is LandEditorStates.NormalState -> uiState.newHoles
            is LandEditorStates.EditState -> uiState.newHoles
            else ->  emptyList()
        }
    }

    val color = remember(uiState){
        when(uiState){
            is LandEditorStates.NeedLocation -> uiState.land.color
            is LandEditorStates.NormalState -> uiState.newColor
            is LandEditorStates.EditState -> uiState.newColor
            else -> Land.emptyLand().color
        }
    }

    val showPoints = remember(uiState){
        when(uiState) {
            is LandEditorStates.AddPointState -> true
            is LandEditorStates.AddBetweenPointState -> true
            is LandEditorStates.DeletePointState -> true
            is LandEditorStates.EditPointState -> true
            else -> false
        }
    }

    val selectedPoints = remember(uiState){
        when(uiState) {
            is LandEditorStates.AddPointState -> {
                border.lastOrNull()?.let {
                    listOf(it)
                } ?: emptyList()
            }
            is LandEditorStates.AddBetweenPointState -> {
                if (uiState.selectedIndex != -1)
                    listOf(
                        border[uiState.selectedIndex]
                    )
                else if (uiState.startIndex != -1)
                    if (uiState.endIndex != -1)
                        listOf(
                            border[uiState.startIndex],
                            border[uiState.endIndex]
                        )
                    else
                        listOf(
                            border[uiState.startIndex]
                        )
                else
                    emptyList()
            }
            is LandEditorStates.EditPointState -> {
                if (uiState.selectedIndex != -1)
                    listOf(
                        border[uiState.selectedIndex]
                    )
                else
                    emptyList()
            }
            else -> {
                emptyList()
            }
        }
    }

    val properties = remember{
        MapProperties(mapType = MapType.SATELLITE)
    }

    GoogleMap(
        cameraPositionState = cameraPositionState,
        properties = properties,
        onMapClick = { point ->
            when(uiState){
                is LandEditorStates.AddPointState ->
                    onMapClick(point)
                is LandEditorStates.AddBetweenPointState ->
                    if(uiState.startIndex != -1 && uiState.endIndex != -1)
                        onMapClick(point)
                is LandEditorStates.EditPointState ->
                    if(uiState.selectedIndex != -1)
                        onMapClick(point)
                else -> {}
            }
        }
    ){
        if(border.isNotEmpty()){
            Polygon(
                points = border,
                holes = holes,
                strokeColor = color.copy(alpha = DefaultMapItemStrokeAlpha),
                fillColor = color.copy(alpha = DefaultMapItemFillAlpha),
                zIndex = 1f
            )
        }

        if(showPoints && currentZoom >= 12f){
            border.forEach {
                Circle(
                    center = it,
                    radius = pointsRadius,
                    strokeColor = color,
                    fillColor = color.copy(alpha =
                    if(selectedPoints.contains(it))
                        DefaultSelectedPointFillAlpha
                    else
                        DefaultUnselectedPointFillAlpha
                    ),
                    zIndex = 2f,
                    clickable = when(uiState){
                        is LandEditorStates.DeletePointState ->
                            true
                        is LandEditorStates.AddBetweenPointState ->
                            uiState.startIndex == -1 || uiState.endIndex == -1
                        is LandEditorStates.EditPointState ->
                            uiState.selectedIndex == -1
                        else ->
                            false
                    },
                    onClick = { circle ->
                        onMapClick(circle.center)
                    }
                )
            }
        }
    }

    if(!cameraPositionState.isMoving && currentZoom != cameraPositionState.position.zoom){
        SideEffect{
            currentZoom = cameraPositionState.position.zoom
            pointsRadius = cameraPositionState.position.calcRadiusFromZoom()
        }
    }
}

@Composable
private fun ShowDialogs(
    uiState: LandEditorStates,
    showNeedSaveDialog: Boolean,
    onAddressUpdate: (String, String) -> Unit,
    onUpdateColor: (Color) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onSaveAction: () -> Unit,
    onNeedSaveDismiss: () -> Unit,
    onCancelAction: () -> Unit,
    onBackPress: () -> Unit
) {

    when(uiState){
        is LandEditorStates.EditTitleState ->
            TextEditorDialog(
                textLabel = "Title",
                onSubmit = onUpdateTitle,
                onDismiss = onCancelAction,
                initialText = uiState.tempTitle
            )
        is LandEditorStates.EditColorState ->
            ColorPickerDialog(
                initValue = uiState.tempColor,
                onSubmitPress = onUpdateColor,
                onDismissDialog = onCancelAction
            )
        is LandEditorStates.NeedLocation ->
            DualTextEditorDialog(
                textLabel1 = "City",
                textLabel2 = "Country",
                properties = DialogProperties(
                    dismissOnClickOutside = false
                ),
                onSubmit = onAddressUpdate,
                onDismiss = onBackPress
            )

        else -> {
            if(showNeedSaveDialog){
                MessageDialog(
                    title = "Unsaved Changes",
                    message = "You about to leave without saving any change",
                    submitText = "Save",
                    cancelText = "Discard",
                    onSubmit = onSaveAction,
                    onCancel = onBackPress,
                    onDismiss = onNeedSaveDismiss
                )
            }
        }
    }
}



@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandLoadingScreen(){
    LandEditorScreen( uiState = LandEditorStates.LoadingState )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandNeedLocationScreen(){
    LandEditorScreen( uiState = LandEditorStates.NeedLocation(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandNormalScreen(){
    LandEditorScreen( uiState = LandEditorStates.NormalState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddPointScreen(){
    LandEditorScreen( uiState = LandEditorStates.AddPointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddBetweenPointScreen(){
    LandEditorScreen( uiState = LandEditorStates.AddBetweenPointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandDeletePointScreen(){
    LandEditorScreen( uiState = LandEditorStates.DeletePointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandEditPointScreen(){
    LandEditorScreen( uiState = LandEditorStates.EditPointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandEditTitleScreen(){
    LandEditorScreen( uiState = LandEditorStates.EditTitleState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandEditColorScreen(){
    LandEditorScreen( uiState = LandEditorStates.EditColorState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandNeedSaveScreen(){
    LandEditorScreen( uiState = LandEditorStates.NormalState(Land.emptyLand()), showNeedSave = true )
}
