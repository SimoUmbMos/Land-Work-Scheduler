package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import com.simosc.landworkscheduler.domain.model.Land

sealed class LandsMenuStates {
    data object Loading: LandsMenuStates()

    sealed class Loaded(
        val lands: List<Land>
    ):LandsMenuStates()

    class NormalState(
        lands: List<Land>
    ): Loaded(
        lands = lands
    )

    sealed class MultiSelectLands(
        lands: List<Land>,
        val selectedLands: List<Land>
    ): Loaded(
        lands
    ){
        abstract fun onToggleLand(land: Land): MultiSelectLands
    }

    class ExportLands(
        lands: List<Land>,
        selectedLands: List<Land> = emptyList()
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
        lands: List<Land>,
        selectedLands: List<Land> = emptyList()
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