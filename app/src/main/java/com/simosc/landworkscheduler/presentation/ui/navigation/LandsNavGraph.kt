package com.simosc.landworkscheduler.presentation.ui.navigation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.simosc.landworkscheduler.R
import com.simosc.landworkscheduler.core.config.DefaultAnimationDelayDurationMillis
import com.simosc.landworkscheduler.domain.extension.getLand
import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.presentation.activities.KmlReaderActivity
import com.simosc.landworkscheduler.presentation.ui.screens.editorland.LandEditorScreen
import com.simosc.landworkscheduler.presentation.ui.screens.editorland.LandEditorViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.editorlandnote.LandNoteEditorScreen
import com.simosc.landworkscheduler.presentation.ui.screens.editorlandnote.LandNoteEditorViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.menulandnotes.LandNotesMenuScreen
import com.simosc.landworkscheduler.presentation.ui.screens.menulandnotes.LandNotesMenuViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuActions
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuScreen
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuStates
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.previewland.LandPreviewScreen
import com.simosc.landworkscheduler.presentation.ui.screens.previewland.LandPreviewStates
import com.simosc.landworkscheduler.presentation.ui.screens.previewland.LandPreviewViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime


fun NavGraphBuilder.landsNavGraph(navController: NavController) {
    navigation(
        route = "lands",
        startDestination = "lands_menu"
    ){

        composable(
            route = "lands_menu"
        ){
            val ctx = LocalContext.current

            val viewModel = hiltViewModel<LandsMenuViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
            val isLoadingAction by viewModel.isLoadingAction.collectAsState()
            val isSearching by viewModel.isSearching.collectAsState()

            val createFileLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument(KmlFileExporter.MimeType)
            ){ newFileUri ->
                newFileUri?.let{
                    CoroutineScope(Dispatchers.IO).launch {
                        var fileGenerated = false
                        ctx.contentResolver.openOutputStream(it)?.use { outputStream ->
                            fileGenerated = viewModel.generateKml(outputStream)
                        }
                        if(!fileGenerated) {
                            it.toFile().delete()
                        }
                    }
                }?: viewModel.changeAction(LandsMenuActions.None)
            }

            LandsMenuScreen(
                uiState = uiState,
                errorMessage = errorMessage?.let { stringResource(id = it) },
                searchQuery = searchQuery,
                isSearching = isSearching,
                isLoadingAction = isLoadingAction,
                onBackPress = {
                    navController.popBackStack()
                },
                onLandPress = { land ->
                    if(uiState is LandsMenuStates.MultiSelectLands){
                        viewModel.toggleLand(land)
                    }else{
                        navController.navigate("land_preview/${land.id}")
                    }
                },
                onNewLandPress = {
                    navController.navigate("land_editor")
                },
                onSearchChange = {
                    viewModel.onSearchChange(it)
                },
                onActionChange = {
                    viewModel.changeAction(it)
                },
                onDeleteSelectedLands = {
                    viewModel.executeLandsDelete()
                },
                onExportSelectedLands = {
                    LocalDateTime.now().run{
                        viewModel.createFileToExport(
                            createFileLauncher,
                            ctx.getString(
                                R.string.land_menu_export_file_name,
                                year,monthValue,dayOfMonth,hour,minute,second,nano
                            )
                        )
                    }
                }
            )
            LaunchedEffect(Unit){
                delay(DefaultAnimationDelayDurationMillis)
                viewModel.loadLands()
            }
        }

        composable(
            route = "land_preview/{lid}",
            arguments = listOf(
                navArgument("lid"){
                    type = NavType.LongType
                }
            )
        ){
            val viewModel = hiltViewModel<LandPreviewViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            LandPreviewScreen(
                uiState = uiState,
                onBackPress = {
                    navController.popBackStack()
                },
                toLandZonesPress = {
                    TODO("Not yet implemented")
                },
                toLandNotesPress = {
                    uiState.let { state ->
                        if(state is LandPreviewStates.Loaded) {
                            navController.navigate(
                                "land_notes_menu/${state.land.id}"
                            )
                        }
                    }
                },
                onEditLandPress = {
                    uiState.let { state ->
                        if(state is LandPreviewStates.Loaded) {
                            navController.navigate(
                                "land_editor?lid=${state.land.id}"
                            )
                        }
                    }
                },
                onDeleteLandPress = {
                    if(uiState is LandPreviewStates.Loaded) {
                        viewModel.deleteLand()
                        navController.popBackStack()
                    }
                },
            )
            LaunchedEffect(Unit){
                viewModel.loadData(it.arguments!!.getLong("lid"))
            }
        }

        composable(
            route = "land_editor?lid={lid}",
            arguments = listOf(
                navArgument("lid"){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            val coroutineScope = rememberCoroutineScope()
            val viewModel = hiltViewModel<LandEditorViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            val error by viewModel.error.collectAsState(initial = null)
            val ctx = LocalContext.current
            val startImportActivity = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ){
                it.data?.getLand("export_land")?.let{ land ->
                    viewModel.onImportLand(land)
                }
            }
            LandEditorScreen(
                uiState = uiState,
                cameraPositionState = viewModel.cameraPositionState,
                error = error?.let{ stringResource(id = it) },
                onBackPress = navController::popBackStack,
                onActionUpdate = viewModel::setAction,
                onMapClick = viewModel::onMapClick,
                onAddressUpdate = { title, address ->
                    if(address.isNotBlank())
                        viewModel.setLandTitleAndAddress(title,address)
                    else
                        viewModel.setLandTitleAndAddress(title,"")
                },
                onUpdateTitle = viewModel::onUpdateTitle,
                onUpdateColor = viewModel::onUpdateColor,
                onSubmitAction = viewModel::onSubmitAction,
                onCancelAction = viewModel::onCancelAction,
                onResetAction = viewModel::onResetAction,
                onImportFromFile = {
                    startImportActivity.launch(
                        Intent(ctx,KmlReaderActivity::class.java)
                    )
                },
                onSaveAction = {
                    coroutineScope.launch {
                        if(viewModel.onSaveLand())
                            navController.popBackStack()
                    }
                }
            )
            LaunchedEffect(Unit){
                viewModel.setSelectedId(it.arguments?.getLong("lid") ?: 0L)
            }
        }

        composable(
            route = "land_notes_menu/{lid}",
            arguments = listOf(
                navArgument("lid"){
                    type = NavType.LongType
                }
            )
        ){
            val viewModel = hiltViewModel<LandNotesMenuViewModel>()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val isSearching by viewModel.isSearching.collectAsState()
            val uiState by viewModel.uiState.collectAsState()
            LandNotesMenuScreen(
                uiState = uiState,
                searchQuery = searchQuery,
                isSearching = isSearching,
                onBackPress = {
                    navController.popBackStack()
                },
                onSearchChange = { newQuery ->
                    viewModel.onSearchQueryUpdate(newQuery)
                },
                onNewNoteClick = { land ->
                    TODO()
                },
                onNoteClick = { note ->
                    TODO()
                }
            )
            LaunchedEffect(Unit){
                viewModel.setLandId(it.arguments!!.getLong("lid"))
            }
        }

        composable(
            route = "land_note_preview/{nid}",
            arguments = listOf(
                navArgument("nid"){
                    type = NavType.LongType
                }
            )
        ){
            TODO("Not yet implemented")
        }

        composable(
            route = "land_note_editor/{lid}?nid={nid}",
            arguments = listOf(
                navArgument("lid"){
                    type = NavType.LongType
                },
                navArgument("nid"){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            val viewModel = hiltViewModel<LandNoteEditorViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            val scope = rememberCoroutineScope()

            LandNoteEditorScreen(
                uiState = uiState,
                onBackPress = navController::popBackStack,
                onTitleUpdate = viewModel::onTitleUpdate,
                onDescUpdate = viewModel::onDescUpdate,
                onColorUpdate = viewModel::onColorUpdate,
                onCenterUpdate = viewModel::onCenterUpdate,
                onRadiusUpdate = viewModel::onRadiusUpdate,
                onRevertChanges = viewModel::onRevertChanges,
                onSaveNote = {
                    scope.launch {
                        if(viewModel.saveNote())
                            navController.popBackStack()
                    }
                },
                onDeleteNote = {
                    scope.launch {
                        viewModel.deleteNote()
                        navController.popBackStack()
                    }
                }
            )
            LaunchedEffect(Unit){
                viewModel.setDataIds(
                    landId = it.arguments!!.getLong("lid"),
                    noteId = it.arguments!!.getLong("nid")
                )
            }
        }

        composable(
            route = "land_zones_menu/{lid}",
            arguments = listOf(
                navArgument("lid"){
                    type = NavType.LongType
                }
            )
        ){
            TODO("Not yet implemented")
        }

        composable(
            route = "land_zone_preview/{zid}",
            arguments = listOf(
                navArgument("zid"){
                    type = NavType.LongType
                }
            )
        ){
            TODO("Not yet implemented")
        }

        composable(
            route = "land_zone_editor/{lid}?zid={zid}",
            arguments = listOf(
                navArgument("lid"){
                    type = NavType.LongType
                },
                navArgument("zid"){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            TODO("Not yet implemented")
        }

    }
}