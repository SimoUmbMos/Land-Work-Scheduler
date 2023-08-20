package com.simosc.landworkscheduler.presentation.ui.screens.editorland

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.twotone.Create
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultMapItemFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultMapItemStrokeAlpha
import com.simosc.landworkscheduler.core.config.DefaultSelectedPointFillAlpha
import com.simosc.landworkscheduler.core.config.DefaultUnselectedPointFillAlpha
import com.simosc.landworkscheduler.domain.extension.calcRadiusFromZoom
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.presentation.ui.components.content.LoadingContentComponent
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.ColorPickerDialog
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.MessageDialog
import com.simosc.landworkscheduler.presentation.ui.components.dialogs.TextEditorDialog
import com.simosc.landworkscheduler.presentation.ui.components.topbar.DefaultTopAppBar


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
    onImportFromFile: () -> Unit = {},
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
                onImportFromFile = onImportFromFile,
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
                        text = stringResource(id = R.string.land_editor_waiting_land_address)
                    )

                    else -> LoadingContentComponent()
                }

                ShowDialogs(
                    uiState = uiState,
                    showNeedSaveDialog = showNeedSaveDialog,
                    onAddressUpdate = onAddressUpdate,
                    onImportFromFile = onImportFromFile,
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

@Composable
private fun LandEditorTopBar(
    uiState: LandEditorStates,
    onBackPress: () -> Unit,
    onActionUpdate: (LandEditorActions) -> Unit,
    onResetAction: () -> Unit,
    onImportFromFile: () -> Unit = {}
) {
    var showSubMenu by remember{
        mutableStateOf(false)
    }
    DefaultTopAppBar(
        title = stringResource(id = R.string.land_editor_title_default),
        subTitle = when(uiState){
            is LandEditorStates.AddPointState -> {
                stringResource(id = R.string.land_editor_subtitle_add_points)
            }

            is LandEditorStates.AddBetweenPointState -> when{
                uiState.selectedIndex != -1 -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_add_point_between_edit_placed_point
                    )
                }

                uiState.startIndex == -1 -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_add_point_between_select_first_point
                    )
                }

                uiState.endIndex == -1 -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_add_point_between_select_second_point
                    )
                }

                else -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_add_point_between_place_new_point
                    )
                }
            }

            is LandEditorStates.DeletePointState -> {
                stringResource(id = R.string.land_editor_subtitle_delete_points)
            }

            is LandEditorStates.EditPointState -> when{
                uiState.selectedIndex < 0 -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_edit_points_select_point
                    )
                }

                else -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_edit_points_edit_selected_point
                    )
                }
            }

            is LandEditorStates.EditColorState -> {
                stringResource(id = R.string.land_editor_subtitle_change_land_color)
            }

            is LandEditorStates.EditTitleState -> {
                stringResource(id = R.string.land_editor_subtitle_change_land_title)
            }

            is LandEditorStates.NeedLocation -> {
                stringResource(id = R.string.land_editor_subtitle_select_location)
            }

            is LandEditorStates.NormalState -> when{
                uiState.newTitle.isBlank() -> {
                    null
                }

                uiState.land.id <= 0L -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_new_land_default,
                        uiState.newTitle
                    )
                }

                else -> {
                    stringResource(
                        id = R.string.land_editor_subtitle_edit_land_default,
                        uiState.land.id,
                        uiState.newTitle
                    )
                }
            }

            else -> {
                null
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackPress
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.navigate_back_label)
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
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(id = R.string.land_editor_action_show_menu)
                    )
                }
                DropdownMenu(
                    expanded = showSubMenu,
                    onDismissRequest = { showSubMenu = false }
                ) {
                    LandEditorActions.values().forEach { landEditorAction ->
                        val text = when (landEditorAction) {
                            LandEditorActions.ADD_POINTS ->
                                R.string.land_editor_action_add_points
                            LandEditorActions.ADD_BETWEEN_POINTS ->
                                R.string.land_editor_action_add_points_between_points
                            LandEditorActions.DELETE_POINTS ->
                                R.string.land_editor_action_delete_points
                            LandEditorActions.EDIT_POINTS ->
                                R.string.land_editor_action_edit_points
                            LandEditorActions.CHANGE_TITLE ->
                                R.string.land_editor_action_change_title
                            LandEditorActions.CHANGE_COLOR ->
                                R.string.land_editor_action_change_color
                        }.let{ stringId -> stringResource(id = stringId)}
                        DropdownMenuItem(
                            text = { Text(text = text) },
                            onClick = {
                                showSubMenu = false
                                onActionUpdate(landEditorAction)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = R.string.land_editor_action_reset_changes
                                )
                            )
                        },
                        onClick = {
                            showSubMenu = false
                            onResetAction()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(
                                    id = R.string.land_editor_action_import_from_file
                                )
                            )
                        },
                        onClick = {
                            showSubMenu = false
                            onImportFromFile()
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
                    imageVector = Icons.TwoTone.Done,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.land_editor_action_save_land),
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
                    imageVector = Icons.TwoTone.Create,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.land_editor_action_save_changes),
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
        mutableFloatStateOf(cameraPositionState.position.zoom)
    }

    var pointsRadius by remember{
        mutableDoubleStateOf(cameraPositionState.position.calcRadiusFromZoom())
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
    onImportFromFile: () -> Unit,
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
                textLabel = stringResource(id = R.string.land_editor_land_title_label),
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
            NewLandDialog(
                initialLandTitle = uiState.land.title,
                onImportFromFile = onImportFromFile,
                onSubmit = onAddressUpdate,
                onDismiss = onBackPress
            )

        else -> {
            if(showNeedSaveDialog){
                MessageDialog(
                    title = stringResource(id = R.string.unsaved_changes_label),
                    message = stringResource(id = R.string.unsaved_changes_text),
                    submitText = stringResource(id = R.string.save_label),
                    cancelText = stringResource(id = R.string.discard_label),
                    onSubmit = onSaveAction,
                    onCancel = onBackPress,
                    onDismiss = onNeedSaveDismiss
                )
            }
        }
    }
}

