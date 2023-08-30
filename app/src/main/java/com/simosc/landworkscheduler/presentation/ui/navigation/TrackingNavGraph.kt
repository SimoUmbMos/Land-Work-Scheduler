package com.simosc.landworkscheduler.presentation.ui.navigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.simosc.landworkscheduler.presentation.ui.screens.previewuserlocation.LiveTrackingScreen
import com.simosc.landworkscheduler.presentation.ui.screens.previewuserlocation.LiveTrackingViewModel

fun NavGraphBuilder.trackingNavGraph(navController: NavController) {
    navigation(
        route = "tracking",
        startDestination = "live_tracking"
    ){

        composable(
            route = "live_tracking"
        ) {
            val viewModel = hiltViewModel<LiveTrackingViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
            val userAzimuth by viewModel.userAzimuth.collectAsStateWithLifecycle()

            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){ result ->
                viewModel.onPermissionsResult(result)
            }

            LiveTrackingScreen(
                uiState = uiState,
                userLocation = userLocation,
                userAzimuth = userAzimuth,
                onBackPress = {
                    navController.popBackStack()
                },
                onAskLocationPermission = {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onOpenLocationProviderSettings = {
                    viewModel.setLoadingState()
                    context.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                },
                onStartLocationUpdates = {
                    viewModel.startLocationUpdates()
                }
            )
            LaunchedEffect(context){
                val permissionFineLocation = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val permissionCoarseLocation = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if( permissionFineLocation || permissionCoarseLocation )
                    viewModel.onPermissionsResult(
                        mapOf(
                            Manifest.permission.ACCESS_FINE_LOCATION to permissionFineLocation,
                            Manifest.permission.ACCESS_COARSE_LOCATION to permissionCoarseLocation,
                        )
                    )
                else
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
            }
            DisposableEffect(lifecycleOwner) {
                val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                    if(event == Lifecycle.Event.ON_RESUME)
                        viewModel.startLocationUpdates()
                    else if(event == Lifecycle.Event.ON_PAUSE)
                        viewModel.stopLocationUpdates()
                }
                lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
                }
            }
            LaunchedEffect(Unit){
                viewModel.startDataUpdates()
            }
        }

    }
}