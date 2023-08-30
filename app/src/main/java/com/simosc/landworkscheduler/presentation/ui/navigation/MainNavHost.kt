package com.simosc.landworkscheduler.presentation.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simosc.landworkscheduler.core.config.DefaultNavHostAnimationDurationMillis
import com.simosc.landworkscheduler.presentation.ui.screens.menumain.MainMenuScreen
import com.simosc.landworkscheduler.presentation.ui.screens.menumain.MainMenuStates
import com.simosc.landworkscheduler.presentation.ui.screens.menumain.MainMenuViewModel

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onFirstScreenFullyLoaded: () -> Unit = {},
) {
    val enterTransition = remember{
        fadeIn(tween(delayMillis = DefaultNavHostAnimationDurationMillis))
    }
    val exitTransition = remember{
        fadeOut(tween(delayMillis = DefaultNavHostAnimationDurationMillis))
    }
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "main_menu",
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { enterTransition },
        popExitTransition = { exitTransition },

    ) {
        composable(
            route = "main_menu"
        ) {
            val viewModel = hiltViewModel<MainMenuViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MainMenuScreen(
                uiState = uiState,
                onAppSettingsClick = {
                    navController.navigate("settings")
                },
                onLandsMenuClick = {
                    navController.navigate("lands")
                },
                onSchedulesMenuClick = {
                    navController.navigate("works")
                },
                onLiveTrackingClick = {
                    navController.navigate("tracking")
                }
            )
            LaunchedEffect(Unit) {
                viewModel.loadData()
            }
            LaunchedEffect(uiState) {
                if(uiState is MainMenuStates.Loaded)
                    onFirstScreenFullyLoaded()
            }
        }

        settingsNavGraph(navController = navController)

        landsNavGraph(navController = navController)

        worksNavGraph(navController = navController)

        trackingNavGraph(navController = navController)

    }
}
