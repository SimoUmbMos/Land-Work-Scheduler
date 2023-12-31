package com.simosc.landworkscheduler.presentation.ui.navigation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.simosc.landworkscheduler.domain.extension.getLand
import com.simosc.landworkscheduler.domain.files.KmlFileExporter
import com.simosc.landworkscheduler.presentation.activities.KmlReaderActivity
import com.simosc.landworkscheduler.presentation.ui.screens.editorland.LandEditorScreen
import com.simosc.landworkscheduler.presentation.ui.screens.editorland.LandEditorViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuScreen
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuStates
import com.simosc.landworkscheduler.presentation.ui.screens.menulands.LandsMenuViewModel
import com.simosc.landworkscheduler.presentation.ui.screens.previewland.LandPreviewScreen
import com.simosc.landworkscheduler.presentation.ui.screens.previewland.LandPreviewStates
import com.simosc.landworkscheduler.presentation.ui.screens.previewland.LandPreviewViewModel
import kotlinx.coroutines.launch


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
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val isLoadingData by viewModel.isLoadingData.collectAsStateWithLifecycle()
            val isLoadingAction by viewModel.isLoadingAction.collectAsStateWithLifecycle()
            val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
            val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle(
                initialValue = null
            )

            val createFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument(KmlFileExporter.MimeType),
                onResult = { viewModel.onCreateFileLauncherResult(uri = it,context = ctx) }
            )

            LandsMenuScreen(
                uiState = uiState,
                errorMessage = errorMessage?.let { stringResource(id = it) },
                searchQuery = searchQuery,
                isLoadingData = isLoadingData,
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
                    viewModel.onExportSelectedLands(ctx, createFileLauncher)
                },
                onRefreshData = {
                    viewModel.startSync()
                }
            )

             LaunchedEffect(Unit){
                 viewModel.startSync()
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
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                viewModel.setLandId(it.arguments!!.getLong("lid"))
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
            val viewModel = hiltViewModel<LandEditorViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle(
                initialValue = null
            )

            val coroutineScope = rememberCoroutineScope()
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
            TODO("Not yet implemented")
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
            TODO("Not yet implemented")
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