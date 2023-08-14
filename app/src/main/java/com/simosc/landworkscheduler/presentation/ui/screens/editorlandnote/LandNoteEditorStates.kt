package com.simosc.landworkscheduler.presentation.ui.screens.editorlandnote

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import java.time.LocalDateTime

sealed class LandNoteEditorStates {
    data object LoadingState: LandNoteEditorStates()

    data object CantInit: LandNoteEditorStates()

    data class NormalState(
        val land: Land,
        val note: Note,
        val newTitle: String = note.title,
        val newDesc: String = note.desc,
        val newColor: Color = note.color,
        val newCenter: LatLng = note.center,
        val newRadius: Double = note.radius
    ): LandNoteEditorStates(){
        fun needSave(): Boolean{
            return note == note.copy(
                title = newTitle,
                desc = newDesc,
                color = newColor,
                center = newCenter,
                radius = newRadius
            )
        }
        fun getNewNote(): Note{
            return note.copy(
                title = newTitle,
                desc = newDesc,
                color = newColor,
                center = newCenter,
                radius = newRadius,
                created = if(note.id > 0L) note.created else LocalDateTime.now(),
                edited = LocalDateTime.now()
            )
        }
        fun revertChanges(): NormalState{
            return this.copy(
                newTitle = note.title,
                newDesc = note.desc,
                newColor = note.color,
                newCenter = note.center,
                newRadius = note.radius
            )
        }
    }

}