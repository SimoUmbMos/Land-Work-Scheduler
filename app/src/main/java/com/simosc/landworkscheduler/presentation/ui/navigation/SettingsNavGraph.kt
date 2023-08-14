package com.simosc.landworkscheduler.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.settingsNavGraph(navController: NavController) {
    navigation(
        route = "settings",
        startDestination = "settings_menu"
    ){

        composable(
            route = "settings_menu"
        ) {
            TODO("Not yet implemented")
        }
    }
}