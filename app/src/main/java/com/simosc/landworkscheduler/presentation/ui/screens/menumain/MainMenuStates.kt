package com.simosc.landworkscheduler.presentation.ui.screens.menumain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MainMenuStates: Parcelable{
    data object Loading: MainMenuStates()
    data class Loaded(val todayWorksCount: Long): MainMenuStates()
}
