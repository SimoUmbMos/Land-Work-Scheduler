package com.simosc.landworkscheduler.presentation.ui.screens.menulands

import com.simosc.landworkscheduler.domain.model.Land

sealed class LandsMenuStates {
    data object Loading: LandsMenuStates()
    class Loaded(
        val lands: List<Land>
    ): LandsMenuStates()

    sealed class MultiSelectLands(
        val lands: List<Land>,
        val selectedLands: List<Land>
    ): LandsMenuStates(){
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