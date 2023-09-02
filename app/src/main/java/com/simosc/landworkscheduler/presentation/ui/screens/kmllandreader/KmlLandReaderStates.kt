package com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader

import android.os.Parcelable
import com.simosc.landworkscheduler.domain.model.Land
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class KmlLandReaderStates: Parcelable{
    data object WaitingFile: KmlLandReaderStates()
    data object LoadingFile: KmlLandReaderStates()
    data object ErrorParsing: KmlLandReaderStates()
    data object NoLandsFound: KmlLandReaderStates()

    @Parcelize
    sealed class LoadedLands(
        open val lands: List<Land>
    ): KmlLandReaderStates(), Parcelable

    class NoLandSelected(
        override val lands: List<Land>
    ): LoadedLands(
        lands = lands
    )

    class LandSelected(
        override val lands: List<Land>,
        val selectedLand: Land,
    ): LoadedLands(
        lands = lands
    )
}
