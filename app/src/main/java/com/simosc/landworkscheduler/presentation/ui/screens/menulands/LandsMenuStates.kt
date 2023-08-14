package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import com.simosc.landworkscheduler.domain.model.Land

sealed class LandsMenuStates {
    data object Loading: LandsMenuStates()
    data class Loaded(
        val lands: List<Land>
    ): LandsMenuStates()
}