@Composable
private fun NewLandDialog(
    initialLandTitle: String = "",
    initialLandAddress: String = "",
    onImportFromFile: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
){
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(
                        id = R.string.land_editor_dialog_title_label
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                var landTitle by remember(initialLandTitle){
                    mutableStateOf(initialLandTitle)
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = landTitle,
                    onValueChange = { landTitle = it },
                    singleLine = true,
                    label = {
                        Text(
                            text = stringResource(id = R.string.land_editor_dialog_land_title_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                var landAddress by remember(initialLandAddress){
                    mutableStateOf(initialLandAddress)
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = landAddress,
                    onValueChange = { landAddress = it },
                    singleLine = true,
                    label = {
                        Text(
                            text = stringResource(id = R.string.land_editor_dialog_land_address_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.land_editor_dialog_land_address_placeholder),
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        onSubmit(
                            landTitle,
                            landAddress
                        )
                    },
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.submit_label
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(
                        id = R.string.land_editor_dialog_other_actions_label
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onImportFromFile,
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.land_editor_dialog_button_import_from_file
                        )
                    )
                }
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
private fun PreviewEditorLandNormalScreenDefault(){
    LandEditorScreen( uiState = LandEditorStates.NormalState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandNormalScreenNewLand(){
    LandEditorScreen(
        uiState = LandEditorStates.NormalState(
            Land.emptyLand().copy(
                title = "Land"
            )
        )
    )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandNormalScreenEditLand(){
    LandEditorScreen(
        uiState = LandEditorStates.NormalState(
            Land.emptyLand().copy(
                id = 1L,
                title = "Land"
            )
        )
    )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddPointScreen(){
    LandEditorScreen( uiState = LandEditorStates.AddPointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddBetweenPointScreenDefault(){
    LandEditorScreen( uiState = LandEditorStates.AddBetweenPointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddBetweenPointScreenSelectedFirst(){
    LandEditorScreen(
        uiState = LandEditorStates.AddBetweenPointState(
            land = Land.emptyLand().copy(
                border = listOf(
                    LatLng(0.0, 0.0),
                    LatLng(1.0, 0.0),
                    LatLng(1.0, 1.0),
                    LatLng(0.0, 1.0),
                )
            ),
            startIndex = 0
        )
    )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddBetweenPointScreenSelectedSecond(){
    LandEditorScreen(
        uiState = LandEditorStates.AddBetweenPointState(
            land = Land.emptyLand().copy(
                border = listOf(
                    LatLng(0.0, 0.0),
                    LatLng(1.0, 0.0),
                    LatLng(1.0, 1.0),
                    LatLng(0.0, 1.0),
                )
            ),
            startIndex = 0,
            endIndex = 1
        )
    )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandAddBetweenPointScreenPlacedPoint(){
    LandEditorScreen(
        uiState = LandEditorStates.AddBetweenPointState(
            land = Land.emptyLand().copy(
                border = listOf(
                    LatLng(0.0, 0.0),
                    LatLng(1.0, 0.0),
                    LatLng(1.0, 1.0),
                    LatLng(0.0, 1.0),
                )
            ),
            startIndex = 0,
            selectedIndex = 1,
            endIndex = 2,
        )
    )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandDeletePointScreen(){
    LandEditorScreen( uiState = LandEditorStates.DeletePointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandEditPointScreenDefault(){
    LandEditorScreen( uiState = LandEditorStates.EditPointState(Land.emptyLand()) )
}

@Composable
@Preview( showBackground = true, showSystemUi = true )
private fun PreviewEditorLandEditPointScreenSelectedPoint(){
    LandEditorScreen(
        uiState = LandEditorStates.EditPointState(
            land = Land.emptyLand().copy(
                border = listOf(
                    LatLng(0.0, 0.0),
                    LatLng(1.0, 0.0),
                    LatLng(1.0, 1.0),
                    LatLng(0.0, 1.0),
                )
            ),
            selectedIndex = 0
        )
    )
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
