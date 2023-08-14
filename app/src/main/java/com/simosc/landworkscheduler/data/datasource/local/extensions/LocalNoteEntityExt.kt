package com.simosc.landworkscheduler.data.datasource.local.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.data.datasource.local.entities.LocalNoteEntity
import com.simosc.landworkscheduler.domain.model.Note
import java.time.LocalDateTime

fun LocalNoteEntity.toModel(): Note {
    return Note(
        id = id,
        lid = lid,
        title = title,
        desc = desc,
        color = Color(color),
        center = LatLng(centerLat, centerLong),
        radius = radius,
        created = LocalDateTime.parse(edited),
        edited = LocalDateTime.parse(edited),
    )
}

fun Note.toEntity(): LocalNoteEntity {
    return LocalNoteEntity(
        id = id,
        lid = lid,
        title = title,
        desc = desc,
        color = color.toArgb(),
        centerLat = center.latitude,
        centerLong = center.longitude,
        radius = radius,
        created = created.toString(),
        edited = edited.toString(),
    )
}