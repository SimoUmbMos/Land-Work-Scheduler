package com.simosc.landworkscheduler.presentation.ui.screens.kmllandreader

import com.simosc.landworkscheduler.domain.model.Land

sealed class KmlLandReaderStates{
    data object WaitingFile: KmlLandReaderStates()
    data object LoadingFile: KmlLandReaderStates()
    data object ErrorParsing: KmlLandReaderStates()
    data object NoLandsFound: KmlLandReaderStates()

    sealed class LoadedLands(
        val lands: List<Land>
    ): KmlLandReaderStates()
    class NoLandSelected(
        lands: List<Land>
    ): LoadedLands(
        lands = lands
    )
    class LandSelected(
        lands: List<Land>,
        val selectedLand: Land,
    ): LoadedLands(
        lands = lands
    )
}
