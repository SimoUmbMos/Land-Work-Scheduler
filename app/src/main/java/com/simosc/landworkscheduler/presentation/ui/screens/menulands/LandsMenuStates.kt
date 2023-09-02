package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import android.os.Parcelable
import com.simosc.landworkscheduler.domain.model.Land
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class LandsMenuStates: Parcelable {
    data object Loading: LandsMenuStates()

    sealed class Loaded(
        open val lands: List<Land>
    ):LandsMenuStates()

    @Parcelize
    class NormalState(
        override val lands: List<Land>
    ): Loaded(
        lands = lands
    ), Parcelable

    @Parcelize
    sealed class MultiSelectLands(
        override val lands: List<Land>,
        open val selectedLands: List<Land>
    ): Loaded(
        lands
    ), Parcelable {
        abstract fun onToggleLand(land: Land): MultiSelectLands
    }

    class ExportLands(
        override val lands: List<Land>,
        override val selectedLands: List<Land> = emptyList()
    ): MultiSelectLands(
        lands = lands,
        selectedLands = selectedLands
    ) {
        override fun onToggleLand(land: Land): MultiSelectLands {
            return ExportLands(
                lands = lands,
                selectedLands = selectedLands.toMutableList().apply{
                    if(contains(land)) remove(land)
                    else add(land)
                }.toList()
            )
        }
    }

    class DeleteLands(
        override val lands: List<Land>,
        override val selectedLands: List<Land> = emptyList()
    ): MultiSelectLands(
        lands = lands,
        selectedLands = selectedLands
    ) {
        override fun onToggleLand(land: Land): MultiSelectLands {
            return DeleteLands(
                lands = lands,
                selectedLands = selectedLands.toMutableList().apply{
                    if(contains(land)) remove(land)
                    else add(land)
                }.toList()
            )
        }
    }

}