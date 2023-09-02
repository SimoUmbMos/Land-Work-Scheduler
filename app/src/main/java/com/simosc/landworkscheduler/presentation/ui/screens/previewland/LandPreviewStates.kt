package com.simosc.landworkscheduler.presentation.ui.screens.previewland

import android.os.Parcelable
import com.simosc.landworkscheduler.domain.model.Land
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class LandPreviewStates: Parcelable {
    data object LoadingState : LandPreviewStates()
    data object LandNotFoundState : LandPreviewStates()
    class Loaded(val land: Land): LandPreviewStates()
}
