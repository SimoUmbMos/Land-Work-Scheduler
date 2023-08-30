package com.simosc.landworkscheduler.domain.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.core.config.DefaultNoteColor
import java.time.LocalDateTime

data class Note(
    val id: Long,
    val lid: Long,
    val title: String,
    val desc: String,
    val color: Color,
    val center: LatLng,
    val radius: Double,
    val created: LocalDateTime,
    val edited: LocalDateTime,
){
    companion object {
        fun emptyNote(
            lid: Long,
            center: LatLng
        ) = Note(
            id = 0L,
            lid = lid,
            title = "",
            desc = "",
            color = DefaultNoteColor,
            center = center,
            radius = 50.0,
            created = LocalDateTime.now(),
            edited = LocalDateTime.now()
        )
    }
}
