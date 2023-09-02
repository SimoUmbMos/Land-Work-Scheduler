package com.simosc.landworkscheduler.presentation.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
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

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                    when(event){
                        Lifecycle.Event.ON_RESUME -> viewModel.startLocationUpdates()
                        Lifecycle.Event.ON_PAUSE -> viewModel.stopLocationUpdates()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
                }
            }

            @Suppress("DEPRECATION") val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.rotation ?: -1
            } else {
                (context as Activity).windowManager.defaultDisplay.rotation
            }
            val configuration = LocalConfiguration.current
            LaunchedEffect(configuration, rotation){
                viewModel.setOrientation(configuration.orientation, rotation)
            }

            LaunchedEffect(Unit){
                viewModel.startDataUpdates()
            }
        }

    }
}