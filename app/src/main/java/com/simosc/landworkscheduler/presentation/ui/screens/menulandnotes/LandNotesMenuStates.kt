package com.simosc.landworkscheduler.presentation.ui.screens.menulandnotes

import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note

sealed class LandNotesMenuStates{

    data object LoadingState: LandNotesMenuStates()

    data object CantInit: LandNotesMenuStates()

    data class LoadedState(
        val land: Land,
        val notes: List<Note>
    ): LandNotesMenuStates()

}
