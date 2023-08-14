package com.simosc.landworkscheduler.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

fun NavGraphBuilder.worksNavGraph(navController: NavController) {
    navigation(
        route = "works",
        startDestination = "works_menu"
    ){

        composable(
            route = "works_menu"
        ) {
            TODO("Not yet implemented")
        }

        composable(
            route = "works_preview/{wid}",
            arguments = listOf(
                navArgument("wid") { type = NavType.LongType }
            )
        ) {
            TODO("Not yet implemented")
        }

        composable(
            route = "works_editor?wid={wid}",
            arguments = listOf(
                navArgument("wid") { type = NavType.LongType }
            )
        ) {
            TODO("Not yet implemented")
        }
    }
}