package com.simosc.landworkscheduler.presentation.ui.screens.menumain

sealed class MainMenuStates{
    data object Loading: MainMenuStates()
    data class Loaded(val todayWorksCount: Long): MainMenuStates()
}
