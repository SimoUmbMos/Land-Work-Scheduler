package com.simosc.landworkscheduler.presentation.ui.screens.previewland

import com.simosc.landworkscheduler.domain.model.Land

sealed class LandPreviewStates {
    data object LoadingState : LandPreviewStates()
    data object LandNotFoundState : LandPreviewStates()
    class Loaded(val land: Land): LandPreviewStates()
}
